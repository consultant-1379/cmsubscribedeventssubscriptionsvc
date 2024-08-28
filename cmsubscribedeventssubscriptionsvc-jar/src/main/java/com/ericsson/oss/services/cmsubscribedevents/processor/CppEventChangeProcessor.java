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

package com.ericsson.oss.services.cmsubscribedevents.processor;

import com.ericsson.oss.mediation.network.api.notifications.NodeNotification;
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean;
import com.ericsson.oss.services.cmsubscribedevents.api.SubscribedChangeEventsProcessor;
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType;
import com.ericsson.oss.services.cmsubscribedevents.nbi.CppEventNotificationProducer;
import com.ericsson.oss.services.cmsubscribedevents.nbi.EventNotificationProducer;
import org.slf4j.Logger;
import javax.ejb.Singleton;
import javax.inject.Inject;

/**
 * Responsible for processing incoming CPP events sent by the CM Subscribed Events DC Listener.
 */
@Singleton
public class CppEventChangeProcessor extends AbstractEventChangeProcessor <NodeNotification> implements SubscribedChangeEventsProcessor<NodeNotification> {

    @Inject
    private Logger logger;

    @Inject
    private CppEventNotificationProducer cppEventNotificationSender;

    @Inject
    EventsInstrumentationBean eventsInstrumentationBean;

    protected OperationType extractEventOperationType(NodeNotification event) {
        switch (event.getAction()) {
            case CREATE:
                eventsInstrumentationBean.incrementCreateEventsReceived();
                return OperationType.CREATE;
            case DELETE:
                eventsInstrumentationBean.incrementDeleteEventsReceived();
                return OperationType.DELETE;
            case UPDATE:
                eventsInstrumentationBean.incrementUpdateEventsReceived();
                return OperationType.REPLACE;
            default:
                logger.debug("Unsupported CPP operation type: {}", event);
                return null;
        }
    }

    @Override
    protected String extractEventHref(final NodeNotification nodeNotification) {
        return nodeNotification.getFdn();
    }

    protected EventNotificationProducer<NodeNotification> getNotificationProducer() {
        return cppEventNotificationSender;
    }

}