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

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class CommonNotificationDataSpec extends CdiSpecification {

    CommonNotificationData commonNotificationData

    def "When CommonNotificationData.toString() is invoked, corresponding string is returned"() {
        given: "CommonNotificationData object is created"
             Map<String, Object> attributesMap = new HashMap<String, Object>()
             attributesMap.put("userLabel", "xyz")
             commonNotificationData = new CommonNotificationData("SubNetwork=ABC,ManagedElement=ME01", "2023-06-23T13:39:21Z", 2L, attributesMap)
        when: "CommonNotificationData.toString() is invoked"
            def result = commonNotificationData.toString()
        then: "Corresponding string with all fields are returned"
            result.contains("Dn: SubNetwork=ABC,ManagedElement=ME01, notifSqNr: 2, updateAttributes: {userLabel=xyz}, timestamp: 2023-06-23T13:39:21Z")
    }
}
