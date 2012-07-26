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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * Test {@link AtomicRate} class
 * 
 * @author Eric Dalquist
 */
public class AtomicRateTest {
    @Test
    public void testConsistentRate() throws Exception {
        final AtomicRate atomicRate = new AtomicRate(1, TimeUnit.SECONDS);
        assertEquals(0.0, atomicRate.getRate());
        
        for (int i = 0; i < 10; i++) {
            countAndWait(atomicRate, 1234, 101);
            assertEquals(12340.0, atomicRate.getRate(), 10);
        }
    }
    
    @Test
    public void testBucketSkippingRate() throws Exception {
        final AtomicRate atomicRate = new AtomicRate(1, TimeUnit.SECONDS);
        assertEquals(0.0, atomicRate.getRate());
        
        for (int i = 0; i < 9; i++) {
            countAndWait(atomicRate, 1234, 101);
            assertEquals(12340.0, atomicRate.getRate(), 10);
            
            if (i % 3 == 0) {
                Thread.sleep(201);
            }
        }
    }
    
    @Test
    public void testRateSkippingRate() throws Exception {
        final AtomicRate atomicRate = new AtomicRate(1, TimeUnit.SECONDS);
        assertEquals(0.0, atomicRate.getRate());
        
        for (int i = 0; i < 2; i++) {
            countAndWait(atomicRate, 1234, 101);
            assertEquals(12340.0, atomicRate.getRate(), 10);
            
            Thread.sleep(1001);
        }
    }
    
    @Test
    public void testVariableRate() throws Exception {
        final AtomicRate atomicRate = new AtomicRate(1, TimeUnit.SECONDS);
        assertEquals(0.0, atomicRate.getRate());
        
        for (int i = 0; i < 5; i++) {
            countAndWait(atomicRate, 1234 * (i + 1), 101);
            
            switch (i) {
                case 0: {
                    assertEquals(12340.0, atomicRate.getRate(), 10);
                    break;
                }
                case 1: {
                    assertEquals(18510.0, atomicRate.getRate(), 10);
                    break;
                }
                case 2: {
                    assertEquals(24680.0, atomicRate.getRate(), 10);
                    break;
                }
                case 3: {
                    assertEquals(30850.0, atomicRate.getRate(), 10);
                    break;
                }
                case 4: {
                    assertEquals(37020.0, atomicRate.getRate(), 10);
                    break;
                }
                default: { 
                    fail();
                }
            }
        }
    }

    private void countAndWait(final AtomicRate atomicRate, final int count, final int wait) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            atomicRate.count();
        }
        Thread.sleep(wait);
    }
}
