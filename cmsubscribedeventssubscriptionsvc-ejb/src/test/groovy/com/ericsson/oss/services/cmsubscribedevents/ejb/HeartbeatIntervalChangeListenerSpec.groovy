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

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import javax.inject.Inject

/**
 * Test class for {@link com.ericsson.oss.services.cmsubscribedevents.heartbeat.HeartbeatIntervalChangeListener}
 */
class HeartbeatIntervalChangeListenerSpec extends CdiSpecification {

    @ObjectUnderTest
    HeartbeatIntervalChangeListener heartbeatIntervalChangeListener

    @MockedImplementation
    ContinuousHeartbeatScheduler continuousHeartbeatScheduler;

    def "When the Heartbeat interval is updated the parameter changes in the system"() {
        given: "The heartbeat value is at default value"
            heartbeatIntervalChangeListener.continuousHeartbeatScheduler >> continuousHeartbeatScheduler
            heartbeatIntervalChangeListener.cmSubscribedEventsHeartbeatInterval = 30
            assert heartbeatIntervalChangeListener.getCmSubscribedEventsHeartbeatInterval () == 30

        when: "Listener detects update to interval and applies it"
            heartbeatIntervalChangeListener.listenForCmSubscribedEventsHeartbeatIntervalChanges (45)

        then: "Heartbeat interval is updated to new value"
            assert heartbeatIntervalChangeListener.getCmSubscribedEventsHeartbeatInterval () == 45
            1 * continuousHeartbeatScheduler.changeInterval()
    }
}