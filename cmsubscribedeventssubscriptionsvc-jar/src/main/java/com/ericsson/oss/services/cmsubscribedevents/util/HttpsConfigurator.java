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
package com.ericsson.oss.services.cmsubscribedevents.util;

import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.HttpsConfigurationException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Determines if notificationRecipientAddress provided is a https address, and if so configures the connection with an SSL Context
 */
public class HttpsConfigurator {

    private static final Logger logger = LoggerFactory.getLogger(HttpsConfigurator.class);

    private static final String TRUSTSTORE_PATH = System.getProperty("TRUSTSTORE_PATH", "/ericsson/cmsubscribedevents/data/certs/cmeventstrust.jks");
    private static final String HTTPS = "https";

    /**
     * Executes HTTP POST for heartbeat notification using the inputted parameters.
     *
     * @param eventListenerUrl
     *     - URL of the Event Listener.
     * @return true if event listener URL has https as its protocol, otherwise false.
     */
    public boolean isSecure(final String eventListenerUrl) {
        try {
            URL url = new URL(eventListenerUrl);
            return HTTPS.equalsIgnoreCase(url.getProtocol());
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Builds an SSL Context around the truststore provided and returns
     *
     * @return SSLConnectionSocketFactory of the built SSLContext
     */
    public SSLConnectionSocketFactory getSSLConnectionSocketFactory() {
        SSLContextBuilder sslBuilder = getSslBuild();

        try {
            sslBuilder = loadTrust(sslBuilder);
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException e) {
            final String errorMessage = String.format("Exception in loading of trust material: %s", e.getMessage());
            logger.error(errorMessage);

            throw new HttpsConfigurationException(errorMessage);
        }

        SSLContext sslcontext;
        try {
            sslcontext = sslBuilder.build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            final String errorMessage = String.format("Error while building SSLContext: %s", e.getMessage());
            logger.error(errorMessage);

            throw new HttpsConfigurationException(errorMessage);
        }

        return new SSLConnectionSocketFactory(sslcontext, new NoopHostnameVerifier());
    }

    public SSLContextBuilder getSslBuild() {
        return SSLContexts.custom();
    }

    public SSLContextBuilder loadTrust(final SSLContextBuilder sslContextBuilder)
        throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        File file = new File(TRUSTSTORE_PATH);

        sslContextBuilder.loadTrustMaterial(file, "Cmeventscert1".toCharArray());

        return sslContextBuilder;
    }
}