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

package com.ericsson.oss.services.cmsubscribedevents.api.constants

import spock.lang.Specification

class SignalMessageConstantsSpec extends Specification {

    def "A utility class is instantiated an exception is thrown"() {
        when: "A user tries to instantiate a utility class"
            new SignalMessageConstants()
        then: "An exception is thrown"
            thrown(IllegalStateException)
    }
}
