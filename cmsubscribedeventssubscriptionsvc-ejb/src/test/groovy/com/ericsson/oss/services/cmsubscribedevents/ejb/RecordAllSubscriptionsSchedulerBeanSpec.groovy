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
import javax.inject.Inject

class RecordAllSubscriptionsSchedulerBeanSpec extends CdiSpecification {

    @ObjectUnderTest
    RecordAllSubscriptionsSchedulerBean recordAllSubscriptionsSchedulerBean

    def "When at RecordAllSubscriptionsSchedulerBean timer timeout, viewAllSubscription() is invoked and recorded all available subscriptions on master instance"() {

        given:
            recordAllSubscriptionsSchedulerBean.membershipListener.isMaster() >> true
            recordAllSubscriptionsSchedulerBean.subscriptionService.viewAllSubscriptions() >> "[{\"ntfSubscriptionControl\":{\"id\":\"1\",\"notificationRecipientAddress\":\"http://testHost:9000/eventListener/v1/sub1\",\"notificationTypes\":[\"notifyMOICreation\",\"notifyMOIDeletion\",\"notifyMOIAttributeValueChanges\",\"notifyMOIChanges\"],\"objectInstance\":\"/\",\"objectClass\":\"/\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0}}}]"

        when: "At Scheduled intervals, execute() is invoked"
            recordAllSubscriptionsSchedulerBean.execute()

        then: "Subscriptions are recorded by SystemRecorder.recordEventData()"
            1 * recordAllSubscriptionsSchedulerBean.systemRecorder.recordEventData(_ as String, _ as Map)
    }

    def "When at RecordAllSubscriptionsSchedulerBean timer timeout, all available subscriptions are not recorded if it's not a master instance"() {

        given:
            recordAllSubscriptionsSchedulerBean.membershipListener.isMaster() >> false
            recordAllSubscriptionsSchedulerBean.subscriptionService.viewAllSubscriptions() >> "[{\"ntfSubscriptionControl\":{\"id\":\"1\",\"notificationRecipientAddress\":\"http://testHost:9000/eventListener/v1/sub1\",\"notificationTypes\":[\"notifyMOICreation\",\"notifyMOIDeletion\",\"notifyMOIAttributeValueChanges\",\"notifyMOIChanges\"],\"objectInstance\":\"/\",\"objectClass\":\"/\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0}}}]"

        when: "At Scheduled intervals, execute() is invoked"
            recordAllSubscriptionsSchedulerBean.execute()

        then: "Subscriptions are recorded by SystemRecorder.recordEventData()"
            0 * recordAllSubscriptionsSchedulerBean.systemRecorder.recordEventData(_ as String, _ as Map)
    }

    def "When at RecordAllSubscriptionsSchedulerBean timer timeout, viewAllSubscription() is invoked and recorded all available subscriptions including those with null scope"() {

        given:
            recordAllSubscriptionsSchedulerBean.membershipListener.isMaster() >> true
            recordAllSubscriptionsSchedulerBean.subscriptionService.viewAllSubscriptions() >> "[{\"ntfSubscriptionControl\":{\"id\":\"1\",\"notificationRecipientAddress\":\"http://testHost:9000/eventListener/v1/sub1\",\"notificationTypes\":[\"notifyMOICreation\",\"notifyMOIDeletion\",\"notifyMOIAttributeValueChanges\",\"notifyMOIChanges\"],\"objectInstance\":\"/\",\"objectClass\":\"/\"}}]"

        when: "At Scheduled intervals, execute() is invoked"
            recordAllSubscriptionsSchedulerBean.execute()

        then: "Subscriptions are recorded by SystemRecorder.recordEventData()"
            1 * recordAllSubscriptionsSchedulerBean.systemRecorder.recordEventData(_ as String, _ as Map)
    }
}