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

package com.ericsson.oss.services.cmsubscribedevents.model.subscription;

public class Subscription {

    private NtfSubscriptionControl ntfSubscriptionControl;

    public Subscription() {}

    public Subscription(final NtfSubscriptionControl ntfSubscriptionControl) {
        this.ntfSubscriptionControl = ntfSubscriptionControl;
    }

    public NtfSubscriptionControl getNtfSubscriptionControl() {
        return ntfSubscriptionControl;
    }

    public void setNtfSubscriptionControl(final NtfSubscriptionControl ntfSubscriptionControl) {
        this.ntfSubscriptionControl = ntfSubscriptionControl;
    }

}