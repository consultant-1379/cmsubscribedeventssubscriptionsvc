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

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.HttpsConfigurationException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import javax.inject.Inject
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.ssl.SSLContexts
import org.slf4j.Logger

class HttpsConfiguratorSpec extends CdiSpecification {

    @ObjectUnderTest
    HttpsConfigurator httpsConfigurator

    @MockedImplementation
    SSLContexts sslContexts

    @Inject
    Logger logger

    def "When a URL passed whose protocol isn't https, false is returned and SSL wrapping process doesn't continue"() {
        given: "Event Listener with http only address"

        when: "URL is passed to HttpsConfigurator"

        then: "False returned, not a https address"
            !httpsConfigurator.isSecure(invalidEventListener)

        where:
            invalidEventListener                                  | _
            "http://141.137.173.200:8443/eventListener/v1/SUB1"   | _
            "xyz"                                                 | _
    }

    def "When an URL with https protocol is passed, true is returned"() {
        given: "Event Listener with invalid address"
            String https_eventListener = "https://141.137.173.200:8443/eventListener/v1/SUB1"

        when: "URL is passed to HttpsConfigurator"

        then: "True returned and no exception thrown"
            httpsConfigurator.isSecure(https_eventListener)
    }

    def "When HttpClient calls to be wrapped in SSL, an SSLConnectionSocketFactory is successfully returned"() {
        given:"Establishing of trust managers for HTTPS connection is mocked"
            HttpsConfigurator httpsConfigurator = new HttpsConfigurator() {
                @Override
                SSLContextBuilder loadTrust(final SSLContextBuilder sslContextBuilder) {
                    return sslContextBuilder
                }
            }

            HttpsConfigurator httpsConfiguratorSpy = Spy(HttpsConfigurator)
            SSLContextBuilder sslContextBuilder = SSLContexts.custom();
            httpsConfiguratorSpy.getSslBuild() >> sslContextBuilder

        when: "HttpsConfigurator is called"
            httpsConfigurator.getSSLConnectionSocketFactory()

        then: "No errors logged or thrown"
            noExceptionThrown()

    }

    def "When an exception is thrown while loading the keystore, expected error is logged"() {
        given:"SSLContextBuilder mocked and returned for initial call"
            SSLContextBuilder sslContextBuilder = Mock(SSLContextBuilder)
            sslContexts.custom() >> sslContextBuilder

        when: "Various exceptions to be thrown during building of trust"
            sslContextBuilder.loadTrustMaterial(*_) >> { throw exceptionThrown }
            httpsConfigurator.getSSLConnectionSocketFactory()

        then: "Error logged and exception thrown"
            final HttpsConfigurationException exception = thrown()
            exception.message.contains( 'Exception in loading of trust material')

        where:
            exceptionThrown                 | _
            new KeyStoreException()         | _
            new NoSuchAlgorithmException()  | _
            new CertificateException()      | _
            new IOException()               | _
    }

    def "When an exception is thrown while building SSLContext, error is logged and exception is thrown"() {
        given:"SSLContext built to use incorrect protocol"
            HttpsConfigurator httpsConfigurator = new HttpsConfigurator() {
                @Override
                SSLContextBuilder loadTrust(final SSLContextBuilder sslContextBuilder) {
                    sslContextBuilder.useProtocol("xof")
                    return sslContextBuilder
                }
            }

        when: "HttpsConfigurator is called"
            httpsConfigurator.getSSLConnectionSocketFactory()

        then: "Error is thrown"
            final HttpsConfigurationException exception = thrown()
            exception.message.contains( 'Error while building SSLContext')
    }
}