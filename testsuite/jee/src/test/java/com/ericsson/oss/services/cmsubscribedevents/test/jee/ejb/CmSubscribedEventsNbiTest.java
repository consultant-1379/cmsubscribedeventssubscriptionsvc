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

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;


import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.ericsson.oss.itpf.sdk.licensing.Permission;

import com.ericsson.oss.mediation.notifications.ComEcimNotificationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.testng.Arquillian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.oss.mediation.notifications.ComEcimNodeNotification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.eventbus.Channel;
import com.ericsson.oss.itpf.sdk.eventbus.annotation.Endpoint;

/**
 * Tests across full slice of CM Subscribed events.
 * Subscription requests will be made using NBI resource using service and data layer below
 *
 */
public class CmSubscribedEventsNbiTest extends Arquillian {

    public static String LOCALHOST = "172.18.0.7";
    public static final int PORT = 8080;
    public static final String HTTP = "http";
    private static final Logger logger = LoggerFactory.getLogger(CmSubscribedEventsNbiTest.class);
    private static final String CM_EVENTS_NBI_ADM_USER = "cmeventsnbi_admin";
    private static final String NO_ROLE_USER = "user_with_no_role";
    private static final String CONTEXT_USER_ID_KEY = "X-Tor-UserID";
    private static final String CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS = "/cm/subscribed-events/v1/subscriptions";
    private static final String CONTENT_TYPE = "Content-type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String UTF_8 = "UTF-8";
    private static final String WIREMOCK_FIND_REQUESTS_API = "/__admin/requests/find";
    private static final String WIREMOCK_COUNT_REQUESTS_API = "/__admin/requests/count";

    private static String createdId;

    private static final List<String> subscriptionIdsForCleanup = new ArrayList<>();

    @Inject
    PibUtil pibUtil;

    @Inject
    @Endpoint("jms:/queue/CmDataChangeDivertedQueue")
    private Channel cmDataChangeDivertedQueue;

    @BeforeClass
    private void setLocalIp() throws UnknownHostException {
        final InetAddress inetAddress = InetAddress.getLocalHost();
        LOCALHOST = inetAddress.getHostAddress();
    }

    @AfterMethod
    private void cleanUp() throws IOException, URISyntaxException {
        if (!LOCALHOST.equals("127.0.0.1")) {
            cleanUpSubscriptions();
        }
    }

    @Test(priority = 1)
    public void subscribedEventLicenseRuntimeException() throws URISyntaxException, IOException {
        StubbedLicensingServiceSpiBean.setRuntimeExceptionToBeReturnedByStub(true);
        int statusCode;
        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
        }

        StubbedLicensingServiceSpiBean.setRuntimeExceptionToBeReturnedByStub(false);

