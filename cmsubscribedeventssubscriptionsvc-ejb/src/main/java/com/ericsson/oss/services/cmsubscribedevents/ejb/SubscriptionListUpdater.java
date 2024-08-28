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
package com.ericsson.oss.services.cmsubscribedevents.ejb;

import static java.util.concurrent.TimeUnit.MINUTES;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionService;
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Startup
@Singleton
public class SubscriptionListUpdater {

    @Inject
    private Logger logger;

    @EServiceRef
    private SubscriptionService subscriptionService;

    @Resource
    private TimerService timerService;

    List<Subscription> allSubscriptions = Collections.emptyList();

    @PostConstruct
    private void init() {
        timerService.createSingleActionTimer(MINUTES.toMillis(1L), new TimerConfig());
    }

    @Timeout
    public void updateSubscription()  {
        try {
            allSubscriptions = new ObjectMapper().readValue(subscriptionService.viewAllSubscriptions(), new TypeReference<ArrayList<Subscription>>() {});
        } catch (final IOException e) {
            logger.warn(String.format("Failed to update subscriptions %s", e.getMessage()));
        }
    }

    public List<Subscription> getUpdatedSubscriptionList() {
        return allSubscriptions;
    }

}
