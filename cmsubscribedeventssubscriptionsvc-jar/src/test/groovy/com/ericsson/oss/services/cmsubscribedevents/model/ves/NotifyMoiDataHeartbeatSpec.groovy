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
package com.ericsson.oss.services.cmsubscribedevents.model.ves

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification

class NotifyMoiDataHeartbeatSpec extends CdiSpecification {

    @ObjectUnderTest
    NotifyMoiDataHeartbeat notifyMoiDataHeartbeat


    def "When NotifyMoiDataHeartbeat is created heartbeat is populated with expected data"() {
        given: "CmSubscribedEventsHeartbeatInterval PIB parameter is set to 10"
        when: "The heartbeat is initialized"
            String heartbeatNotificationPeriod = notifyMoiDataHeartbeat.getHeartbeatNtfPeriod()
        then: "The heartbeat contains the expected heartbeatNotificationPeriod"
            heartbeatNotificationPeriod == "30"
    }
}
