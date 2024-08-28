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

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.services.cmsubscribedevents.instrumentation.SubscriptionInstrumentationBean;
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.api.SubscriptionInstrumentation;

@Stateless
public class SubscriptionInstrumentationFacade implements SubscriptionInstrumentation {

    @Inject
    SubscriptionInstrumentationBean subscriptionInstrumentationBean;

    @Override
    public void incrementSuccessfulPostSubscriptions() {
        subscriptionInstrumentationBean.incrementSuccessfulPostSubscriptions();
    }

    @Override
    public void incrementFailedPostSubscriptions() {
        subscriptionInstrumentationBean.incrementFailedPostSubscriptions();
    }

    @Override
    public void incrementSuccessfulSubscriptionViews() {
        subscriptionInstrumentationBean.incrementSuccessfulSubscriptionViews();
    }

    @Override
    public void incrementFailedSubscriptionViews() {
        subscriptionInstrumentationBean.incrementFailedSubscriptionViews();
    }

    @Override
    public void incrementSuccessfulSubscriptionDeletion() {
        subscriptionInstrumentationBean.incrementSuccessfulSubscriptionDeletion();
    }

    @Override
    public void incrementFailedSubscriptionDeletion() {
        subscriptionInstrumentationBean.incrementFailedSubscriptionDeletion();
    }

    @Override
    public void incrementSuccessfulViewAllSubscriptions() {
        subscriptionInstrumentationBean.incrementSuccessfulViewAllSubscriptions();
    }

    @Override
    public void incrementFailedViewAllSubscriptions() {
        subscriptionInstrumentationBean.incrementFailedViewAllSubscriptions();
    }
}