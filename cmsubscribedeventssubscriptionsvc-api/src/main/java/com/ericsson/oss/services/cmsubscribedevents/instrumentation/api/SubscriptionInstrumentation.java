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
package com.ericsson.oss.services.cmsubscribedevents.instrumentation.api;

import javax.ejb.Remote;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;

@EService
@Remote
public interface SubscriptionInstrumentation {

    public void incrementSuccessfulPostSubscriptions();
    public void incrementFailedPostSubscriptions();
    public void incrementSuccessfulSubscriptionViews();
    public void incrementFailedSubscriptionViews();
    public void incrementSuccessfulSubscriptionDeletion();
    public void incrementFailedSubscriptionDeletion();
    public void incrementSuccessfulViewAllSubscriptions();
    public void incrementFailedViewAllSubscriptions();
}
