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

import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsDataChangedEvent;
import com.ericsson.oss.mediation.network.api.notifications.NodeNotification;
import com.ericsson.oss.mediation.notifications.ComEcimNodeNotification;
import com.ericsson.oss.services.cmsubscribedevents.api.SubscribedEventsProcessor;
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription;
import com.ericsson.oss.services.cmsubscribedevents.processor.ComEcimEventChangeProcessor;
import com.ericsson.oss.services.cmsubscribedevents.processor.CppEventChangeProcessor;
import com.ericsson.oss.services.cmsubscribedevents.processor.DpsEventChangeProcessor;

import java.util.Collections;
import java.util.List;

import javax.ejb.Singleton;
import javax.inject.Inject;

import java.util.stream.Collectors;

@Singleton
public class SubscribedEventsProcessorEjb implements SubscribedEventsProcessor {

    @Inject
    ComEcimEventChangeProcessor comEcimEventChangeProcessor;

    @Inject
    CppEventChangeProcessor cppEventChangeProcessor;

    @Inject
    DpsEventChangeProcessor dpsEventChangeProcessor;

    @Inject
    SubscriptionListUpdater subscriptionListUpdater;

    List<Subscription> allSubscriptions = Collections.emptyList();

    @Override
    public void processEvent(Object event) {
        allSubscriptions = subscriptionListUpdater.getUpdatedSubscriptionList();
        if (event instanceof NodeNotification) {
            cppEventChangeProcessor.processChangeEvent((NodeNotification)event, allSubscriptions.stream().collect(Collectors.toList()));
        } else if (event instanceof ComEcimNodeNotification) {
            comEcimEventChangeProcessor.processChangeEvent((ComEcimNodeNotification)event, allSubscriptions.stream().collect(Collectors.toList()));
        } else if (event instanceof DpsDataChangedEvent) {
            dpsEventChangeProcessor.processChangeEvent((DpsDataChangedEvent)event, allSubscriptions.stream().collect(Collectors.toList()));
        }
    }

}
