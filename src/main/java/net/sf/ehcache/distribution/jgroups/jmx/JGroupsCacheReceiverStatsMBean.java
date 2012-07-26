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

/**
 * Describes statistics about the JGroups Receiver bean 
 * 
 * @author Eric Dalquist
 */
public interface JGroupsCacheReceiverStatsMBean {
    /**
     * @return Number of remove_all events received
     */
    long getRemoveAllCount();
    /**
     * @return Rate of remove_all event reception over the last second
     */
    double getRemoveAllRate();
    
    /**
     * @return Number of remove events received where the local element existed.
     */
    long getRemoveExistingCount();
    /**
     * @return Rate of remove event reception where the local element existed over the last second
     */
    double getRemoveExistingRate();
    
    /**
     * @return Number of remove events received where the local element did not exist
     */
    long getRemoveNotExistingCount();
    /**
     * @return Rate of remove event reception where the local element did not exist over the last second
     */
    double getRemoveNotExistingRate();
    
    /**
     * @return Number of put events received
     */
    long getPutCount();
    /**
     * @return Rate of put event reception over the last second
     */
    double getPutRate();
    
    /**
     * @return the bootstrap request event count
     */
    long getBootstrapRequestCount();
    
    /**
     * @return the bootstrap complete event count
     */
    long getBootstrapCompleteCount();
    
    /**
     * @return the bootstrap incomplete event count
     */
    long getBootstrapIncompleteCount();
    
    /**
     * @return the bootstrap response event count
     */
    long getBootstrapResponseCount();
}
