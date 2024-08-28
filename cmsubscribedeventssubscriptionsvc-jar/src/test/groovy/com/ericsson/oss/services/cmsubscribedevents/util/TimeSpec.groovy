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

package com.ericsson.oss.services.cmsubscribedevents.util

import spock.lang.Specification

class TimeSpec extends Specification {

    def "System Time returns date in the specified format"() {
        given: "The system requests the date in a certain format"
        when: "System time get System Time is called"
            String returnedTime = Time.getSystemTime(format)

        then: "System time is returned in the expected format"
            result == (returnedTime ==~ pattern)

        where:
            format                         | pattern                                                                       | result
            "dd-MMM-yyyy HH:mm:ss"         | /[0-9]{2}-[A-Z]{1}[a-z]{2}-[0-9]{4} (2[0-9]|[01][0-9]):[0-5][0-9]:[0-5][0-9]/ | true
            "dd-MM-yyyy HH:mm:ss"          | /[0-9]{2}-[0-9]{2}-[0-9]{4} (2[0-9]|[01][0-9]):[0-5][0-9]:[0-5][0-9]/         | true
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" | /[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9]{3}Z/       | true
            "dd-MM-yyyy HH:mm:ss"          | /[0-9]{1}-[0-9]{2}-[0-9]{4} (2[0-9]|[01][0-9]):[0-5][0-9]:[0-5][0-9]/         | false
    }

}
