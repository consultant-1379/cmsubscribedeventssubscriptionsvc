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
package com.ericsson.oss.services.cmsubscribedevents.heartbeat;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription;
import com.ericsson.oss.services.fm.internalalarm.api.InternalAlarmRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;

public class InternalFmAlarmSender {

    private static final String INTERNAL_ALARM_SERVICE_PATH_PROPERTY_NAME = "INTERNAL_ALARM_SERVICE_PATH";
    private static final String INTERNAL_ALARM_SERVICE_FULL_URL_PROPERTY_NAME = "INTERNAL_ALARM_SERVICE_FULL_URL";
    private static final String INTERNAL_ALARM_SERVICE_PATH = "/internal-alarm-service/internalalarm/internalalarmservice/translate"; //NOSONAR
    private static final String INTERNAL_ALARM_SERVICE_PROTOCOL = "http";
    private static final String INTERNAL_ALARM_FDN = "ManagementSystem=ENM";
    private static final String INTERNAL_ALARM_SERVICE_HOSTNAME = "internalalarm-service";
    private static final int INTERNAL_ALARM_SERVICE_PORT = 8080;
    private static final String HOST = "host";

    @Inject
    private Logger logger;

    public void sendFmAlarmForDeleteSubscription(final Subscription subscription) throws JsonProcessingException {

        CloseableHttpClient client = HttpClients.createDefault();
        try {
            final String problemText = String.format("The endpoint identified by notificationRecipientAddress in Subscription [%s] cannot be reached. Subscription %d has been deleted", 
                    new ObjectMapper().writeValueAsString(subscription), subscription.getNtfSubscriptionControl().getId());
            final HttpPost httpPost = new HttpPost(getFullUrl());
            final InternalAlarmRequest internalAlarmRequest = generateAlarmRequest(problemText);
            final String jsonRequest = getJsonString(internalAlarmRequest);
            logger.debug("Json request to raise internal alarm {}", jsonRequest);
            final EntityBuilder entityBuilder = EntityBuilder.create();
            entityBuilder.setContentType(ContentType.APPLICATION_JSON);
            entityBuilder.setText(jsonRequest);
            httpPost.setHeader(HOST, INTERNAL_ALARM_SERVICE_HOSTNAME);
            final HttpEntity entity = entityBuilder.build();
            httpPost.setEntity(entity);
            logger.info("Raising an internal alarm with Problem Text \"{}\"  ", problemText);
            final HttpResponse response = client.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                logger.info("Alarm request with Problem Text \"{}\" processed successfully", problemText);
            } else {
                logger.info("Alarm request with Problem Text \"{}\" failed to process", problemText);
            }
        } catch (Exception exception) {
            logger.error("Failed to raise internal alarm {}", exception.getMessage());
        } finally {
            try {
                client.close();
            } catch (Exception exception) {
                logger.error("Failed to close HttpClient object {}", exception.getMessage());
            }
        }

    }

    final InternalAlarmRequest generateAlarmRequest(final String problemText) {
        final InternalAlarmRequest internalAlarmRequest = new InternalAlarmRequest();
        internalAlarmRequest.setEventType("Continuous Heartbeat");
        internalAlarmRequest.setProbableCause("Connection Establishment Error");
        internalAlarmRequest.setSpecificProblem("CM Subscribed Events Continuous Heartbeat Failure");
        internalAlarmRequest.setPerceivedSeverity("WARNING");
        internalAlarmRequest.setRecordType("ERROR_MESSAGE");
        internalAlarmRequest.setManagedObjectInstance(INTERNAL_ALARM_FDN);
        final Map<String, String> additionalAttributes = new HashMap<>();
        additionalAttributes.put("problemText", problemText);
        internalAlarmRequest.setAdditionalAttributes(additionalAttributes);
        return internalAlarmRequest;
    }

    private String getFullUrl() {
        return System.getProperty(INTERNAL_ALARM_SERVICE_FULL_URL_PROPERTY_NAME, getDefaultUrl());
    }

    private String getDefaultUrl() {
        return String.format("%s://%s:%d%s", INTERNAL_ALARM_SERVICE_PROTOCOL, INTERNAL_ALARM_SERVICE_HOSTNAME, INTERNAL_ALARM_SERVICE_PORT, getInternalAlarmServicePath());
    }

    private String getInternalAlarmServicePath() {
        return System.getProperty(INTERNAL_ALARM_SERVICE_PATH_PROPERTY_NAME, INTERNAL_ALARM_SERVICE_PATH);
    }

    private String getJsonString(final InternalAlarmRequest internalAlarmRequest) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(internalAlarmRequest);
    }

}
