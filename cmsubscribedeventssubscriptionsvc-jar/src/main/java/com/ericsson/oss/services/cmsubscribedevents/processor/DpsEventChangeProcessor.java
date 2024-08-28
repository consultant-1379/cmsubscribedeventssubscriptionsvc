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

import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsAttributeChangedEvent;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsDataChangedEvent;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectCreatedEvent;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectDeletedEvent;
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean;
import com.ericsson.oss.services.cmsubscribedevents.api.SubscribedChangeEventsProcessor;
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType;
import com.ericsson.oss.services.cmsubscribedevents.nbi.DpsEventNotificationProducer;
import com.ericsson.oss.services.cmsubscribedevents.nbi.EventNotificationProducer;
import org.slf4j.Logger;
import javax.inject.Inject;
import javax.ejb.Singleton;

/**
 * Responsible for processing incoming DPS events sent by the CM Subscribed Events DC Listener.
 */
@Singleton
public class DpsEventChangeProcessor extends AbstractEventChangeProcessor <DpsDataChangedEvent> implements SubscribedChangeEventsProcessor<DpsDataChangedEvent> {

    @Inject
    private Logger logger;

    @Inject
    private DpsEventNotificationProducer dpsEventNotificationProducer;

    @Inject
    EventsInstrumentationBean eventsInstrumentationBean;

    protected OperationType extractEventOperationType(DpsDataChangedEvent event) {
        if (event instanceof DpsObjectCreatedEvent) {
            eventsInstrumentationBean.incrementCreateEventsReceived();
            return OperationType.CREATE;
        } else if (event instanceof DpsObjectDeletedEvent) {
            eventsInstrumentationBean.incrementDeleteEventsReceived();
            return OperationType.DELETE;
        } else if (event instanceof DpsAttributeChangedEvent) {
            eventsInstrumentationBean.incrementUpdateEventsReceived();
            return OperationType.REPLACE;
        } else {
            logger.debug("Unsupported DPS operation type: {}", event);
            return null;
        }
    }

    @Override
    protected String extractEventHref(final DpsDataChangedEvent dpsDataChangedEvent) {
        return dpsDataChangedEvent.getFdn();
    }

    @Override
    protected EventNotificationProducer<DpsDataChangedEvent> getNotificationProducer() {
        return dpsEventNotificationProducer;
    }

}