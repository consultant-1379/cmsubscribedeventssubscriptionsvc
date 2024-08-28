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

import static com.ericsson.oss.services.cmsubscribedevents.constants.InstrumentationConstants.CM_SUBSCRIBED_EVENTS_NBI_SUBSCRIPTION_DELETED;
import static com.ericsson.oss.services.cmsubscribedevents.constants.InstrumentationConstants.SUBSCRIPTION_ID;

import java.util.HashMap;

import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionService;
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.api.SubscriptionInstrumentation;
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription;

import org.slf4j.Logger;

/**
 * Responsible for deleting subscriptions after heartbeat failure
 *
 */
public class ContinuousHeartbeatFailureSubscriptionDeleter {

    @Inject
    private Logger logger;

    @Inject
    private InternalFmAlarmSender internalFmAlarmSender;

    @EServiceRef
    private SubscriptionService subscriptionService;

    @EServiceRef
    private SubscriptionInstrumentation subscriptionInstrumentation;

    @Inject
    SystemRecorder systemRecorder;

    /**
     * Delete specified subscription and send corresponding FM alarm
     * @param subscription
     * @return boolean
     *     true if subscription has been successfully deleted and alarm successfully sent
     */
    public boolean deleteSubscription(final Subscription subscription) {
        boolean success = true;
        try {
            subscriptionService.deleteSubscription(subscription.getNtfSubscriptionControl().getId());
            internalFmAlarmSender.sendFmAlarmForDeleteSubscription(subscription);
            recordDeletionSuccess(subscription.getNtfSubscriptionControl().getId());
        } catch (final Exception e) {
            logger.error("Continuous Heartbeat Monitoring Failed to delete subscription with id {}. {}", subscription.getNtfSubscriptionControl().getId(), e );
            subscriptionInstrumentation.incrementFailedSubscriptionDeletion();
            success = false;
        }

        return success;
    }

    private void recordDeletionSuccess(final int id) {
        logger.info("Successfully deleted SubscriptionId:{}", id);
        systemRecorder.recordCommand("DELETE", CommandPhase.FINISHED_WITH_SUCCESS, "cmsubscribedeventssubscriptionsvc", "subscriptions", "Subscription ID:" + id);

        final HashMap<String, Object> subscriptionIdMap = new HashMap<>();
        subscriptionIdMap.put(SUBSCRIPTION_ID, id);
        systemRecorder.recordEventData(CM_SUBSCRIBED_EVENTS_NBI_SUBSCRIPTION_DELETED, subscriptionIdMap);

        subscriptionInstrumentation.incrementSuccessfulSubscriptionDeletion();
    }
}
