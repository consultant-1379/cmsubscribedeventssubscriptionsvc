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
package com.ericsson.oss.services.cmsubscribedevents.nbi;

import javax.inject.Inject;

import com.ericsson.oss.mediation.network.api.notifications.NodeNotification;
import com.ericsson.oss.services.cmsubscribedevents.builder.VesEventBuilder;
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType;

/**
 * Sends VES notifications via HTTP for NodeNotifications.
 *
 */
public class CppEventNotificationProducer extends EventNotificationProducer<NodeNotification> {

    @Inject
    VesEventBuilder vesEventBuilder;

    public String buildNotificationTypeModel(final OperationType operationType, final String subscriptionNotificationType,
            final NodeNotification event) {
        return vesEventBuilder.buildCppVesEventString(operationType, subscriptionNotificationType, event);
    }
}
