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

import com.ericsson.oss.mediation.notifications.ComEcimNodeNotification;
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean;
import com.ericsson.oss.services.cmsubscribedevents.api.SubscribedChangeEventsProcessor;
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType;
import com.ericsson.oss.services.cmsubscribedevents.nbi.ComEcimEventNotificationProducer;
import com.ericsson.oss.services.cmsubscribedevents.nbi.EventNotificationProducer;
import org.slf4j.Logger;
import javax.ejb.Singleton;
import javax.inject.Inject;

/**
 * Responsible for processing incoming Com Ecim events sent by the CM Subscribed Events DC Listener.
 */
@Singleton
public class ComEcimEventChangeProcessor extends AbstractEventChangeProcessor<ComEcimNodeNotification> implements SubscribedChangeEventsProcessor<ComEcimNodeNotification> {

    @Inject
    private Logger logger;

    @Inject
    private ComEcimEventNotificationProducer comEcimEventNotificationSender;

    @Inject
    EventsInstrumentationBean eventsInstrumentationBean;

    protected OperationType extractEventOperationType(final ComEcimNodeNotification comEcimNotification) {
        switch (comEcimNotification.getAction()) {
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
                logger.debug("Unsupported COM ECIM operation type: {}", comEcimNotification);
                return null;
        }
    }

    @Override
    protected String extractEventHref(final ComEcimNodeNotification comEcimNodeNotification) {
        return comEcimNodeNotification.getDn();
    }

    protected EventNotificationProducer<ComEcimNodeNotification> getNotificationProducer() {
        return comEcimEventNotificationSender;
    }
}