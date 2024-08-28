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

package com.ericsson.oss.services.cmsubscribedevents.instrumentation

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import spock.lang.Specification


class SubscriptionInstrumentationBeanSpec extends Specification {

    @ObjectUnderTest
    SubscriptionInstrumentationBean subscriptionInstrumentationBean

    def 'When the counters are incremented, metrics are updated with incremented value'() {
        given: 'the mbean instance is initialed'
            subscriptionInstrumentationBean = new SubscriptionInstrumentationBean()

        when: 'increment methods for metrics are called'
            for (int i = 0; i < eventCount; i++) {
                subscriptionInstrumentationBean.incrementSuccessfulPostSubscriptions()
                subscriptionInstrumentationBean.incrementSuccessfulSubscriptionDeletion()
                subscriptionInstrumentationBean.incrementSuccessfulSubscriptionViews()
                subscriptionInstrumentationBean.incrementSuccessfulViewAllSubscriptions()
                subscriptionInstrumentationBean.incrementFailedPostSubscriptions()
                subscriptionInstrumentationBean.incrementFailedSubscriptionDeletion()
                subscriptionInstrumentationBean.incrementFailedSubscriptionViews()
                subscriptionInstrumentationBean.incrementFailedViewAllSubscriptions()
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
