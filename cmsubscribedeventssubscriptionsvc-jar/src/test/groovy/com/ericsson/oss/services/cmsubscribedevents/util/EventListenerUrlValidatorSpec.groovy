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

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import spock.lang.Unroll

@Unroll
class EventListenerUrlValidatorSpec extends CdiSpecification {

    @ObjectUnderTest
    EventListenerUrlValidator eventListenerUrlValidator

    def "Event Listener URL validator throws a MalformedURLException for invalid URL #url"() {
        given: "An invalid URL"
        when: "URL validator checks if a URL is valid"
            eventListenerUrlValidator.validateEventListenerUrl(url)
        then: "URL validator throws a Malformed URL Exception"
            thrown(MalformedURLException)
        where:
            url                                                                          | _
            "xyz"                                                                        | _
            "http://"                                                                    | _
            "https://"                                                                   | _
            "https!://"                                                                  | _
            "http://2001:db8:3333:4444:5555:6666:7777:8888:9994/eventListener/v1/sub1"   | _
            "https://[2001:db8:3333:4444:5555:6666:7777:8888:9994/eventListener/v1/sub1" | _
            "https://localhost/eventListener1/v11"                                       | _
            "https://localhost/eventListener/v1 /"                                       | _
            "https://localhost:8080/eventListener/test/v11"                              | _
            "https://192.168.0.1/eventListener/new/v"                                    | _
            "https://192.168.0.1/eventListener/v"                                        | _
            "https://192.168.0.1/eventlistener/v"                                        | _
            "https://192.168.0.1/eventListener/v/"                                       | _
            "https://192.168.0.1/eventListener/v/v2/eventListener/v"                     | _

    }

    def "Event Listener URL validator does not throw a MalformedURLException for valid #url"() {
        given: "A valid URL"
        when: "URL validator checks if a URL is valid"
            eventListenerUrlValidator.validateEventListenerUrl(url)
        then: "URL validator does not throw an exception"
            noExceptionThrown()
        where:
            url                                                                           | _
            "http://[2001:db8:3333:4444:5555:6666:7777:8888]/eventListener/v1/sub1"       | _
            "http://[2001:db8:3333:4444:5555:6666:7777:8888]/home/eventListener/v1/sub1"  | _
            "http://[2001:db8:3333:4444:5555:6666:7777:8888]:9994/eventListener/v1/sub1"  | _
            "https://[2001:db8:3333:4444:5555:6666:7777:8888]:9994/eventListener/v1/sub1" | _
            "https://site.com/eventListener/v11"                                          | _
            "https://localhost/eventListener/v11"                                         | _
            "https://localhost:8080/eventListener/v11"                                    | _
            "https://192.168.0.1/eventListener/v11"                                       | _
            "https://192.168.0.1/eventListener/v11/sub99"                                 | _
            "https://192.168.0.1/eventListener/v1.1.2"                                    | _
            "https://192.168.0.1/eventListener/v93.100.0"                                 | _
            "https://192.168.0.1/eventListener/v12.3"                                     | _
            "https://192.168.0.1/eventListener/vx"                                        | _
            "https://192.168.0.1/eventListener/v/v2/eventListener/v2"                     | _
    }
}
