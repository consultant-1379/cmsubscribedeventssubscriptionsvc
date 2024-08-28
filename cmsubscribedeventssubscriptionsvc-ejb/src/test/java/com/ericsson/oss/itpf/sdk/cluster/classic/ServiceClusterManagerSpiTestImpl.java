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
package com.ericsson.oss.itpf.sdk.cluster.classic;

import java.io.Serializable;
import java.util.List;

import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent.ClusterMemberInfo;

/**
 * ServiceClusterManagerSPI for test purposes.
 */
public class ServiceClusterManagerSpiTestImpl implements ServiceClusterManagerSPI {

    @Override
    public <T extends Serializable> boolean joinServiceCluster(final String serviceClusterName, final MembershipChangeListener membershipChangeListener,
            final ClusterMessageListener<T> clusterMessageListener) {
        return true;
    }

    @Override
    public boolean leaveCluster(final String serviceClusterName) {
        return false;
    }

    @Override
    public void send(final String serviceClusterName, final Serializable message) {
    }

    @Override
    public List<ClusterMemberInfo> getAllClusterMembers() {
        return null;
    }

    @Override
    public ClusterMemberInfo getCurrentMaster() {
        return null;
    }
}