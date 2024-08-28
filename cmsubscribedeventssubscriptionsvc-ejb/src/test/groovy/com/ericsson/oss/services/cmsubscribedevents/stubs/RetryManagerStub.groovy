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
 
 import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand
 import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException
 import com.ericsson.oss.itpf.sdk.core.retry.RetryManager
 import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy
 /**
  * Stubs <code>RetryManager</code>.
  */
 public class RetryManagerStub implements RetryManager {
 
     private boolean throwException = false
     @Override
     public <T> T executeCommand(final RetryPolicy retryPolicy, final RetriableCommand<T> command) throws IllegalArgumentException,
     RetriableCommandException {
         if (throwException) {
             throw new RetriableCommandException()
         }
         return command.execute()
     }
 
     public void setThrowException(final boolean throwException) {
         this.throwException = throwException
     }
 }
 