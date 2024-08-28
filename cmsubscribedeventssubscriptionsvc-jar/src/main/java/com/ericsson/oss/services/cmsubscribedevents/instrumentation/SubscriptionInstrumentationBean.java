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

package com.ericsson.oss.services.cmsubscribedevents.instrumentation;

import static com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute.CollectionType.TRENDSUP;
import static com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute.Visibility.ALL;

import com.ericsson.oss.itpf.sdk.instrument.annotation.InstrumentedBean;
import com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute;
import javax.enterprise.context.ApplicationScoped;

/**
 * Class is used to gather Subscriptions related metrics of CM Subscribed Events NBI for uploading to DDC site.
 */
@InstrumentedBean(description = "Collects Subscriptions related metrics to upload to the DDC site for CM Subscribed events NBI",
    displayName = "Subscriptions related metrics of CM Subscribed Events NBI")
@ApplicationScoped
public class SubscriptionInstrumentationBean {
    private long successfulPostSubscriptions;
    private long failedPostSubscriptions;
    private long successfulSubscriptionViews;
    private long failedSubscriptionViews;
    private long successfulSubscriptionDeletion;
    private long failedSubscriptionDeletion;
    private long successfulViewAllSubscriptions;
    private long failedViewAllSubscriptions;

    @MonitoredAttribute(displayName = "Number of Successful Post Subscriptions", visibility = ALL, collectionType = TRENDSUP)
    public long getSuccessfulPostSubscriptions() {
        return successfulPostSubscriptions;
    }

    public void incrementSuccessfulPostSubscriptions() {
        successfulPostSubscriptions++;
    }

    @MonitoredAttribute(displayName = "Number of Failed Post Subscriptions", visibility = ALL, collectionType = TRENDSUP)
    public long getFailedPostSubscriptions() {
        return failedPostSubscriptions;
    }

    public void incrementFailedPostSubscriptions() {
        failedPostSubscriptions++;
    }

    @MonitoredAttribute(displayName = "Number of Successful Subscription Views", visibility = ALL, collectionType = TRENDSUP)
    public long getSuccessfulSubscriptionViews() {
        return successfulSubscriptionViews;
    }

    public void incrementSuccessfulSubscriptionViews() {
        successfulSubscriptionViews++;
    }

    @MonitoredAttribute(displayName = "Number of Failed Subscription Views", visibility = ALL, collectionType = TRENDSUP)
    public long getFailedSubscriptionViews() {
        return failedSubscriptionViews;
    }

    public void incrementFailedSubscriptionViews() {
        failedSubscriptionViews++;
    }

    @MonitoredAttribute(displayName = "Number of Successful Subscription Deletion", visibility = ALL, collectionType = TRENDSUP)
    public long getSuccessfulSubscriptionDeletion() {
        return successfulSubscriptionDeletion;
    }

    public void incrementSuccessfulSubscriptionDeletion() {
        successfulSubscriptionDeletion++;
    }

    @MonitoredAttribute(displayName = "Number of Failed Subscription Deletion", visibility = ALL, collectionType = TRENDSUP)
    public long getFailedSubscriptionDeletion() {
        return failedSubscriptionDeletion;
    }

    public void incrementFailedSubscriptionDeletion() {
        failedSubscriptionDeletion++;
    }

    @MonitoredAttribute(displayName = "Number of Successful View All Subscriptions", visibility = ALL, collectionType = TRENDSUP)
    public long getSuccessfulViewAllSubscriptions() {
        return successfulViewAllSubscriptions;
    }

    public void incrementSuccessfulViewAllSubscriptions() {
        successfulViewAllSubscriptions++;
    }

    @MonitoredAttribute(displayName = "Number of Failed  View All Subscriptions", visibility = ALL, collectionType = TRENDSUP)
    public long getFailedViewAllSubscriptions() {
        return failedViewAllSubscriptions;
    }

    public void incrementFailedViewAllSubscriptions() {
        failedViewAllSubscriptions++;
    }
}