        logger.info("Received RESTful HTTP Response Code: [{}]", statusCode);
        // Check HTTP STATUS 500 = FORBIDDEN.
        assertEquals(statusCode, SC_INTERNAL_SERVER_ERROR, "The expected HTTP 500 was not returned.");
    }

    @Test(priority = 2)
    public void subscribedEventNoValidLicense() throws IOException, URISyntaxException {
        modifyCmEventsLicensingPermission(Permission.DENIED_NO_VALID_LICENSE);
        int statusCode;
        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        eventsGetRequest.setHeader(CONTEXT_USER_ID_KEY, CM_EVENTS_NBI_ADM_USER);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
        }

        // Reset the license back to Permission.ALLOWED to ensure no failures in other test cases
        modifyCmEventsLicensingPermission(Permission.ALLOWED);
        logger.info("Received RESTful HTTP Response Code: [{}]", statusCode);
        // Check HTTP STATUS 403 = FORBIDDEN.
        assertEquals(statusCode, SC_FORBIDDEN, "The expected HTTP 403 was not returned.");

    }

    @Test(priority = 3)
    public void subscribedEventsGetAllSubscriptionsWithValidLicenseAndZeroSubscriptions() throws IOException, URISyntaxException {
        int statusCode;
        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        eventsGetRequest.setHeader(CONTEXT_USER_ID_KEY, CM_EVENTS_NBI_ADM_USER);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            assertNull(entity, "List all subscriptions with zero subscribers should return no content");
        }

        logger.info("Received RESTful HTTP Response Code: [{}]", statusCode);
        // Check HTTP STATUS 200 = OK.
        assertEquals(statusCode, SC_NO_CONTENT, "The expected HTTP 204 was not returned.");
    }

    @Test(priority = 4)
    public void subscribedEventCreateSubscriptionSuccessTest() throws URISyntaxException, IOException {
        String responseMessage;
        final String subscription = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub1\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";

        responseMessage = postSubscriptionForEventHandling(subscription);

        final ObjectMapper mapper = new ObjectMapper();
        createdId = mapper.readTree(responseMessage).get("ntfSubscriptionControl").get("id").textValue();

        final String expectedResponseMessage = subscription.replace("\"id\":\"999\"", "\"id\":\"" + createdId + "\"");
        assertEquals(mapper.readTree(expectedResponseMessage), mapper.readTree(responseMessage), "The expected response message was not returned.");

    }

    @Test(priority = 5)
    public void subscribedEventCreateSubscriptionValidationFailureTest() throws URISyntaxException, IOException {
        int statusCode;
        String message;
        //missing mandatory attribute ntfSubscriptionControl.notificationRecipientAddress:
        final String expectedResponseMessage = "{\"errors\":[{\"errorMessage\":\"ntfSubscriptionControl.notificationRecipientAddress: is missing but it is required\"}]}";
        StringEntity JSONNtfSubscriptionControl = new StringEntity("{\"ntfSubscriptionControl\":{\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"" + 999 + "\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}");
        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        final HttpPost httpPostRequest = new HttpPost(uri);
        httpPostRequest.setHeader(CONTEXT_USER_ID_KEY, CM_EVENTS_NBI_ADM_USER);
        httpPostRequest.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        httpPostRequest.setEntity(JSONNtfSubscriptionControl);
        CloseableHttpClient httpclient = HttpClients.createDefault();

        logger.info("Sending RESTful HTTP POST Request: [{}]", httpPostRequest.getRequestLine().getUri());
        try (CloseableHttpResponse response = httpclient.execute(httpPostRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            message = EntityUtils.toString(entity);
            logger.info("Response message: {}", message);
        }

        // Check HTTP STATUS 400 = Bad Request.
        logger.info("Received RESTful HTTP Response Code: [{}]", statusCode);
        assertEquals(message, expectedResponseMessage, "The expected response message was not returned.");
        assertEquals(statusCode, SC_BAD_REQUEST, "The expected HTTP 400 was not returned.");

    }

    @Test(priority = 6)
    public void subscribedEventViewSubscription() throws IOException, URISyntaxException {
        final String subscription = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub1\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        String responseMessage = postSubscriptionForEventHandling(subscription);
        final ObjectMapper mapper = new ObjectMapper();
        final String subscriptionId = mapper.readTree(responseMessage).get("ntfSubscriptionControl").get("id").textValue();

        final String CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID = CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS + "/" + subscriptionId;
        int statusCode;
        final String expectedResponseMessage = subscription.replace("\"id\":\"999\"", "\"id\":\"" + createdId + "\"");

        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/{subscriptionId}
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        eventsGetRequest.setHeader(CONTEXT_USER_ID_KEY, CM_EVENTS_NBI_ADM_USER);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            responseMessage = EntityUtils.toString(entity);
        }

        // Check HTTP STATUS 200 = OK.
        assertEquals(statusCode, SC_OK, "The expected HTTP 200 was not returned.");
        assertEquals(mapper.readTree(responseMessage), mapper.readTree(expectedResponseMessage), "The expected response message was not returned.");
    }

    @Test(priority = 7)
    public void subscribedEventViewAllSubscriptions() throws IOException, URISyntaxException {
        int statusCode;
        createSubscriptionsForViewAll();


        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        eventsGetRequest.setHeader(CONTEXT_USER_ID_KEY, CM_EVENTS_NBI_ADM_USER);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());
        String getResponse;
        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
            logger.debug("Wiremock ResponseMessage Status Code: {}", statusCode);
            HttpEntity entity = response.getEntity();
            getResponse = EntityUtils.toString(entity, UTF_8);
        }

        // Check HTTP STATUS 200 = OK
        assertEquals(statusCode, SC_OK, "The expected HTTP 200 was not returned.");
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode actualResponse = mapper.readTree(getResponse);
        //amount of subscriptions in expected is 2
        assertEquals(actualResponse.size(), 2);

    }

    @Test(priority = 8)
    public void subscribedEventViewSubscriptionFailure() throws IOException, URISyntaxException {
        final String subscriptionId = "999";
        final String CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID = CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS + "/" + subscriptionId;
        int statusCode;
        String responseMessage;
        String expectedResponseMessage = "{\"errors\":[{\"errorMessage\":\"Resource could not be found.\"}]}";

        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/{subscriptionId}
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        eventsGetRequest.setHeader(CONTEXT_USER_ID_KEY, CM_EVENTS_NBI_ADM_USER);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            responseMessage = EntityUtils.toString(entity);
        }

        // Check HTTP STATUS 404 = NOT_FOUND.
        assertEquals(statusCode, SC_NOT_FOUND, "The expected HTTP 404 was not returned.");
        assertEquals(responseMessage, expectedResponseMessage, "The expected response message was not returned");

    }

    @Test(priority = 9)
    public void subscribedEventDeleteSubscription() throws IOException, URISyntaxException {
        final String subscription = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub1\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        String responseMessage = postSubscriptionForEventHandling(subscription);
        final ObjectMapper mapper = new ObjectMapper();
        final String subscriptionId = mapper.readTree(responseMessage).get("ntfSubscriptionControl").get("id").textValue();

        final String CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID = CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS + "/" + subscriptionId;
        int statusCode;
        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/{subscriptionId}
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpDelete eventsDeleteRequest = new HttpDelete(uri);
        eventsDeleteRequest.setHeader(CONTEXT_USER_ID_KEY, CM_EVENTS_NBI_ADM_USER);
        logger.info("Sending RESTful HTTP Delete Request: [{}]", eventsDeleteRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsDeleteRequest)) {
             statusCode = response.getStatusLine().getStatusCode();
         }
        // Check HTTP STATUS 204 = NO_CONTENT.
        assertEquals(statusCode, SC_NO_CONTENT, "The expected HTTP 204 was returned.");
        subscriptionIdsForCleanup.remove(subscriptionId);
    }

    @Test(priority = 10)
    public void subscribedEventInvalidUserPermission() throws IOException, URISyntaxException {
        int statusCode;

        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        eventsGetRequest.setHeader(CONTEXT_USER_ID_KEY, NO_ROLE_USER);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
        }

        logger.info("Received RESTful HTTP Response Code: [{}]", statusCode);
        // Check HTTP STATUS 401 = UnAuthorized.
        assertEquals(statusCode, SC_UNAUTHORIZED, "The expected HTTP 401 was not returned.");

    }

    @Test(priority =11)
    public void notificationSentWithMatchingNotificationTypeTest() throws InterruptedException, IOException, URISyntaxException {
        final String subscription = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub1\",\"notificationTypes\":[\"notifyMOIDeletion\", \"notifyMOIAttributeValueChanges\", \"notifyMOICreation\"],\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        postSubscriptionForEventHandling(subscription);

        URI wiremockUri = new URIBuilder().setScheme(HTTP).setHost("wiremock").setPort(8181).setPath(WIREMOCK_FIND_REQUESTS_API).build();

        final HttpPost wireMockVerificationString = new HttpPost(wiremockUri);
        wireMockVerificationString.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        final StringEntity moiJsonExpected = new StringEntity("{\"method\":\"POST\",\"url\":\"/eventListener/v1/sub1\",\"bodyPatterns\":[{\"matchesJsonPath\":\"$.event.stndDefinedFields.data[?(@.notificationType=~ /^(notifyMOICreation|notifyMOIDeletion|notifyMOIAttributeValueChanges)$/)]\"}]}");
        wireMockVerificationString.setEntity(moiJsonExpected);

        for(final Object[] testData : TestDataProvider.getNotificationEventData()) {
            final Serializable notification = (Serializable)testData[0];
            final String notificationType = (String) testData[1];
            cmDataChangeDivertedQueue.send(notification);
            TimeUnit.SECONDS.sleep(15);

            String wiremockResponseMessage = executeWiremockRequest(wireMockVerificationString);

            assertEquals(extractNotificationTypeFromWiremockResponse(wiremockResponseMessage), notificationType, "The expected Notification Type was not returned");
            deleteExistingWiremockServerRequests();
        }
    }

    @Test(priority=12)
    public void notificationSentWithMoiChangesTest() throws IOException, URISyntaxException, InterruptedException {
        final String subscriptionMoiChanges = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub2\",\"notificationTypes\":[\"notifyMOIChanges\"],\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        postSubscriptionForEventHandling(subscriptionMoiChanges);

        final ObjectMapper mapper = new ObjectMapper();

        URI wiremockUriCountRequests = new URIBuilder().setScheme(HTTP).setHost("wiremock").setPort(8181).setPath(WIREMOCK_COUNT_REQUESTS_API).build();
        final HttpPost wireMockVerificationString = new HttpPost(wiremockUriCountRequests);
        wireMockVerificationString.setHeader(CONTENT_TYPE, APPLICATION_JSON);

        final StringEntity moiChangesJsonExpected = new StringEntity("{\"method\":\"POST\",\"url\":\"/eventListener/v1/sub2\",\"bodyPatterns\":[{\"matchesJsonPath\":\"$.event.stndDefinedFields.data[?(@.notificationType== 'notifyMOIChanges')]\"}]}");
        wireMockVerificationString.setEntity(moiChangesJsonExpected);

        for(final Object[] testData : TestDataProvider.getNotificationEventData()) {
            final Serializable createEvent = (Serializable) testData[0];

            cmDataChangeDivertedQueue.send(createEvent);
            TimeUnit.SECONDS.sleep(15);

            String wiremockResponseMessage = executeWiremockRequest(wireMockVerificationString);

            JsonNode wiremockCountNode = mapper.readTree(wiremockResponseMessage);
            int numberOfMoiChangesVesReceived = wiremockCountNode.get("count").asInt();
            assertEquals(numberOfMoiChangesVesReceived, 1, "The number of expected notifyMOIChanges VES events was not received by listener");
            deleteExistingWiremockServerRequests();
        }
    }

    @Test(priority =13)
    public void pushVesEventForMultipleSubscriptions() throws IOException, URISyntaxException, InterruptedException {
        final String subscriptionBlankNotificationTypes = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub2\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        postSubscriptionForEventHandling(subscriptionBlankNotificationTypes);

        final String subscriptionWithoutNotifyMOIChanges = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub1\",\"notificationTypes\":[\"notifyMOIDeletion\", \"notifyMOIAttributeValueChanges\", \"notifyMOICreation\"],\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        postSubscriptionForEventHandling(subscriptionWithoutNotifyMOIChanges);

        final ObjectMapper mapper = new ObjectMapper();

        URI wiremockUriCountRequests = new URIBuilder().setScheme(HTTP).setHost("wiremock").setPort(8181).setPath(WIREMOCK_COUNT_REQUESTS_API).build();
        final HttpPost wireMockVerificationString = new HttpPost(wiremockUriCountRequests);
        wireMockVerificationString.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        final StringEntity moiCreationJsonExpected = new StringEntity("{\"method\":\"POST\",\"url\":\"/eventListener/v1/sub1\",\"bodyPatterns\":[{\"matchesJsonPath\":\"$.event.stndDefinedFields.data[?(@.notificationType=~ /^(notifyMOICreation|notifyMOIDeletion|notifyMOIAttributeValueChanges)$/)]\"}]}");
        wireMockVerificationString.setEntity(moiCreationJsonExpected);

        for(final Object[] testData : TestDataProvider.getNotificationEventData()) {
            final Serializable event = (Serializable)testData[0];
            cmDataChangeDivertedQueue.send(event);
            TimeUnit.SECONDS.sleep(20);

            String wiremockResponseMessage = executeWiremockRequest(wireMockVerificationString);

            JsonNode wiremockCountMoiCreationNode = mapper.readTree(wiremockResponseMessage);
            int numberOfMoiCreationVesReceived = wiremockCountMoiCreationNode.get("count").asInt();
            assertEquals(numberOfMoiCreationVesReceived, 1, "The expected number of expected notifyMOICreation VES events was not received by listener /v1/sub1/");

            final StringEntity moiChangesJsonExpected = new StringEntity("{\"method\":\"POST\",\"url\":\"/eventListener/v1/sub2\",\"bodyPatterns\":[{\"matchesJsonPath\":\"$.event.stndDefinedFields.data[?(@.notificationType== 'notifyMOIChanges')]\"}]}");
            wireMockVerificationString.setEntity(moiChangesJsonExpected);
            wiremockResponseMessage = executeWiremockRequest(wireMockVerificationString);

            JsonNode wiremockCountMoiChangesNode = mapper.readTree(wiremockResponseMessage);
            int numberOfMoiChangesVesReceived = wiremockCountMoiChangesNode.get("count").asInt();
            assertEquals(numberOfMoiChangesVesReceived, 1, "The expected number of expected notifyMOIChanges VES events was not received by listener /v1/sub2/");

            deleteExistingWiremockServerRequests();
        }
    }

    @Test(priority=14)
    public void correctListOfSubscriptionsUsedForEachEvent() throws IOException, URISyntaxException, InterruptedException {
        final String subscriptionBlankNotificationTypes = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub1\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        postSubscriptionForEventHandling(subscriptionBlankNotificationTypes);

        final String notifyMOIDeletionSubscription = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub2\",\"notificationTypes\":[\"notifyMOIDeletion\"],\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        postSubscriptionForEventHandling(notifyMOIDeletionSubscription);
        final String notifyMOIAttributeValueChangesSubscription = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub3\",\"notificationTypes\":[\"notifyMOIAttributeValueChanges\"],\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        postSubscriptionForEventHandling(notifyMOIAttributeValueChangesSubscription);
        final String notifyMOICreationSubscription = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub4\",\"notificationTypes\":[\"notifyMOICreation\"],\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        postSubscriptionForEventHandling(notifyMOICreationSubscription);

        ComEcimNodeNotification comEcimNotification = createComEcimNodeNotification();
        comEcimNotification.setAction(ComEcimNotificationType.CREATE);
        cmDataChangeDivertedQueue.send(comEcimNotification);
        comEcimNotification.setAction(ComEcimNotificationType.DELETE);
        cmDataChangeDivertedQueue.send(comEcimNotification);
        comEcimNotification.setAction(ComEcimNotificationType.UPDATE);
        cmDataChangeDivertedQueue.send(comEcimNotification);

        TimeUnit.SECONDS.sleep(20);

        URI wiremockUriCountRequests = new URIBuilder().setScheme(HTTP).setHost("wiremock").setPort(8181).setPath(WIREMOCK_COUNT_REQUESTS_API).build();
        final HttpPost wireMockVerificationString = new HttpPost(wiremockUriCountRequests);
        wireMockVerificationString.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        final StringEntity moiJsonExpected = new StringEntity("{\"method\":\"POST\",\"bodyPatterns\":[{\"matchesJsonPath\":\"$.event.stndDefinedFields.data[?(@.notificationType=~ /^(notifyMOICreation|notifyMOIDeletion|notifyMOIAttributeValueChanges|notifyMOIChanges)$/)]\"}]}");
        wireMockVerificationString.setEntity(moiJsonExpected);

        String wiremockResponseMessage = executeWiremockRequest(wireMockVerificationString);
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode wiremockCountMoiCreationNode = mapper.readTree(wiremockResponseMessage);
        int numberOfMoiCreationVesReceived = wiremockCountMoiCreationNode.get("count").asInt();
        assertEquals(numberOfMoiCreationVesReceived, 6, "The expected number notifications is 6 across all subscribers");
        deleteExistingWiremockServerRequests();
    }

    @Test(priority =15)
    public void continuousHeartbeat() throws IOException, URISyntaxException, InterruptedException {


        final String subscription = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub1\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        postSubscriptionForEventHandling(subscription);

        final ObjectMapper mapper = new ObjectMapper();

        URI wiremockUriCountRequests = new URIBuilder().setScheme(HTTP).setHost("wiremock").setPort(8181).setPath(WIREMOCK_COUNT_REQUESTS_API).build();
        final HttpPost wireMockVerificationString = new HttpPost(wiremockUriCountRequests);
        wireMockVerificationString.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        final StringEntity heartbeatJsonExpected = new StringEntity("{\"method\":\"POST\",\"url\":\"/eventListener/v1/sub1\",\"bodyPatterns\":[{\"matchesJsonPath\":\"$.event.commonEventHeader[?(@.eventName=~ /^(Heartbeat_ENM-Ericsson_VES)$/)]\"}]}");
        wireMockVerificationString.setEntity(heartbeatJsonExpected);

        deleteExistingWiremockServerRequests();

        //Set the heartbeat interval to 5 seconds
        pibUtil.updateParameter( "cmSubscribedEventsHeartbeatInterval", "5");

        //Wait for 10 seconds to include two continuous heartbeat intervals.
        TimeUnit.SECONDS.sleep(10);

        String wiremockResponseMessage = executeWiremockRequest(wireMockVerificationString);

        JsonNode wiremockCountMoiCreationNode = mapper.readTree(wiremockResponseMessage);
        int numberOfMoiCreationVesReceived = wiremockCountMoiCreationNode.get("count").asInt();
        assertEquals(numberOfMoiCreationVesReceived, 2, "The expected number of continuous heartbeats was not received by listener /v1/sub1/");


        deleteExistingWiremockServerRequests();

    }

    @Test(priority =16)
    public void continuousHeartbeatFailure() throws IOException, URISyntaxException, InterruptedException {

        resetScenarios();
        pibUtil.updateParameter( "cmSubscribedEventsHeartbeatInterval", "5");
        final String subscription = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/heartbeatFailure\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";

        String responseMessage = postSubscriptionForEventHandling(subscription);
        final ObjectMapper mapper = new ObjectMapper();
        final String subscriptionId = mapper.readTree(responseMessage).get("ntfSubscriptionControl").get("id").textValue();

        final String CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID = CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS + "/" + subscriptionId;
        int statusCode;

        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/{subscriptionId}
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        eventsGetRequest.setHeader(CONTEXT_USER_ID_KEY, CM_EVENTS_NBI_ADM_USER);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
        }

        // Check HTTP STATUS 200 = OK.
        assertEquals(statusCode, SC_OK, "The expected HTTP 200 was not returned.");
        // wait 15 seconds, 3 heartbeats should fail, and subscription no longer exists
        TimeUnit.SECONDS.sleep(15);

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
        }
        assertEquals(statusCode, SC_NOT_FOUND, "The expected HTTP 404 was not returned.");
        subscriptionIdsForCleanup.remove(subscriptionId);
    }

    private void modifyCmEventsLicensingPermission(Permission permission) {
        StubbedLicensingServiceSpiBean.setPermissionToBeReturnedByStub(StubbedLicensingServiceSpiBean.LICENSE_KEY_5MHzSC, permission);
        StubbedLicensingServiceSpiBean.setPermissionToBeReturnedByStub(StubbedLicensingServiceSpiBean.LICENSE_KEY_CELL_CARRIER, permission);
        StubbedLicensingServiceSpiBean.setPermissionToBeReturnedByStub(StubbedLicensingServiceSpiBean.LICENSE_KEY_ONOFFSCOPE_CORE, permission);
        StubbedLicensingServiceSpiBean.setPermissionToBeReturnedByStub(StubbedLicensingServiceSpiBean.LICENSE_KEY_ONOFFSCOPE_RADIO, permission);
        StubbedLicensingServiceSpiBean.setPermissionToBeReturnedByStub(StubbedLicensingServiceSpiBean.LICENSE_KEY_ONOFFSCOPE_TRANSPORT, permission);
    }

    private ComEcimNodeNotification createComEcimNodeNotification() {
        String dn = "SubNetwork=sub1,ManagedElement=LTE04dg2ERBS00035";
        String time = new Date().toString();
        ComEcimNodeNotification comEcimNodeNotification = new ComEcimNodeNotification(dn, 3L, time, 1L, false);
        Map<String, Object> updateAttributesCreate = new HashMap<>();
        updateAttributesCreate.put("userLabel", "47");
        comEcimNodeNotification.setUpdateAttributes(updateAttributesCreate);
        comEcimNodeNotification.setDn(dn);
        return comEcimNodeNotification;
    }

    private void deleteExistingWiremockServerRequests() throws IOException, URISyntaxException {
        // Deletion of requests sent to wiremock to simplify verification for later tests
        CloseableHttpClient httpclient = HttpClients.createDefault();
        URI deleteWiremockReqUri = new URIBuilder().setScheme(HTTP).setHost("wiremock").setPort(8181).setPath("/__admin/requests").build();
        final HttpDelete deleteRequests = new HttpDelete(deleteWiremockReqUri);
        deleteRequests.setHeader(CONTENT_TYPE, APPLICATION_JSON);

        try (CloseableHttpResponse response = httpclient.execute(deleteRequests)) {
            int actualStatusCode = response.getStatusLine().getStatusCode();
            assertEquals(actualStatusCode, 200);
        }
    }

    private void resetScenarios() throws IOException, URISyntaxException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        URI postWiremockReqUri = new URIBuilder().setScheme(HTTP).setHost("wiremock").setPort(8181).setPath("/__admin/scenarios/reset").build();
        final HttpPost postRequests = new HttpPost(postWiremockReqUri);
        postRequests.setHeader(CONTENT_TYPE, APPLICATION_JSON);

        try (CloseableHttpResponse response = httpclient.execute(postRequests)) {
            int actualStatusCode = response.getStatusLine().getStatusCode();
            assertEquals(actualStatusCode, 200);
        }
    }

    private String extractNotificationTypeFromWiremockResponse(String responseMessage) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode wiremockResponseNode = mapper.readTree(responseMessage);
        String vesEvent = wiremockResponseNode.get("requests").get(0).get("body").asText();
        logger.debug("VES Event Received: {}", vesEvent);

        return mapper.readTree(vesEvent).get("event").get("stndDefinedFields").get("data").get("notificationType").asText();
    }

    private void cleanUpSubscriptions() throws IOException, URISyntaxException {
        for(String subscriptionId: subscriptionIdsForCleanup){
            logger.debug("cleanUpSubscriptions for Subscription ID: {}", subscriptionId);
            int actualStatusCode;
            // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/{subscriptionId}
            URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS + "/" + subscriptionId).build();
            CloseableHttpClient httpclient = HttpClients.createDefault();
            final HttpDelete eventsDeleteRequest = new HttpDelete(uri);
            eventsDeleteRequest.setHeader(CONTEXT_USER_ID_KEY, CM_EVENTS_NBI_ADM_USER);
            logger.debug("Sending RESTful HTTP Delete Request: [{}]", eventsDeleteRequest.getURI());

            try (CloseableHttpResponse response = httpclient.execute(eventsDeleteRequest)) {
                actualStatusCode = response.getStatusLine().getStatusCode();
            }
            // Check HTTP STATUS 204 = NO_CONTENT.
            assertEquals(actualStatusCode, SC_NO_CONTENT, "The expected HTTP 204 was not returned.");
        }
        subscriptionIdsForCleanup.clear();
    }

    private String executeWiremockRequest(HttpPost wireMockVerificationString) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try (CloseableHttpResponse response = httpclient.execute(wireMockVerificationString)) {
            int actualStatusCode = response.getStatusLine().getStatusCode();
            logger.debug("Wiremock ResponseMessage Status Code: {}", actualStatusCode);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, UTF_8);
        }
    }

    private String postSubscriptionForEventHandling(final String subscription) throws IOException, URISyntaxException {
        int actualStatusCode;
        final StringEntity JSONNtfSubscriptionControl = new StringEntity(subscription);

        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        final HttpPost httpPostSubscriptionRequest = new HttpPost(uri);
        httpPostSubscriptionRequest.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        httpPostSubscriptionRequest.setHeader(CONTEXT_USER_ID_KEY, CM_EVENTS_NBI_ADM_USER);
        httpPostSubscriptionRequest.setEntity(JSONNtfSubscriptionControl);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        logger.debug("Sending RESTful HTTP POST Request: [{}]", httpPostSubscriptionRequest.getRequestLine().getUri());
        String responseMessage;
        try (CloseableHttpResponse response = httpclient.execute(httpPostSubscriptionRequest)) {
            actualStatusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            responseMessage = EntityUtils.toString(entity, UTF_8);
            logger.debug("Response ResponseMessage: {}", responseMessage);
        }
        // Check HTTP STATUS 201 = Created.
        logger.info("Received RESTful HTTP Response Code: [{}]", actualStatusCode);
        assertEquals(actualStatusCode, SC_CREATED, "The expected HTTP 200 was not returned.\"");

        final ObjectMapper mapper = new ObjectMapper();
        createdId = mapper.readTree(responseMessage).get("ntfSubscriptionControl").get("id").textValue();
        subscriptionIdsForCleanup.add(createdId);

        return responseMessage;
    }

    private void  createSubscriptionsForViewAll() throws IOException, URISyntaxException {
        final String subscriptionOne = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub1\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"888\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        final String subscriptionTwo= "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://wiremock:8443/eventListener/v1/sub1\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"889\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        postSubscriptionForEventHandling(subscriptionOne);
        postSubscriptionForEventHandling(subscriptionTwo);
    }



}
