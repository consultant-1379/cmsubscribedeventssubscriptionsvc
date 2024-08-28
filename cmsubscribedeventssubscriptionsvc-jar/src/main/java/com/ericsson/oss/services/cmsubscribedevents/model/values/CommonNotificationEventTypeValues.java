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

package com.ericsson.oss.services.cmsubscribedevents.model.values;

import com.ericsson.oss.services.cmsubscribedevents.model.ves.CommonNotifyMoiData;

/**
 * Interface definition for attributes that are common for all notification types but change values per notification type.
 */
public interface CommonNotificationEventTypeValues {

    String getStndDefinedNamespace();

    String getDomain();

    String getEventName();

    String getReportingEntityName();

    String getEventIdPrefix();

    String getSourceName();

    Long getSequenceNumber();

    CommonNotifyMoiData getNotifyMoiData();

}