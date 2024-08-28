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

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class DataConverterSpec extends CdiSpecification {

    def "When a valid node FDN is inputted the target name is returned"() {
        when: "Data converter is used to retrieve the target name"
            String returnedTargetName = DataConverter.retrieveTargetName(FDN)

        then: "Target name is returned"
            returnedTargetName == targetName

        where:
            FDN                                                                                                                  | targetName
            "MeContext=Transport01ML6691-1-5-1-05,ManagedElement=Transport01ML6691-1-5-1-05,mib-2=1"                             | "Transport01ML6691-1-5-1-05"
            "SubNetwork=AutoProvisioning,ManagedElement=LTE01dg2ERBS00023,UsedServiceFunction=1,UsedServiceType=1"               | "LTE01dg2ERBS00023"
            "SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR01gNodeBRadio00002"                                                | "NR01gNodeBRadio00002"
            "MeContext=Node1"                                                                                                    | "Node1"
            "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=ieatnetsimv14987_LTE03ERBS00003,Inventory=1" | "ieatnetsimv14987_LTE03ERBS00003"
            "Network=1,Link=Id-SPFRER60001/1/1-SPFRER60002/1/2"                                                                  | "Id-SPFRER60001/1/1-SPFRER60002/1/2"
    }

    def "When a invalid node FDN is inputted an exception returned"() {
        when: "Data converter is used to retrieve the target name"
            DataConverter.retrieveTargetName(FDN)

        then: "Target name is returned"
            thrown(IllegalArgumentException)

        where:
            FDN          | _
            ""           | _
            "MeCmib-2=1" | _
            null         | _
    }
}

