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

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import spock.lang.Specification

class HttpsConfigurationExceptionSpec extends Specification {

    @ObjectUnderTest
    HttpsConfigurationException httpsConfigurationException

    def "Retrieve error message output from HttpsConfigurationException"() {
        given: "A HttpsConfigurationException is created"
            httpsConfigurationException = new HttpsConfigurationException("Https Configuration failed")
        when: "When a calling class reads the message"
            def message = httpsConfigurationException.getMessage()
        then: "The exception message is reported"
            message == "Https Configuration failed"
    }
}