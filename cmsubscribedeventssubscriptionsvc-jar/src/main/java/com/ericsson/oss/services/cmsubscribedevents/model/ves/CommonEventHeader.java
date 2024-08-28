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

package com.ericsson.oss.services.cmsubscribedevents.model.ves;

import com.ericsson.oss.services.cmsubscribedevents.model.values.CommonNotificationEventTypeValues;
import java.time.Instant;
import java.util.UUID;

/**
 * Holds the VES CommonEventHeader information.
 */
public class CommonEventHeader {

    private final String version;

    private final String vesEventListenerVersion;

    private final String domain;

    private final String stndDefinedNamespace;

    private final String eventName;

    private final String eventId;

    private final Long sequence;

    private final String priority;

    private final String reportingEntityName;

    private final String sourceName;

    private final String nfVendorName;

    private final Long startEpochMicrosec;

    private final Long lastEpochMicrosec;

    /**
     * Creates a CommonEventHeader for a VES event.
     *
     * @param commonNotificationEventTypeValues
     *     - Contains values specific to the notification type.
     */
    public CommonEventHeader(final CommonNotificationEventTypeValues commonNotificationEventTypeValues) {
        this.version = "4.1";
        this.vesEventListenerVersion = "7.2.1";
        this.domain = commonNotificationEventTypeValues.getDomain();
        this.stndDefinedNamespace = commonNotificationEventTypeValues.getStndDefinedNamespace();
        this.eventName = commonNotificationEventTypeValues.getEventName();
        this.eventId = commonNotificationEventTypeValues.getEventIdPrefix() + UUID.randomUUID();
        this.sequence = commonNotificationEventTypeValues.getSequenceNumber();
        this.priority = "High";
        this.reportingEntityName = commonNotificationEventTypeValues.getReportingEntityName();
        this.sourceName = commonNotificationEventTypeValues.getSourceName();
        this.nfVendorName = "Ericsson";
        this.startEpochMicrosec = Instant.now().toEpochMilli() * 1000;
        this.lastEpochMicrosec = Instant.now().toEpochMilli() * 1000;
    }

    public String getVersion() {
        return version;
    }

    public String getVesEventListenerVersion() {
        return vesEventListenerVersion;
    }

    public String getDomain() {
        return domain;
    }

    public String getStndDefinedNamespace() {
        return stndDefinedNamespace;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventId() {
        return eventId;
    }

    public Long getSequence() {
        return sequence;
    }

    public String getPriority() {
        return priority;
    }

    public String getReportingEntityName() {
        return reportingEntityName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getNfVendorName() {
        return nfVendorName;
    }

    public Long getStartEpochMicrosec() {
        return startEpochMicrosec;
    }

    public Long getLastEpochMicrosec() {
        return lastEpochMicrosec;
    }

}