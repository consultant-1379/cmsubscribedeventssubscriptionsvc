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

package com.ericsson.oss.services.cmsubscribedevents.instrumentation;

import static com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute.CollectionType.TRENDSUP;
import static com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute.Visibility.ALL;

import com.ericsson.oss.itpf.sdk.instrument.annotation.InstrumentedBean;
import com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class is used to gather events related metrics of CM Subscribed Events NBI for uploading to DDC site.
 */
@InstrumentedBean(description = "Collects events related metrics to upload to the DDC site for CM Subscribed events NBI",
    displayName = "Events related metrics of CM Subscribed Events NBI")
@ApplicationScoped
public class EventsInstrumentationBean {
    private final AtomicLong createEventsReceived = new AtomicLong(0);
    private final AtomicLong updateEventsReceived = new AtomicLong(0);
    private final AtomicLong deleteEventsReceived = new AtomicLong(0);
    private final AtomicLong totalEventsReceived = new AtomicLong(0);

    private final AtomicLong vesEventsToBePushedNotifyMoiCreation = new AtomicLong(0);
    private final AtomicLong vesEventsToBePushedNotifyMoiChangesCreate = new AtomicLong(0);
    private final AtomicLong vesEventsToBePushedNotifyMoiAvc = new AtomicLong(0);
    private final AtomicLong vesEventsToBePushedNotifyMoiChangesReplace = new AtomicLong(0);
    private final AtomicLong vesEventsToBePushedNotifyMoiDeletion = new AtomicLong(0);
    private final AtomicLong vesEventsToBePushedNotifyMoiChangesDelete = new AtomicLong(0);
    private final AtomicLong totalVesEventsToBePushed = new AtomicLong(0);

    private final AtomicLong totalVesEventsPushedSuccessfully = new AtomicLong(0);
    private final AtomicLong totalVesEventsPushedError = new AtomicLong(0);
    private final AtomicLong totalVesEventsPushedCancelled = new AtomicLong(0);

    private final AtomicLong totalSuccessfulHeartbeatRequests = new AtomicLong(0);
    private final AtomicLong totalFailedHeartbeatRequests = new AtomicLong(0);

    private final AtomicLong successfulContinuousHeartbeatRequests = new AtomicLong(0);
    private final AtomicLong failedContinuousHeartbeatRequests = new AtomicLong(0);

    @MonitoredAttribute(displayName = "Number of Create Events Received", visibility = ALL, collectionType = TRENDSUP)
    public long getCreateEventsReceived() {
        return createEventsReceived.get();
    }

