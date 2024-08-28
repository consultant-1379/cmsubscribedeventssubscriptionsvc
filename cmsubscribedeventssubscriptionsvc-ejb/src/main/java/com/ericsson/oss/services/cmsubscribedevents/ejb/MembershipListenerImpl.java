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
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent;
import com.ericsson.oss.itpf.sdk.cluster.annotation.ServiceCluster;

@ApplicationScoped
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class MembershipListenerImpl implements MembershipListener {

    @Inject
    private Logger logger;

    volatile boolean master = false;

    // observer method will be invoked by ServiceFramework every time there are membership changes in service cluster named CmSubscribedEventsCluster
    void listenForMembershipChange(@Observes @ServiceCluster("CmSubscribedEventsCluster") final MembershipChangeEvent mce) {
        logger.info("Catch MemberShip Change [ isMaster: {} ]", mce.isMaster());
        setMaster(mce.isMaster());
    }

    @Override
    public boolean isMaster() {
        return master;
    }

    private void setMaster(final boolean master) {
        this.master = master;
    }

}