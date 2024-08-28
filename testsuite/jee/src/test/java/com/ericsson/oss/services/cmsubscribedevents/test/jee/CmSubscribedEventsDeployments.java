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
package com.ericsson.oss.services.cmsubscribedevents.test.jee;

import java.io.File;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import com.ericsson.oss.services.cmsubscribedevents.test.jee.ejb.CmSubscribedEventsNbiTest;

/**
 * Creates the deployed test war, packaging the test class and dependent packages and libraries
 *
 */
@ArquillianSuiteDeployment
public class CmSubscribedEventsDeployments {

    private static final String GROUP_HTTP_CLIENT = "org.apache.httpcomponents";

    private CmSubscribedEventsDeployments() {
    }

    @Deployment()
    public static Archive<?> generate() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war");
        archive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        archive.addPackage(CmSubscribedEventsNbiTest.class.getPackage());

        archive.addAsLibrary(fromMaven("com.ericsson.oss.itpf.datalayer.dps:dps-api:?"));
        archive.addAsLibrary(fromMaven("com.ericsson.oss.mediation:network-element-connector-api-jar:?"));
        archive.addAsLibrary(fromMaven("com.ericsson.oss.mediation.cm:com-ecim-mdb-notification-listener-api-jar:?"));

        archive.addAsLibraries(resolveAsFiles(GROUP_HTTP_CLIENT, "httpcore"));
        archive.addAsLibraries(resolveAsFiles(GROUP_HTTP_CLIENT, "httpclient"));

        return archive;
    }

    private static File[] resolveAsFiles(final String groupId, final String artifactId) {
        return Maven.resolver().loadPomFromFile("pom.xml").resolve(groupId + ":" + artifactId).withTransitivity().asFile();
    }

    private static File fromMaven(final String gav) {
        return Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve(gav)
                .withoutTransitivity()
                .asSingleFile();
    }
}
