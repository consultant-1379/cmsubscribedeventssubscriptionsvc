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
package com.ericsson.oss.services.cmsubscribedevents.enums

import com.ericsson.cds.cdi.support.spock.CdiSpecification

/**
 * Test class for {@link OperationType}
 */
class OperationTypeSpec extends CdiSpecification {

    def "Verify the correct notification type returned"() {

        when: "the notification type is requested"
            def notificationType = operationType.getNotificationType()

        then: "the correct value is returned"
        notificationType == expectedNotificationType

        where:
            operationType             | expectedNotificationType
            OperationType.CREATE      | "notifyMOICreation"
            OperationType.REPLACE     | "notifyMOIAttributeValueChanges"
            OperationType.DELETE      | "notifyMOIDeletion"
            OperationType.ALL_CHANGES | "notifyMOIChanges"
    }

}