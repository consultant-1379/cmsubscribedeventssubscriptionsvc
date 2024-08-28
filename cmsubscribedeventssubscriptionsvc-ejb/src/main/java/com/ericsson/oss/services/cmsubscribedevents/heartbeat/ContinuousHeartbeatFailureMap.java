/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.cmsubscribedevents.heartbeat;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores information on consecutive heart beat failures.
 *
 */
public class ContinuousHeartbeatFailureMap {

    private final Map<Integer,Integer> failureMap = new HashMap<>();

    /**
     * Remove heartbeat failure recording for a subscription by id
     * @param id
     */
    public void clearSubscriptionById(final Integer id) {
        failureMap.remove(id);
    }

    /**
     * Decides if a subscription determined by ID should be deleted based on previous heart beats.
     * @param id
     * @return true
     *     if the subscription should be deleted
     */
    public boolean shouldSubscriptionBeDeleted(final Integer id) {
        Integer currentFailureCount = failureMap.get(id);
        if (currentFailureCount == null) {
            currentFailureCount = 0;
        }
        currentFailureCount++;
        failureMap.put(id, currentFailureCount);
        return currentFailureCount > 2;
    }

}
