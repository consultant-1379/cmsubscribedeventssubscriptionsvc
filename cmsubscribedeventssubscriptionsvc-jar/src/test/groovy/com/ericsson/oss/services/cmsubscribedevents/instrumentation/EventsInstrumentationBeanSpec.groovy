/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 *  COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cmsubscribedevents.instrumentation

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import spock.lang.Specification

class EventsInstrumentationBeanSpec extends Specification {

    @ObjectUnderTest
    EventsInstrumentationBean eventsInstrumentationBean

    def 'When the counters are incremented, metrics are updated with incremented value'() {
        given: 'the mbean instance is initialed'
            eventsInstrumentationBean = new EventsInstrumentationBean()

        when: 'increment methods for metrics are called'
            for (int i = 0; i < eventCount; i++) {
                eventsInstrumentationBean.incrementCreateEventsReceived()
                eventsInstrumentationBean.incrementDeleteEventsReceived()
                eventsInstrumentationBean.incrementUpdateEventsReceived()
                eventsInstrumentationBean.incrementTotalSuccessfulHeartbeatRequests()
                eventsInstrumentationBean.incrementTotalFailedHeartbeatRequests()
                eventsInstrumentationBean.incrementTotalVesEventsPushedSuccessfully()
                eventsInstrumentationBean.incrementTotalVesEventsPushedError()
                eventsInstrumentationBean.incrementTotalVesEventsPushedCancelled()
                eventsInstrumentationBean.incrementVesEventsToBePushedNotifyMoiCreation()
                eventsInstrumentationBean.incrementVesEventsToBePushedNotifyMoiDeletion()
                eventsInstrumentationBean.incrementVesEventsToBePushedNotifyMoiAvc()
                eventsInstrumentationBean.incrementVesEventsToBePushedNotifyMoiChangesCreate()
                eventsInstrumentationBean.incrementVesEventsToBePushedNotifyMoiChangesDelete()
                eventsInstrumentationBean.incrementVesEventsToBePushedNotifyMoiChangesReplace()
                eventsInstrumentationBean.incrementSuccessfulContinuousHeartbeatRequests()
                eventsInstrumentationBean.incrementFailedContinuousHeartbeatRequests()
            }

        then: 'the metrics are updated with incremented values'
            eventsInstrumentationBean.getCreateEventsReceived() == result
            eventsInstrumentationBean.getDeleteEventsReceived() == result
            eventsInstrumentationBean.getUpdateEventsReceived() == result
            eventsInstrumentationBean.getTotalEventsReceived() == result * 3
            eventsInstrumentationBean.getTotalSuccessfulHeartbeatRequests() == result
            eventsInstrumentationBean.getTotalFailedHeartbeatRequests() == result
            eventsInstrumentationBean.getTotalVesEventsPushedSuccessfully() == result
            eventsInstrumentationBean.getTotalVesEventsPushedError() == result
            eventsInstrumentationBean.getTotalVesEventsPushedCancelled() == result
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiCreation() == result
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiDeletion() == result
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiAvc() == result
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiChangesCreate() == result
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiChangesDelete() == result
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiChangesReplace() == result
            eventsInstrumentationBean.getTotalVesEventsToBePushed() == result * 6
            eventsInstrumentationBean.getSuccessfulContinuousHeartbeatRequests() == result
            eventsInstrumentationBean.getTotalFailedHeartbeatRequests() == result

        where:
            eventCount | result
            1          | 1
            2          | 2
            3          | 3
    }
}