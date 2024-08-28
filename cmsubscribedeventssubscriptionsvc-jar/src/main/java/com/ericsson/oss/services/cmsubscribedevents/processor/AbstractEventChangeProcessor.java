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

package com.ericsson.oss.services.cmsubscribedevents.processor;

import java.util.List;
import com.ericsson.oss.services.cmsubscribedevents.api.SubscribedChangeEventsProcessor;
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType;
import com.ericsson.oss.services.cmsubscribedevents.filter.SubscriptionFilter;
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription;
import com.ericsson.oss.services.cmsubscribedevents.nbi.EventNotificationProducer;
import javax.ejb.Singleton;
/**
 * Generic processor for handling different types of events.
 *
 * @param <T> The type of event to process.
 */
@Singleton
public abstract class AbstractEventChangeProcessor<T> implements SubscribedChangeEventsProcessor<T> {

    @Override
    public void processChangeEvent(T changeEvent, List<Subscription> subscriptions) {
        if (!subscriptions.isEmpty()) {
            OperationType eventOperationType = extractEventOperationType(changeEvent);
            String eventHref = extractEventHref(changeEvent);
            if (eventOperationType != null) {
                List<Subscription> filteredSubscriptions = SubscriptionFilter.filter(subscriptions, eventOperationType, eventHref);
                if (!filteredSubscriptions.isEmpty()) {
                    getNotificationProducer().publishEventToQueue(filteredSubscriptions, eventOperationType, changeEvent);
                }
            }
        }
    }

    protected abstract OperationType extractEventOperationType(T event);

    protected abstract String extractEventHref(T event);

    protected abstract EventNotificationProducer<T> getNotificationProducer();
}




