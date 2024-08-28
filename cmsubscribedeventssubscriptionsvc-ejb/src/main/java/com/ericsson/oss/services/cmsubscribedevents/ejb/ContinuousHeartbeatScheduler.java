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

import com.ericsson.oss.services.cmsubscribedevents.api.MembershipListener;
import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.SubscriptionHeartbeatException;
import com.ericsson.oss.services.cmsubscribedevents.heartbeat.ContinuousHeartbeatFailureMap;
import com.ericsson.oss.services.cmsubscribedevents.heartbeat.ContinuousHeartbeatFailureSubscriptionDeleter;
import com.ericsson.oss.services.cmsubscribedevents.heartbeat.HeartbeatValidator;
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean;
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription;

import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.inject.Inject;

import java.io.IOException;
import java.util.List;

/**
 * ContinuousHeartbeatScheduler will schedule to execute continuous heartbeat to all available subscriptions' notificationRecipientAddress.
 */
@Singleton
@Startup
public class ContinuousHeartbeatScheduler {

    private static final String CONTINUOUS_HEARTBEAT_TIMER_INFO = "ContinuousHeartbeatTimer";

    @Inject
    private Logger logger;

    @Resource
    private TimerService timerService;

    @Inject
    private HeartbeatIntervalChangeListener heartbeatIntervalChangeListener;

    @Inject
    private HeartbeatValidator heartbeatValidator;

    @Inject
    private EventsInstrumentationBean eventsInstrumentationBean;

    @Inject
    MembershipListener membershipListener;

    @Inject
    SubscriptionListUpdater subscriptionListUpdater;

    @Inject
    ContinuousHeartbeatFailureMap continuousHeartbeatFailureMap;

    @Inject
    ContinuousHeartbeatFailureSubscriptionDeleter continuousHeartbeatFailureSubscriptionDeleter;

    @PostConstruct
    public void startContinuousHeartbeatTimer() {
        final int heartbeatIntervalDuration = heartbeatIntervalChangeListener.getCmSubscribedEventsHeartbeatInterval();
        if (heartbeatIntervalDuration > 0) {
            logger.info("Starting timer to schedule continuous heartbeat");
            timerService.createTimer(heartbeatIntervalDuration * 1000L, heartbeatIntervalDuration * 1000L, CONTINUOUS_HEARTBEAT_TIMER_INFO);
        } else {
            logger.info("Not scheduling continuous heartbeat since 'cmSubscribedEventsHeartbeatInterval' pib parameter is set to '{}'", heartbeatIntervalDuration);
        }
    }



    @Timeout
    public void continuousHeartbeatSchedulerTimeout(final Timer timer) throws IOException {
        if (membershipListener.isMaster()) {
            logger.debug("Timer with info '{}' timed out and continuousHeartbeatSchedulerTimeout invoked", timer.getInfo());
            final List<Subscription> allSubscriptions = subscriptionListUpdater.getUpdatedSubscriptionList();
            for (final Subscription subscription : allSubscriptions) {
                final String notificationRecipientAddress = subscription.getNtfSubscriptionControl().getNotificationRecipientAddress();
                logger.debug("Executing continuous heartbeat for subscription with subscription id {} towards notificationRecipientAddress url {}",
                    subscription.getNtfSubscriptionControl().getId(), notificationRecipientAddress);
                try  {
                    if (heartbeatValidator.executeHeartbeatRequest(notificationRecipientAddress)) {
                        eventsInstrumentationBean.incrementSuccessfulContinuousHeartbeatRequests();
                        continuousHeartbeatFailureMap.clearSubscriptionById(subscription.getNtfSubscriptionControl().getId());
                    } else {
                        handleHeartbeatFailure(subscription);
                    }
                } catch (final SubscriptionHeartbeatException e) {
                    logger.error(String.format("Continuous heartbeat failure for subscription %s %s", subscription, e.getMessage()));
                    handleHeartbeatFailure(subscription);
                }
            }
        }
    }

    private void handleHeartbeatFailure(final Subscription subscription) {
        eventsInstrumentationBean.incrementFailedContinuousHeartbeatRequests();
        if (continuousHeartbeatFailureMap.shouldSubscriptionBeDeleted(subscription.getNtfSubscriptionControl().getId())
            && (continuousHeartbeatFailureSubscriptionDeleter.deleteSubscription(subscription))) {
            continuousHeartbeatFailureMap.clearSubscriptionById(subscription.getNtfSubscriptionControl().getId());
        }
    }

    /**
     * Change the continuous heartbeat scheduler interval by cancelling the existing timer and creating a new timer with current heartbeatScheduler value.
     *
     */
    public void changeInterval() {
        logger.info("cmSubscribedEventsHeartbeatInterval is changed. Cancelling the current timer and new timer with new interval is created.");

        for(Timer timer: timerService.getTimers()) {
            logger.info("Cancelling current timer for continuous heartbeat schedule");
            if (CONTINUOUS_HEARTBEAT_TIMER_INFO.equals(timer.getInfo())){
                timer.cancel();
            }
        }
        startContinuousHeartbeatTimer();
    }

}
