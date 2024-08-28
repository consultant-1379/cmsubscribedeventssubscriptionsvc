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

import java.util.Arrays;

import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.test.spi.TestResult;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class CmSubscribedEventsTestRunner implements TestRunner {

    @SuppressWarnings("deprecation")
    @Override
    public TestResult execute(Class<?> testClass, String methodName) {
        final CmSubscribedEventsTestListener listener = new CmSubscribedEventsTestListener();

        final TestNG runner = new TestNG(false);
        runner.setVerbose(0);

        runner.addListener(listener);
        runner.setXmlSuites(Arrays.asList(createSuite(testClass, methodName)));
        runner.run();
        return listener.getTestResult();
    }

    private static XmlSuite createSuite(final Class<?> className, final String methodName) {
        final XmlSuite suite = new XmlSuite();
        suite.setName("Arquillian");

        final XmlTest test = new XmlTest(suite);
        test.setName("Arquillian - " + className);;

        final XmlClass testClass = new XmlClass(className);
        testClass.getIncludedMethods().add(new XmlInclude(methodName));

        test.setXmlClasses(Arrays.asList(testClass));
        return suite;
    }
}
