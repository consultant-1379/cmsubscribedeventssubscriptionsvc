/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2023
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 *
 */
package com.ericsson.oss.services.cmsubscribedevents.builder;

import static com.ericsson.oss.services.cmsubscribedevents.constants.SubscriptionConstants.VES_EVENT_TIMESTAMP_FORMAT;

import com.ericsson.oss.itpf.datalayer.dps.notification.event.AttributeChangeData;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsAttributeChangedEvent;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsDataChangedEvent;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectCreatedEvent;
import com.ericsson.oss.mediation.network.api.notifications.NodeNotification;
import com.ericsson.oss.mediation.notifications.ComEcimNodeNotification;
import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.PushNotificationException;
import com.ericsson.oss.services.cmsubscribedevents.constants.SubscriptionConstants;
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType;
import com.ericsson.oss.services.cmsubscribedevents.model.values.CommonNotificationEventTypeValues;
import com.ericsson.oss.services.cmsubscribedevents.model.ves.CommonEventHeader;
import com.ericsson.oss.services.cmsubscribedevents.model.ves.Event;
import com.ericsson.oss.services.cmsubscribedevents.model.ves.EventWrapper;
import com.ericsson.oss.services.cmsubscribedevents.model.ves.StndDefinedFields;
import com.ericsson.oss.services.cmsubscribedevents.util.DataConverter;
import com.ericsson.oss.services.cmsubscribedevents.util.Time;
import com.ericsson.oss.services.cmsubscribedevents.util.TimeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;

/**
 * Create Data Change Ves Events as Strings.
 * Creates Heartbeat Ves Events in an EventWrapper
 */
public class VesEventBuilder {

    private static final Long DEFAULT_NOTIFICATION_SEQUENCE_NUMBER = 0L;
    private static final String NOTIFICATION_EVENT_IS_NULL = "Notification event is null";
    private static final String NOTIFICATION_EVENT_DOESN_T_CONTAIN_A_TIMESTAMP = "Notification event doesn't contain a timestamp";
    private static final String NOTIFICATION_EVENT_DOESN_T_CONTAIN_A_DN_FIELD = "Notification event doesn't contain a DN field - {}";
    private static final String NOTIFICATION_SEQUENCE_NUMBER_IS_EMPTY = "Notification sequence number is empty - {}";
    private static final String UNSUPPORTED_NOTIFICATION_TYPE_FOR_BUILDING_THE_COMMON_NOTIFY_MOI_DATA = "Unsupported notification type for building the CommonNotifyMoiData";

    private static final String VES_EVENT_NAME_PROVISIONING_PREFIX = "Provisioning_";
    private static final String ATTRIBUTE_LIST_VALUE_CHANGES = "\"attributeListValueChanges\":%%value_changes%%";
    private static final String ATTRIBUTE_LIST = "\"attributeList\":%%value_changes%%";

    private static Logger logger = LoggerFactory.getLogger(VesEventBuilder.class);

    private static List<String> notificationTypes = new ArrayList<>();
    static {
        notificationTypes.add(OperationType.ALL_CHANGES.getNotificationType());
        notificationTypes.add(OperationType.CREATE.getNotificationType());
        notificationTypes.add(OperationType.DELETE.getNotificationType());
        notificationTypes.add(OperationType.REPLACE.getNotificationType());
    }

    @Inject
    private TimeConverter timeConverter;