    public void incrementCreateEventsReceived() {
        totalEventsReceived.incrementAndGet();
        createEventsReceived.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of Update Events Received", visibility = ALL, collectionType = TRENDSUP)
    public long getUpdateEventsReceived() {
        return updateEventsReceived.get();
    }

    public void incrementUpdateEventsReceived() {
        totalEventsReceived.incrementAndGet();
        updateEventsReceived.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of Delete Events Received", visibility = ALL, collectionType = TRENDSUP)
    public long getDeleteEventsReceived() {
        return deleteEventsReceived.get();
    }

    public synchronized void incrementDeleteEventsReceived() {
        totalEventsReceived.incrementAndGet();
        deleteEventsReceived.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Total Events Received", visibility = ALL, collectionType = TRENDSUP)
    public long getTotalEventsReceived() {
        return totalEventsReceived.get();
    }

    @MonitoredAttribute(displayName = "Number of VES Events to be pushed of type NotifyMOICreation", visibility = ALL, collectionType = TRENDSUP)
    public long getVesEventsToBePushedNotifyMoiCreation() {
        return vesEventsToBePushedNotifyMoiCreation.get();
    }

    public void incrementVesEventsToBePushedNotifyMoiCreation() {
        totalVesEventsToBePushed.incrementAndGet();
        vesEventsToBePushedNotifyMoiCreation.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of VES Events to be pushed of type NotifyMOIChanges - CREATE", visibility = ALL, collectionType = TRENDSUP)
    public long getVesEventsToBePushedNotifyMoiChangesCreate() {
        return vesEventsToBePushedNotifyMoiChangesCreate.get();
    }

    public void incrementVesEventsToBePushedNotifyMoiChangesCreate() {
        totalVesEventsToBePushed.incrementAndGet();
        vesEventsToBePushedNotifyMoiChangesCreate.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of VES Events to be pushed of type NotifyMOIAttributeValueChanges", visibility = ALL, collectionType = TRENDSUP)
    public long getVesEventsToBePushedNotifyMoiAvc() {
        return vesEventsToBePushedNotifyMoiAvc.get();
    }

    public void incrementVesEventsToBePushedNotifyMoiAvc() {
        totalVesEventsToBePushed.incrementAndGet();
        vesEventsToBePushedNotifyMoiAvc.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of VES Events to be pushed of type NotifyMOIChanges - REPLACE", visibility = ALL, collectionType = TRENDSUP)
    public long getVesEventsToBePushedNotifyMoiChangesReplace() {
        return vesEventsToBePushedNotifyMoiChangesReplace.get();
    }

    public void incrementVesEventsToBePushedNotifyMoiChangesReplace() {
        totalVesEventsToBePushed.incrementAndGet();
        vesEventsToBePushedNotifyMoiChangesReplace.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of VES Events to be pushed of type NotifyMOIAttributeValueChanges", visibility = ALL, collectionType = TRENDSUP)
    public long getVesEventsToBePushedNotifyMoiDeletion() {
        return vesEventsToBePushedNotifyMoiDeletion.get();
    }

    public void incrementVesEventsToBePushedNotifyMoiDeletion() {
        totalVesEventsToBePushed.incrementAndGet();
        vesEventsToBePushedNotifyMoiDeletion.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of VES Events to be pushed of type NotifyMOIChanges - DELETE", visibility = ALL, collectionType = TRENDSUP)
    public long getVesEventsToBePushedNotifyMoiChangesDelete() {
        return vesEventsToBePushedNotifyMoiChangesDelete.get();
    }

    public void incrementVesEventsToBePushedNotifyMoiChangesDelete() {
        totalVesEventsToBePushed.incrementAndGet();
        vesEventsToBePushedNotifyMoiChangesDelete.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Total VES Events to be pushed", visibility = ALL, collectionType = TRENDSUP)
    public long getTotalVesEventsToBePushed() {
        return totalVesEventsToBePushed.get();
    }

    @MonitoredAttribute(displayName = "Number of VES Events  pushed successfully", visibility = ALL, collectionType = TRENDSUP)
    public long getTotalVesEventsPushedSuccessfully() {
        return totalVesEventsPushedSuccessfully.get();
    }

    public void incrementTotalVesEventsPushedSuccessfully() {
        totalVesEventsPushedSuccessfully.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of VES Events  pushed and failed with error", visibility = ALL, collectionType = TRENDSUP)
    public long getTotalVesEventsPushedError() {
        return totalVesEventsPushedError.get();
    }

    public void incrementTotalVesEventsPushedError() {
        totalVesEventsPushedError.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of VES Events  pushed and cancelled", visibility = ALL, collectionType = TRENDSUP)
    public long getTotalVesEventsPushedCancelled() {
        return totalVesEventsPushedCancelled.get();
    }

    public void incrementTotalVesEventsPushedCancelled() {
        totalVesEventsPushedCancelled.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of Successful Heartbeat Requests", visibility = ALL, collectionType = TRENDSUP)
    public long getTotalSuccessfulHeartbeatRequests() {
        return totalSuccessfulHeartbeatRequests.get();
    }

    public void incrementTotalSuccessfulHeartbeatRequests() {
        totalSuccessfulHeartbeatRequests.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of failed Heartbeat Requests", visibility = ALL, collectionType = TRENDSUP)
    public long getTotalFailedHeartbeatRequests() {
        return totalFailedHeartbeatRequests.get();
    }

    public void incrementTotalFailedHeartbeatRequests() {
        totalFailedHeartbeatRequests.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of Successful Continuous Heartbeat Requests", visibility = ALL, collectionType = TRENDSUP)
    public long getSuccessfulContinuousHeartbeatRequests() {
        return successfulContinuousHeartbeatRequests.get();
    }

    public void incrementSuccessfulContinuousHeartbeatRequests() {
        successfulContinuousHeartbeatRequests.incrementAndGet();
    }

    @MonitoredAttribute(displayName = "Number of failed Continuous Heartbeat Requests", visibility = ALL, collectionType = TRENDSUP)
    public long getFailedContinuousHeartbeatRequests() {
        return failedContinuousHeartbeatRequests.get();
    }

    public void incrementFailedContinuousHeartbeatRequests() {
        failedContinuousHeartbeatRequests.incrementAndGet();
    }

}
