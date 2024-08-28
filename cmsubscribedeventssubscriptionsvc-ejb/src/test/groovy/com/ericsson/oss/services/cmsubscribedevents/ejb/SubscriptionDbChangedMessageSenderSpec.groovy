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

import com.ericsson.oss.itpf.sdk.cluster.classic.ServiceClusterManagerSpiTestImpl
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent.ClusterMemberInfo
import com.ericsson.oss.itpf.sdk.cluster.classic.ServiceClusterBean
import com.ericsson.oss.itpf.sdk.cluster.classic.ClusterMessageListener
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionService
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription
import com.ericsson.oss.services.cmsubscribedevents.processor.ComEcimEventChangeProcessor
import com.ericsson.oss.services.cmsubscribedevents.processor.CppEventChangeProcessor
import com.ericsson.oss.services.cmsubscribedevents.processor.DpsEventChangeProcessor

import java.util.Collections;
import javax.inject.Inject

class SubscriptionDbChangedMessageSenderSpec extends CdiSpecification {

    @Inject
    SubscriptionDbChangedMessageSender subscriptionDbChangedMessageSender

    @Inject
    ServiceClusterManagerSpiTestImpl serviceClusterManagerSpiTestImpl

    @MockedImplementation
    SubscriptionDbChangedMessageListener subscriptionDbChangedMessageListener

    def "when message sender has joined the cluster, signal message can be sent"() {
        given: "sender ha joined the cluster"
            subscriptionDbChangedMessageSender.joinCluster()
        when: "send signal message"
            subscriptionDbChangedMessageSender.sendSignalMessage()
        then: "no exception is thrown"
            noExceptionThrown()
    }

    def "when message sender has joined the cluster, leave cluster execute with no exception"() {
        given: "sender ha joined the cluster"
            subscriptionDbChangedMessageSender.joinCluster()
        when: "send sinal message"
            subscriptionDbChangedMessageSender.leaveClusterService()
        then: "no exception is thrown"
            noExceptionThrown()
    }

}