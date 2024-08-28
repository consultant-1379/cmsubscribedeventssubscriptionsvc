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
package com.ericsson.oss.services.cmsubscribedevents.ejb

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.SubscriptionInstrumentationBean

class SubscriptionInstrumentationFacadeSpec extends CdiSpecification {

    @ObjectUnderTest
    SubscriptionInstrumentationFacade subscriptionInstrumentationFacade

    @Inject
    SubscriptionInstrumentationBean subscriptionInstrumentationBean

    def 'When the counters are incremented, metrics are updated with incremented value'() {

        when: 'increment methods for metrics are called'
            for (int i = 0; i < eventCount; i++) {
                subscriptionInstrumentationFacade.incrementSuccessfulPostSubscriptions()
                subscriptionInstrumentationFacade.incrementSuccessfulSubscriptionDeletion()
                subscriptionInstrumentationFacade.incrementSuccessfulSubscriptionViews()
                subscriptionInstrumentationFacade.incrementSuccessfulViewAllSubscriptions()
                subscriptionInstrumentationFacade.incrementFailedPostSubscriptions()
                subscriptionInstrumentationFacade.incrementFailedSubscriptionDeletion()
                subscriptionInstrumentationFacade.incrementFailedSubscriptionViews()
                subscriptionInstrumentationFacade.incrementFailedViewAllSubscriptions()
            }

        then: 'the metrics are updated with incremented values'
            subscriptionInstrumentationBean.getSuccessfulPostSubscriptions() == result
            subscriptionInstrumentationBean.getSuccessfulSubscriptionDeletion() == result
            subscriptionInstrumentationBean.getSuccessfulSubscriptionViews() == result
            subscriptionInstrumentationBean.getSuccessfulViewAllSubscriptions() == result
            subscriptionInstrumentationBean.getFailedPostSubscriptions() == result
            subscriptionInstrumentationBean.getFailedSubscriptionDeletion() == result
            subscriptionInstrumentationBean.getFailedSubscriptionViews() == result
            subscriptionInstrumentationBean.getFailedViewAllSubscriptions() == result

        where:
            eventCount | result
            1          | 1
            2          | 2
            3          | 3
    }
}