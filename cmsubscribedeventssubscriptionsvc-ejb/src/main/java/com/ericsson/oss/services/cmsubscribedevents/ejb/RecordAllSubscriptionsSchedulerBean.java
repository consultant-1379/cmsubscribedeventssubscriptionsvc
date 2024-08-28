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
package com.ericsson.oss.services.cmsubscribedevents.ejb;

import static com.ericsson.oss.services.cmsubscribedevents.constants.InstrumentationConstants.CM_SUBSCRIBED_EVENTS_NBI_SUBSCRIPTION;
import static com.ericsson.oss.services.cmsubscribedevents.constants.InstrumentationConstants.NOTIFICATION_TYPES;
import static com.ericsson.oss.services.cmsubscribedevents.constants.InstrumentationConstants.SCOPE;
import static com.ericsson.oss.services.cmsubscribedevents.constants.InstrumentationConstants.SUBSCRIPTION_ID;

import com.ericsson.oss.services.cmsubscribedevents.api.MembershipListener;
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionService;
import com.ericsson.oss.services.cmsubscribedevents.entities.NtfSubscriptionControlWrapper;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * RecordAllSubscriptionsSchedulerBean will schedule to record all available subscriptions in elastic search log at specific time everyday.
 */
@Singleton
@Startup
public class RecordAllSubscriptionsSchedulerBean {

    @Inject
    private Logger logger;

    @EServiceRef
    private SubscriptionService subscriptionService;

    @Inject
    SystemRecorder systemRecorder;

    @Inject
    MembershipListener membershipListener;

    @Schedule(hour = "1")
    public void execute() throws IOException{
        if (membershipListener.isMaster()) {
            logger.info("Executing scheduled recording of all available subscriptions in elastic search log");
            final String allSubscriptionString = subscriptionService.viewAllSubscriptions();
            final List<NtfSubscriptionControlWrapper> ntfSubscriptionControlWrappers = new ObjectMapper()
                .readValue(allSubscriptionString, new TypeReference<ArrayList<NtfSubscriptionControlWrapper>>() {
                });
            for (final NtfSubscriptionControlWrapper ntfSubscriptionControlWrapper : ntfSubscriptionControlWrappers) {
                recordEventData(ntfSubscriptionControlWrapper);
            }
        }
    }

    private void recordEventData(final NtfSubscriptionControlWrapper ntfSubscriptionControlWrapper) {
        final HashMap<String, Object> subscriptionDataMap = new HashMap<>();
        subscriptionDataMap.put(SUBSCRIPTION_ID, ntfSubscriptionControlWrapper.getNtfSubscriptionControl().getId());
        subscriptionDataMap.put(NOTIFICATION_TYPES, ntfSubscriptionControlWrapper.getNtfSubscriptionControl().getNotificationTypes());
        subscriptionDataMap.put(SCOPE, ntfSubscriptionControlWrapper.getNtfSubscriptionControl().getScope() == null ? "" :
            ntfSubscriptionControlWrapper.getNtfSubscriptionControl().getScope().getScopeType());
        systemRecorder.recordEventData(CM_SUBSCRIBED_EVENTS_NBI_SUBSCRIPTION, subscriptionDataMap);
    }

}
