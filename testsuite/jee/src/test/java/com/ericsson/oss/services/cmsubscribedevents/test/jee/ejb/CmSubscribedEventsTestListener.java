/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.cmsubscribedevents.test.jee.ejb;

import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.jboss.arquillian.test.spi.TestResult;

public class CmSubscribedEventsTestListener implements ITestListener {

    private ITestContext context;

    @Override
	public void onTestStart(ITestResult result) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTestFailure(ITestResult result) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStart(ITestContext context) {
        // TODO Auto-generated method stub
        this.context = context;
    }

    @Override
    public void onFinish(ITestContext context) {
        this.context = context;
    }

    public TestResult getTestResult() {
        if (context.getFailedConfigurations().size() > 0) {
            return TestResult.failed(getThrowableFor(context.getFailedConfigurations()));
        } else if (context.getFailedTests().size() > 0) {
            return TestResult.failed(getThrowableFor(context.getFailedTests()));
        } else if (context.getSkippedTests().size() > 0) {
            return TestResult.skipped(new Throwable());
        } else if (context.getPassedTests().size() > 0) {
            return TestResult.passed().setThrowable(getThrowableFor(context.getPassedTests()));
        } else {
            return TestResult.failed(new IllegalStateException("Unknown test result: " + context).fillInStackTrace());
        }
    }

    private static Throwable getThrowableFor(final IResultMap resultMap) {
        return resultMap.getAllResults().iterator().next().getThrowable();
    }

}
