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

import static com.ericsson.oss.services.cmsubscribedevents.api.constants.ExceptionMessageConstants.PUSH_VES_GENERAL_ERROR

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import spock.lang.Specification

class PushNotificationExceptionSpec extends Specification {

    @ObjectUnderTest
    PushNotificationException pushNotificationException

    def "Retrieve error message output from PushNotificationException"() {
        given: "A PushNotificationException is created"
            pushNotificationException = new PushNotificationException(PUSH_VES_GENERAL_ERROR)
        when: "When a calling class reads the message"
            def message = pushNotificationException.getMessage()
        then: "The exception message is reported"
            message == PUSH_VES_GENERAL_ERROR
    }
}