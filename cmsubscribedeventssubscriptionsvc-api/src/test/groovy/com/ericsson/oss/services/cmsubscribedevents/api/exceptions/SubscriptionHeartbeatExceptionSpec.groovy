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

package com.ericsson.oss.services.cmsubscribedevents.api.exceptions

import static com.ericsson.oss.services.cmsubscribedevents.api.constants.ExceptionMessageConstants.HEARTBEAT_GENERAL_ERROR

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import spock.lang.Specification

class SubscriptionHeartbeatExceptionSpec extends Specification {


    @ObjectUnderTest
    SubscriptionHeartbeatException subscriptionHeartbeatException

    def "Retrieve error message output from SubscriptionHeartbeatException"() {
        given: "A SubscriptionHeartbeatException is created"
            subscriptionHeartbeatException = new SubscriptionHeartbeatException(HEARTBEAT_GENERAL_ERROR)
        when: "When a calling class reads the message"
            def message = subscriptionHeartbeatException.getMessage()
        then: "The exception message is reported"
            message == HEARTBEAT_GENERAL_ERROR
    }
}