    public String buildDpsVesEventString(final OperationType operationType, final String subscriptionNotificationType,
            final DpsDataChangedEvent event) {
        if (event == null) {
            logger.error(NOTIFICATION_EVENT_IS_NULL);
            return null;
        }
        Map<String, Object> updatedAttributes = new HashMap<>();
        switch (operationType) {
            case CREATE:
                final DpsObjectCreatedEvent dpsObjectCreatedEvent = (DpsObjectCreatedEvent) event;
                updatedAttributes = dpsObjectCreatedEvent.getAttributeValues();
                break;
            case DELETE:
                break;
            case REPLACE:
                final DpsAttributeChangedEvent dpsAttributeChangedEvent = (DpsAttributeChangedEvent) event;
                final Set<AttributeChangeData> attributeChangeDataSet = dpsAttributeChangedEvent.getChangedAttributes();
                for (final AttributeChangeData attributeChangeData : attributeChangeDataSet) {
                    final String attributeName = attributeChangeData.getName();
                    final Object updatedValue = attributeChangeData.getNewValue();
                    updatedAttributes.put(attributeName, updatedValue);
                }
                break;
            default:
                logger.error("Invalid Operation Type");
                return null;
        }

        return buildVesEventString(DEFAULT_NOTIFICATION_SEQUENCE_NUMBER, event.getFdn(), operationType, subscriptionNotificationType, Time.getSystemTime(VES_EVENT_TIMESTAMP_FORMAT),
                updatedAttributes);
    }

    public String buildCppVesEventString(final OperationType operationType, final String subscriptionNotificationType,
            final NodeNotification event) {

        if (event == null) {
            logger.error(NOTIFICATION_EVENT_IS_NULL);
            return null;
        }
        Map<String, Object> updatedAttributes = new HashMap<>();
        if ( operationType.equals(OperationType.CREATE) || operationType.equals(OperationType.REPLACE)) {
            updatedAttributes = event.getUpdateAttributes();
        }

        return buildVesEventString(DEFAULT_NOTIFICATION_SEQUENCE_NUMBER, event.getFdn(), operationType, subscriptionNotificationType, String.valueOf(event.getCreationTimestamp()),
                updatedAttributes);
    }

    public String buildComEcimVesEventString(final OperationType operationType, final String subscriptionNotificationType,
            final ComEcimNodeNotification event) {
        if (event == null) {
            logger.error(NOTIFICATION_EVENT_IS_NULL);
            return null;
        }
        Long notificationSequenceNumber = event.getNotifSqNr();
        if ( null == notificationSequenceNumber) {
            logger.debug(NOTIFICATION_SEQUENCE_NUMBER_IS_EMPTY, event);
            notificationSequenceNumber = 0L;
        }

        Map<String, Object> updatedAttributes = new HashMap<>();
        if ( operationType.equals(OperationType.CREATE) || operationType.equals(OperationType.REPLACE)) {
            updatedAttributes = event.getUpdateAttributes();
        }

        return buildVesEventString(notificationSequenceNumber, event.getDn(), operationType, subscriptionNotificationType, event.getTimestamp(),
                updatedAttributes);
    }

