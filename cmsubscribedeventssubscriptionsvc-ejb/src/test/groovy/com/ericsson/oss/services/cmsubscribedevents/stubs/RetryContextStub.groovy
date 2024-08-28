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
package com.ericsson.oss.services.cmsubscribedevents.stubs

import com.ericsson.oss.itpf.sdk.core.retry.RetryContext
/**
 * Stubs <code>RetryContext</code>.
 */
public class RetryContextStub implements RetryContext {

    @Override
    public String getCommandId() {
        return null
    }

    @Override
    public int getCurrentAttempt() {
        return 1
    }
}
