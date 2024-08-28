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

package com.ericsson.oss.services.cmsubscribedevents.nbi.queue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;

import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean;
import com.ericsson.oss.services.cmsubscribedevents.util.HttpsConfigurator;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Startup EJB that will check the status of the EventQueue, and coordinate "PushEvents"
 * worker threads, to send events to subscribers.
 */
@Local
@EService
@Startup
@Singleton
public class EventQueueSenderEjb implements EventQueueSender{

    @Resource
    private TimerService timerService;

    public static final String CONTENT_TYPE = "Content-type";
    public static final String APPLICATION_JSON = "application/json";

    Logger logger = LoggerFactory.getLogger(EventQueueSenderEjb.class);

    @Inject
    private RetryManager retryManager;

    @Inject
    EventsInstrumentationBean eventsInstrumentationBean;

    @Inject
    HttpsConfigurator httpsConfigurator;

    private List<Future> pushEventsThreads;
    private ExecutorService service;

    @Override
    @PostConstruct
    public void startSending() {
        pushEventsThreads = new ArrayList<>();
        service = Executors.newFixedThreadPool(10);
        pushEventsThreads.add(service.submit(new PushEvents()));
        timerService.createSingleActionTimer(2000, createNonPersistentTimerConfig());
    }

    @Timeout
    public void coordinatePushEventsThreads() {
        logger.debug("Futures size {}", pushEventsThreads.size());

        pushEventsThreads.removeIf(Future::isDone);

        logger.debug("Futures size after removing finished threads {}", pushEventsThreads.size());
        final int totalEventsCount = countEventsSize();
        logger.debug("push map size {}", totalEventsCount);
        int futuresSize = pushEventsThreads.size();
        while( (futuresSize < 10 ) &&
            ( totalEventsCount > (futuresSize * futuresSize * 10)) ) {
            pushEventsThreads.add(service.submit(new PushEvents()));
            futuresSize = pushEventsThreads.size();
        }
        logger.debug("Futures size after adding new threads {}", futuresSize);
        timerService.createSingleActionTimer(2000, createNonPersistentTimerConfig());
    }

    private TimerConfig createNonPersistentTimerConfig() {
        final TimerConfig timerConfig = new TimerConfig();
        timerConfig.setPersistent(false);
        return timerConfig;
    }

    private int countEventsSize() {
        int count = 0;
        for (final String host : EventQueue.getInstance().getPushMap().keySet()) {
            count = count + EventQueue.getInstance().getPushMap().get(host).size();
        }
        return count;
    }

    private class PushEvents implements Runnable {

        @Override
        public void run() {
            boolean allEventsSent = false;

            while (!allEventsSent) {
                allEventsSent = isAllRecipientEventsSent();
            }
        }

        private boolean isAllRecipientEventsSent() {
            boolean allEventsSent = true;
            final Set<String> hosts = EventQueue.getInstance().getPushMap().keySet();

            for (final String host : hosts) {
                allEventsSent  = allEventsSent && allHostEventsSent(host);
            }
            return allEventsSent;
        }

        private boolean allHostEventsSent(final String host) {
            final HttpPost httpPostRequest = new HttpPost(host);
            httpPostRequest.setHeader(CONTENT_TYPE, APPLICATION_JSON);

            logger.trace("STARTing thread execution with queue size {}", EventQueue.getInstance().getPushMap().get(host).size());
            String nextEntity = EventQueue.getInstance().getPushMap().get(host).poll();

            final RetryPolicy retryPolicy = RetryPolicy.builder()
                    .attempts(5)
                    .waitInterval(1, TimeUnit.SECONDS)
                    .retryOn(Exception.class)
                    .build();

            final RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(3000)
                    .setConnectionRequestTimeout(3000)
                    .setSocketTimeout(3000).build();

            CloseableHttpClient httpClient = httpsConfigurator.isSecure(host)
                ? HttpClientBuilder.create().setDefaultRequestConfig(config).setSSLSocketFactory(httpsConfigurator.getSSLConnectionSocketFactory()).build()
                : HttpClientBuilder.create().setDefaultRequestConfig(config).build();

            int sendCount = 0;
            try {
                while ( (nextEntity != null) && (sendCount < 5000)) {
                    httpPostRequest.setEntity(new StringEntity(nextEntity));
                    pushEvent(retryPolicy, httpPostRequest, httpClient);
                    nextEntity = EventQueue.getInstance().getPushMap().get(host).poll();
                    sendCount++;
                }
                logger.debug("Ending thread execution with queue size {}", EventQueue.getInstance().getPushMap().get(host).size());
            } catch (Exception e ) {
                logger.debug("Exception in push thread {} {}", e.getClass(), e.getMessage());
            } finally {
                httpPostRequest.releaseConnection();
            }
           return nextEntity == null;
        }

        private void pushEvent(final RetryPolicy retryPolicy, final HttpPost httpPostRequest, final CloseableHttpClient httpClient) {
            try {
                retryManager.executeCommand(retryPolicy, new RetriableCommand<Void>() {

                    @Override
                    public Void execute(RetryContext retryContext) throws Exception {

                        try (final CloseableHttpResponse response = httpClient.execute(httpPostRequest)) {
                            final int statusCode = response.getStatusLine().getStatusCode();
                            if (statusCode == 204){
                                eventsInstrumentationBean.incrementTotalVesEventsPushedSuccessfully();
                            } else {
                                eventsInstrumentationBean.incrementTotalVesEventsPushedError();
                            }
                            return null;
                        }
                    }
                });

            } catch (Exception e) {
                eventsInstrumentationBean.incrementTotalVesEventsPushedError();
                logger.debug("Error failed to send VES event notification {} {}", e.getClass(), e.getMessage());
            }
        }
    }

}