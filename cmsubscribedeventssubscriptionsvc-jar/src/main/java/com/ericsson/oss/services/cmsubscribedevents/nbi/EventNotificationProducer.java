/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 *  COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cmsubscribedevents.nbi;

import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean;
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType;
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import com.ericsson.oss.services.cmsubscribedevents.nbi.queue.EventQueue;
import javax.inject.Inject;

/**
 * Send notifications via HTTP.
 */
public abstract class EventNotificationProducer<T> {

    @Inject
    EventsInstrumentationBean eventsInstrumentationBean;

    abstract String buildNotificationTypeModel(OperationType operationType, String subscriptionNotificationType, T event);

    Logger logger = LoggerFactory.getLogger(EventNotificationProducer.class);
    /**
     * Builds VES events based on the eventNotificationType and adds to the EventQueue for each Subscription in filteredSubscriptions
     *  respective recipient address
     * @param filteredSubscriptions
     *     - Subscriptions filtered down by incoming event's Notification Type
     * @param eventNotificationType
     *     - Notification Type of the event incoming
     * @param event
     *     - Incoming event off the CmDataChangeDivertedQueue
     */
    public void publishEventToQueue(final List<Subscription> filteredSubscriptions, final OperationType eventNotificationType, final T event) {

        String eventNotificationTypeVesStringEntity = null;
        String moiChangesVesStringEntity = null;

        for (final Subscription subscription : filteredSubscriptions) {
            String [] subscriptionNotificationTypes = subscription.getNtfSubscriptionControl().getNotificationTypes();

            if (subscriptionNotificationTypes == null || Arrays.asList(subscriptionNotificationTypes).contains(OperationType.ALL_CHANGES.getNotificationType())) {
                updateMetricsForNotifyMoiChanges(eventNotificationType);
                moiChangesVesStringEntity = buildVesStringEntity(moiChangesVesStringEntity, eventNotificationType, OperationType.ALL_CHANGES.getNotificationType(), event);
                EventQueue.getInstance().add(subscription.getNtfSubscriptionControl().getNotificationRecipientAddress(), moiChangesVesStringEntity);
            } else {
                updateMetricsForOtherThanNotifyMoiChanges(eventNotificationType);
                eventNotificationTypeVesStringEntity = buildVesStringEntity(eventNotificationTypeVesStringEntity, eventNotificationType, eventNotificationType.getNotificationType(), event);
                EventQueue.getInstance().add(subscription.getNtfSubscriptionControl().getNotificationRecipientAddress(), eventNotificationTypeVesStringEntity);
            }
        }
    }

    private String buildVesStringEntity(String currentVesStringEntity, final OperationType operationType, final String subscriptionNotificationType, final T event) {
        if (currentVesStringEntity == null) {
            currentVesStringEntity = buildNotificationTypeModel(operationType, subscriptionNotificationType, event);
        }
        return currentVesStringEntity;
    }

    private void updateMetricsForNotifyMoiChanges(final OperationType eventNotificationType) {
        if (eventNotificationType == OperationType.CREATE) {
            eventsInstrumentationBean.incrementVesEventsToBePushedNotifyMoiChangesCreate();
        } else if (eventNotificationType == OperationType.DELETE) {
            eventsInstrumentationBean.incrementVesEventsToBePushedNotifyMoiChangesDelete();
        } else if (eventNotificationType == OperationType.REPLACE) {
            eventsInstrumentationBean.incrementVesEventsToBePushedNotifyMoiChangesReplace();
        }
    }

    private void updateMetricsForOtherThanNotifyMoiChanges(final OperationType eventNotificationType) {
        if (eventNotificationType == OperationType.CREATE) {
            eventsInstrumentationBean.incrementVesEventsToBePushedNotifyMoiCreation();
        } else if (eventNotificationType == OperationType.DELETE) {
            eventsInstrumentationBean.incrementVesEventsToBePushedNotifyMoiDeletion();
        } else if (eventNotificationType == OperationType.REPLACE) {
            eventsInstrumentationBean.incrementVesEventsToBePushedNotifyMoiAvc();
        }
    }
}
