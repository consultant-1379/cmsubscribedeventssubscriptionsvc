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

import static com.ericsson.oss.services.cmsubscribedevents.api.constants.SignalMessageConstants.SUBSCRIPTION_UPDATE;

import com.ericsson.oss.itpf.sdk.cluster.classic.ServiceClusterBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Joins the CmSubscribedEventsCluster on startup and is responsible for sending messages to the cluster
 * for any Cm Subscribed Events DB changes
 *
 */
@Startup
@Singleton
public class SubscriptionDbChangedMessageSender {

    @Inject
    private Logger log;

    @Inject
    SubscriptionDbChangedMessageListener subscriptionDbChangedMessageListener;

    private ServiceClusterBean serviceClusterBean;
    private String clusterName = "CmSubscribedEventsCluster";

    @PostConstruct
    void initializeCluster() {
        try {
            serviceClusterBean = new ServiceClusterBean(clusterName);
            joinCluster();
        } catch (final Exception exception) {
            log.error("Error initializing SignalMessageSender on cluster [{}] exception [{}].", clusterName, exception.getMessage());
        }
        log.info("Initialized serviceClusterBean on cluster [{}] successfully.", clusterName);
    }

    @PreDestroy
    void leaveClusterService() {
        log.info("PreDestroy method called to leave cluster");
        leaveCluster();
    }

    private void joinCluster() {
        boolean joinedCluster = false;
        try {
            joinedCluster = serviceClusterBean.joinCluster(subscriptionDbChangedMessageListener);
        } catch (final Exception exception) {
            log.error("Error joining cluster [{}] exception [{}].", clusterName, exception.getMessage());
        }
        if (joinedCluster) {
            log.info("Joined cluster [{}] successfully.", clusterName);
        } else {
            log.error("Failed to join cluster [{}].", clusterName);
        }
    }

    private void leaveCluster() {
        if (serviceClusterBean.isClusterMember()) {
            log.info("serviceClusterBean is leaving cluster [{}]", clusterName);
            serviceClusterBean.leaveCluster();
        }
    }

    /**
     * Sends the subscription update message to all members of the CmSubscribedEvents Cluster
     */
    public void sendSignalMessage() {
        serviceClusterBean.send(SUBSCRIPTION_UPDATE);
    }

}
