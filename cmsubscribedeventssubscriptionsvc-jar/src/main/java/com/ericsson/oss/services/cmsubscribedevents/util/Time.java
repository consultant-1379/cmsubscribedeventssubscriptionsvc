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

package com.ericsson.oss.services.cmsubscribedevents.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Retrieves System time.
 */
public final class Time {

    private Time() {
    }

    /**
     * Returns UTC system time in specified format.
     *
     * @param format
     *     time date format pattern e.g. "yyyy-MM-dd HH:mm:ss,sss".
     * @return UTC system time in specified format.
     */
    public static String getSystemTime(final String format) {
        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(format);
        final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);

        return dateFormat.format(localDateTime);
    }

}