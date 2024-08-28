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
package com.ericsson.oss.itpf.sdk.core.classic;

import java.util.List;

import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionService;
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionServiceImpl;

public class ServiceFinderSpiTestImpl implements ServiceFinderSPI {

    private SubscriptionService subscriptionService = new SubscriptionServiceImpl();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T find(Class arg0, String arg1) {
        return (T) subscriptionService;
    }

    @Override
    public <T> List<T> findAll(Class arg0, String... arg1) {
        return null;
    }

    public void setSubscriptionService(final SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
}