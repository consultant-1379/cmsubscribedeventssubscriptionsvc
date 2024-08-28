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

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsAttributeChangedEvent
import com.ericsson.oss.mediation.network.api.notifications.NodeNotification
import com.ericsson.oss.mediation.notifications.ComEcimNodeNotification
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionService
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription
import com.ericsson.oss.services.cmsubscribedevents.nbi.queue.EventQueue
import com.ericsson.oss.services.cmsubscribedevents.processor.ComEcimEventChangeProcessor
import com.ericsson.oss.services.cmsubscribedevents.processor.CppEventChangeProcessor
import com.ericsson.oss.services.cmsubscribedevents.processor.DpsEventChangeProcessor

import javax.inject.Inject

class SubscribedEventsProcessorEjbSpec extends CdiSpecification {

    @ObjectUnderTest
    SubscribedEventsProcessorEjb subscribedEventsProcessorEjb

    @MockedImplementation
    ComEcimEventChangeProcessor comEcimEventChangeProcessor

    @MockedImplementation
    CppEventChangeProcessor cppEventChangeProcessor

    @MockedImplementation
    DpsEventChangeProcessor dpsEventChangeProcessor

    @MockedImplementation
    SubscriptionService subscriptionService

    def setup () {
        subscriptionService.viewAllSubscriptions() >> "[{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"http://141.137.232.12:9004/eventListener/v1/SUB1\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"9\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}]"
    }

    def "when dps change event received, dps processor invoked" () {
        given: "Dps event"
            DpsAttributeChangedEvent event = new DpsAttributeChangedEvent()
        when: "subscribed events processor processor dps event"
            subscribedEventsProcessorEjb.processEvent(event)
        then: "dps processor is called"
            1 * dpsEventChangeProcessor.processChangeEvent(event, _ as List<Subscription>);
    }

    def "when com ecim change event received, com ecim processor invoked" () {
        given: "Dps event"
            ComEcimNodeNotification event = new ComEcimNodeNotification("MockDn", 1L, new Date().toString(), 1L, false)
        when: "subscribed events processor processor dps event"
            subscribedEventsProcessorEjb.processEvent(event)
        then: "dps processor is called"
            1 * comEcimEventChangeProcessor.processChangeEvent(event, _ as List<Subscription>);
    }

    def "when cpp change event received, cpp processor invoked" () {
        given: "Dps event"
            NodeNotification event = new NodeNotification()
        when: "subscribed events processor processor dps event"
            subscribedEventsProcessorEjb.processEvent(event)
        then: "dps processor is called"
            1 * cppEventChangeProcessor.processChangeEvent(event, _ as List<Subscription>);
    }

}
