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

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent

class MembershipListenerImplSpec extends CdiSpecification {

    @ObjectUnderTest
    MembershipListenerImpl membershipListenerImpl

    def "when listenForMembershipChange is invoked by a MembershipChangeEvent as master instance true,then isMaster returns corresponding true'"() {

        given: "MembershipChangeEvent's isMaster returns true"
            def membershipChangeEvent = Mock(MembershipChangeEvent)
            membershipChangeEvent.isMaster() >> true

        when: "Observer method for MembershipChangeEvent change is invoked"
            membershipListenerImpl.listenForMembershipChange(membershipChangeEvent)

        then: "membershipListenerImpl.isMaster returns true"
            membershipListenerImpl.isMaster() == true
    }

    def "when listenForMembershipChange is invoked by a MembershipChangeEvent as master instance false,then isMaster returns corresponding false'"() {

        given: "MembershipChangeEvent's isMaster returns false"
            def membershipChangeEvent = Mock(MembershipChangeEvent)
            membershipChangeEvent.isMaster() >> false

        when: "At Scheduled intervals, execute() is invoked"
            membershipListenerImpl.listenForMembershipChange(membershipChangeEvent)

        then: "membershipListenerImpl.isMaster returns false"
            membershipListenerImpl.isMaster() == false
    }
}