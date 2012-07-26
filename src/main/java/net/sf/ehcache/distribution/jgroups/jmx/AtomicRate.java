/**
 *  Copyright 2003-2010 Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.sf.ehcache.distribution.jgroups.jmx;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Tracks a rate (counter / time) using a bucket based approach to reduce overhead.
 * <p/>
 * This is thread safe
 * 
 * @author Eric Dalquist
 */
public final class AtomicRate {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final RateBucket[] rateBuckets;
    private final TimeUnit rateUnit;
    private final long rateDuration;
    private final long bucketDuration;
    private int newestBucket;
    
    /**
     * @param duration Duration the rate spans
     * @param unit The time unit for the duration
     */
    public AtomicRate(int duration, TimeUnit unit) {
        final int buckets = 10;
        this.newestBucket = 0;
        this.rateBuckets = new RateBucket[buckets];
        this.rateBuckets[this.newestBucket] = new RateBucket();
        
        this.rateUnit = unit;
        this.rateDuration = rateUnit.toNanos(duration);
        this.bucketDuration = this.rateDuration / buckets;
    }
    
    /**
     * @return The {@link TimeUnit} the rate is over
     */
    public TimeUnit getRateUnit() {
        return rateUnit;
    }

    /**
     * @return The duration the rate covers, returned in the scale of the {@link #getRateUnit()}
     */
    public long getRateDuration() {
        return rateUnit.convert(rateDuration, TimeUnit.NANOSECONDS);
    }

    /**
     * Increment the counter
     */
    public void count() {
        final Lock readLock = this.readWriteLock.readLock();
        readLock.lock();
        try {
            //Get the most recent bucket
            RateBucket rateBucket = this.rateBuckets[this.newestBucket];
            
            //If the bucket is old, create a new one
            if (rateBucket.start + bucketDuration < System.nanoTime()) {
                //Upgrade the lock and recheck the age of the newest bucket
                readLock.unlock();
                final Lock writeLock = this.readWriteLock.writeLock();
                writeLock.lock();
                try {
                    rateBucket = this.rateBuckets[this.newestBucket];
                    if (rateBucket.start + bucketDuration < System.nanoTime()) {
                        rateBucket = new RateBucket();
                        this.newestBucket = (this.newestBucket + 1) % this.rateBuckets.length;
                        this.rateBuckets[this.newestBucket] = rateBucket;
                    }
                } finally {
                    //Downgrade the lock (don't need to bother with read lock then write unlock and this is safer)
                    writeLock.unlock();
                    readLock.lock();
                }
            }
            
            //Increment the bucket's counter
            rateBucket.count.incrementAndGet();
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * @return The current rate. The rate returned is in the {@link TimeUnit} used provided in the constructor.
     */
    public double getRate() {
        final long now = System.nanoTime();
        int count = 0;
        long duration = 0;
        
        final Lock readLock = this.readWriteLock.readLock();
        readLock.lock();
        try {
            int bucketIndex = this.newestBucket;
            while (true) {
                final RateBucket bucket = this.rateBuckets[bucketIndex];
                //Stop counting if we hit a null bucket or a bucket older than the max age of the rate window
                if (bucket == null || bucket.start + this.rateDuration < now) {
                    break;
                }

                count += bucket.count.get();
                duration += Math.min(this.bucketDuration, now - bucket.start);

                bucketIndex = bucketIndex - 1;
                if (bucketIndex < 0) {
                    bucketIndex = this.rateBuckets.length - 1;
                }
            }
        } finally {
            readLock.unlock();
        }
        
        
        if (duration == 0) {
            return Double.NaN;
        }
        
        return ((double)count / duration) * this.rateUnit.toNanos(1);
    }
    
    /**
     * Tracks the count and start time for each bucket
     */
    private static final class RateBucket {
        private final long start = System.nanoTime();
        private final AtomicInteger count = new AtomicInteger();
        
        @Override
        public String toString() {
            return "RateBucket [start=" + start + ", count=" + count + "]";
        }
    }
}