    private String buildVesEventString(final long sequenceNumber, final String fdn, final OperationType operationType,
            final String subscriptionNotificationType, final String timeStamp, final Map<String, Object> updatedAttributes) {
        String eventId = VES_EVENT_NAME_PROVISIONING_PREFIX + UUID.randomUUID();

        if (timeStamp == null || timeStamp.equals("null") || timeStamp
                .isEmpty()) {
                logger.error(NOTIFICATION_EVENT_DOESN_T_CONTAIN_A_TIMESTAMP);
                return null;
        } else if (fdn == null || fdn.isEmpty()) {
                logger.error(NOTIFICATION_EVENT_DOESN_T_CONTAIN_A_DN_FIELD);
                return null;
        } else if (!notificationTypes.contains(subscriptionNotificationType)) {
            logger.error(UNSUPPORTED_NOTIFICATION_TYPE_FOR_BUILDING_THE_COMMON_NOTIFY_MOI_DATA);
            return null;
        }
        final StringBuilder commonEventHeader = new StringBuilder().append("{\"version\":\"4.1\",\"vesEventListenerVersion\":\"7.2.1\",\"domain\":\"stndDefined\",\"stndDefinedNamespace\":\"3GPP-Provisioning\",\"eventName\":\"")
                .append(VES_EVENT_NAME_PROVISIONING_PREFIX)
                .append("ENM-Ericsson_VES\",\"eventId\":\"")
                .append(eventId)
                .append("\",\"sequence\":")
                .append("" + sequenceNumber)
                .append(",\"priority\":\"High\",\"reportingEntityName\":\"")
                .append(DataConverter.retrieveTargetName(fdn))
                .append("\",\"sourceName\":\"")
                .append(System.getProperty(SubscriptionConstants.PRESENTATION_SERVER_NAME))
                .append("\",\"nfVendorName\":\"Ericsson\",\"startEpochMicrosec\":")
                .append("" + (Instant.now().toEpochMilli() * 1000))
                .append(",\"lastEpochMicrosec\":")
                .append("" + (Instant.now().toEpochMilli() * 1000))
                .append("}");

        String eventSpecificData = "";

        String attributeValues = getUpdatedAttributeValues(updatedAttributes);

        if (subscriptionNotificationType.equals("notifyMOIChanges")) {

            final StringBuilder moiChange = new StringBuilder().append("\"moiChanges\":[{\"notificationId\":")
                    .append("" + (UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE))
                    .append(",\"path\":\"")
                    .append(fdn)
                    .append("\",\"operation\":\"")
                    .append(operationType.toString())
                    .append("\",\"value\":")
                    .append(attributeValues)
                    .append("}]");
            eventSpecificData = moiChange.toString();
        } else {
            if (subscriptionNotificationType.equals("notifyMOIAttributeValueChanges")) {
                eventSpecificData = ATTRIBUTE_LIST_VALUE_CHANGES.replace("%%value_changes%%", attributeValues);
            } else {
                eventSpecificData = ATTRIBUTE_LIST.replace("%%value_changes%%", attributeValues);
            }
        }

        final StringBuilder data = new StringBuilder().append("\"href\":\"")
                .append(fdn)
                .append("\",\"notificationId\":")
                .append("" + (UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE))
                .append(",\"notificationType\":\"")
                .append(subscriptionNotificationType)
                .append("\",\"eventTime\":\"")
                .append(timeConverter.covertToUTC(timeStamp))
                .append("\",\"systemDN\":\"")
                .append("" + System.getProperty(SubscriptionConstants.PRESENTATION_SERVER_NAME))
                .append("\",")
                .append(eventSpecificData);

        final StringBuilder event = new StringBuilder().append("{\"event\":{\"commonEventHeader\":")
                .append(commonEventHeader)
                .append(",\"stndDefinedFields\":{\"stndDefinedFieldsVersion\":\"1.0\",\"data\":{")
                .append(data)
                .append("}}}}");
        return event.toString();
    }

    private String getUpdatedAttributeValues(final Map<String, Object> updatedAttributes) {
        String attributeValues = "[]";
        if (!updatedAttributes.isEmpty()) {
            final List<Map<String, Object>> listOfUpdateAttributes = new ArrayList<>();
            listOfUpdateAttributes.add(updatedAttributes);
            try {
                attributeValues = new ObjectMapper().writeValueAsString(listOfUpdateAttributes);
            } catch (final IOException ioe) {
                final String errorMessage = String.format("Error while adding VES event to internal queue. %s", ioe.getMessage());
                logger.error(errorMessage);
                throw new PushNotificationException(errorMessage);
            }
        }
        return attributeValues;
    }

    /**
     * Create an {@link Event} in an {@link EventWrapper} based on inputted values.
     *
     * @param eventTypeValues
     *     - Contains values for the type of Notification object that is being created.
     * @return {@link EventWrapper} containing {@link Event}.
     */
    public EventWrapper createEvent(final CommonNotificationEventTypeValues eventTypeValues) {
        final CommonEventHeader commonEventHeader = new CommonEventHeader(eventTypeValues);
        final StndDefinedFields stndDefinedFields = new StndDefinedFields(eventTypeValues);
        final Event event = new Event(commonEventHeader, stndDefinedFields);
        return new EventWrapper(event);
    }

}