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

import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Appender implementation to add required TestNG resources to the deployed archive
 */
public class TestNGDeploymentAppender extends CachedAuxilliaryArchiveAppender {

    @Override
    public Archive<?> buildArchive() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "arquillian-testng.jar")
                .addPackages(true, Filters.exclude("/org/testng/junit/.*|/org/testng/eclipse/.*"), "org.testng", "bsh", "org.jboss.arquillian.testng")
                .addAsServiceProvider(TestRunner.class, CmSubscribedEventsTestRunner.class);

        optionalPackages(archive, Filters.exclude(".*/InterceptorStackCallback\\$InterceptedMethodInvocation.*"), "com.google.inject");
        optionalPackages(archive, Filters.includeAll(), "com.beust");

        return archive;
    }

    private static void optionalPackages(final JavaArchive jar, final Filter<ArchivePath> filter, final String... packages) {
        jar.addPackages(true, filter, packages);
    }
}