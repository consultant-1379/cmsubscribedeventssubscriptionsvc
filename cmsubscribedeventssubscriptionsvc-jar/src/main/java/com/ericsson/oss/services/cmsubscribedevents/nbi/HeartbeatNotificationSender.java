/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 *  COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cmsubscribedevents.nbi;

import static com.ericsson.oss.services.cmsubscribedevents.api.constants.ExceptionMessageConstants.HEARTBEAT_GENERAL_ERROR;
import static com.ericsson.oss.services.cmsubscribedevents.api.constants.ExceptionMessageConstants.HEARTBEAT_TIMEOUT_ERROR;

import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.SubscriptionHeartbeatException;
import com.ericsson.oss.services.cmsubscribedevents.util.HttpsConfigurator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Sends Heartbeat VES event notifications via HTTP.
 */
public class HeartbeatNotificationSender {

    public static final String CONTENT_TYPE = "Content-type";

    public static final String APPLICATION_JSON = "application/json";

    private final Logger logger = LoggerFactory.getLogger(HeartbeatNotificationSender.class);

    @Inject
    HttpsConfigurator httpsConfigurator;

    /**
     * Executes HTTP POST for heartbeat notification using the inputted parameters.
     *
     * @param eventListenerUrl
     *     - URL of the Event Listener.
     * @param notificationBody
     *     - JSON body of the notification.
     * @param heartbeatConnectionTimeoutSec
     *     - Timeout in seconds for http connection.
     * @return True if eventListenerUrl returns a 204 response within the specified timeout otherwise returns false.
     */
    public boolean isHeartbeatNotificationSuccessful(final String eventListenerUrl, final StringEntity notificationBody,
        final int heartbeatConnectionTimeoutSec) {

        final HttpResponse response = sendHttpPostRequest(eventListenerUrl, notificationBody, heartbeatConnectionTimeoutSec);
        final int statusCode = response.getStatusLine().getStatusCode();

        logger.debug("Received Heartbeat RESTful HTTP Response Code: [{}] for URL: [{}]", statusCode, eventListenerUrl);
        return statusCode == 204;
    }

    private HttpResponse sendHttpPostRequest(final String eventListenerUrl, final StringEntity notificationBody,
        final int heartbeatConnectionTimeoutSec) {

        final HttpPost httpPostRequest = new HttpPost(eventListenerUrl);
        httpPostRequest.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        httpPostRequest.setEntity(notificationBody);
        final HttpResponse response;
        final RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(heartbeatConnectionTimeoutSec * 1000)
            .setConnectionRequestTimeout(heartbeatConnectionTimeoutSec * 1000)
            .setSocketTimeout(heartbeatConnectionTimeoutSec * 1000).build();

        try (CloseableHttpClient httpClient = httpsConfigurator.isSecure(eventListenerUrl)
            ? HttpClientBuilder.create().setDefaultRequestConfig(config).setSSLSocketFactory(httpsConfigurator.getSSLConnectionSocketFactory()).build()
            : HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {
            response = httpClient.execute(httpPostRequest);
        } catch (ConnectTimeoutException | SocketTimeoutException e) {
            logger.debug("Heartbeat time out", e);
            throw new SubscriptionHeartbeatException(HEARTBEAT_TIMEOUT_ERROR);
        } catch (IOException e) {
            logger.debug("Heartbeat IO Exception", e);
            throw new SubscriptionHeartbeatException(HEARTBEAT_GENERAL_ERROR);
        }
        return response;
    }
}