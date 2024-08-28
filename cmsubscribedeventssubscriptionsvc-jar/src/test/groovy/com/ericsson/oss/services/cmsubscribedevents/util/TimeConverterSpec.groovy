/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2023
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cmsubscribedevents.util

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import javax.inject.Inject
import org.slf4j.Logger

class TimeConverterSpec extends CdiSpecification {

    @Inject
    Logger logger

    @ObjectUnderTest
    TimeConverter timeConverter

    def "When an incoming timestamp is in a supported format the UTC time with the expected content is outputted"() {
        when: "Timestamp is converted"
            String convertedTimestamp = timeConverter.covertToUTC(incomingTimestamp)

        then: "Timestamp is the expected format"
            convertedTimestamp == expectedTimestamp

        and: "Warning is not logged"
            0 * logger.warn(_ as String, incomingTimestamp)

        where:
            incomingTimestamp              | expectedTimestamp
            "2023-04-14T09:40:47Z"         | "2023-04-14T09:40:47.000Z"
            "2016-04-24T11:12:12-0100"     | "2016-04-24T12:12:12.000Z"
            "Sun Nov 06 23:10:10 IST 2022" | "2022-11-06T23:10:10.000Z"
            "Wed Dec 07 13:00:06 GMT 2022" | "2022-12-07T13:00:06.000Z"
            "Tue May 09 11:27:07 UTC 2023" | "2023-05-09T11:27:07.000Z"
    }

    def "When an incoming timestamp is not in a supported format the system UTC time with the expected format is outputted"() {
        when: "Timestamp is converted"
            String convertedTimestamp = timeConverter.covertToUTC(incomingTimestamp)

        then: "Timestamp is the expected format"
            convertedTimestamp ==~ /[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9]{3}Z/

        and: "Warning is logged"
            1 * logger.warn(_ as String, incomingTimestamp)

        where:
            incomingTimestamp      | _
            ""                     | _
            "2016-04-24T12:12.12H" | _

    }

    def "When an incoming timestamp is in a supported format in a different timezone the system UTC time with the expected format is outputted "() {
        given: "A timezone"
            TimeZone.setDefault(TimeZone.getTimeZone(timeZone))

        when: "Timestamp is converted"
            String convertedTimestamp = timeConverter.covertToUTC(incomingTimestamp)

        then: "Timestamp is the expected format"
            convertedTimestamp ==~ /[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9]{3}Z/

        and: "Warning is not logged"
            0 * logger.warn(_ as String, incomingTimestamp)

        where:
            incomingTimestamp      | timeZone        | _
            "2016-04-24T12:12:12Z" | "Asia/Tokyo"    | _
            "2016-04-24T12:12:12Z" | "Europe/Dublin" | _
    }

    def "When an incoming timestamp is in correct format it is not updated"() {
        given: "A timestamp in the correct format"
            String incomingTimestamp = "2023-04-14T09:40:47.000Z"

        when: "Timestamp is converted"
            String convertedTimestamp = timeConverter.covertToUTC(incomingTimestamp)

        then: "Timestamp is the expected format"
            convertedTimestamp == incomingTimestamp

        and: "Warning is not logged"
            0 * logger.warn(_ as String, incomingTimestamp)
    }
}
