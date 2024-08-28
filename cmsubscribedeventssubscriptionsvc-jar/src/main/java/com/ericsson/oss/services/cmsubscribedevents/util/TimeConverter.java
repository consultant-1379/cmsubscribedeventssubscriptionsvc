/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2023
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cmsubscribedevents.util;

import static com.ericsson.oss.services.cmsubscribedevents.constants.SubscriptionConstants.VES_EVENT_TIMESTAMP_FORMAT;

import org.slf4j.Logger;
import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Contains methods to convert timestamps.
 */
public class TimeConverter {

    @Inject
    Logger logger;

    /**
     * Converts notification timestamp into the expected format that can be used in VESNotifications. If timestamp is not in expected format UTC
     * system time is returned in VESNotification format and a warning is outputted.
     *
     * @param timestamp
     *     The timestamp of notification provided by node notification event. Expected formats:
     *     <ul>
     *     <li>"E MMM dd HH:mm:ss z yyyy"
     *     <li>"yyyy-MM-dd'T'HH:mm:ssZ"
     *     <li>"yyyy-MM-dd'T'HH:mm:ss"
     *     <li>"yyyy-MM-dd'T'HH:mm:ss'Z'"
     *     </ul>
     * @return String containing timestamp in a VESNotification format or current UTC system time if unable to parse inputted notification.
     */

    public String covertToUTC(final String timestamp) {

        if (!checkIsConversionRequired(timestamp)) {
            return timestamp;
        }

        final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .optionalStart()
            .appendPattern("E MMM dd HH:mm:ss z yyyy")
            .optionalEnd()
            .optionalStart()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart()
            .appendPattern("[XXX][X]")
            .optionalEnd()
            .optionalStart()
            .appendPattern("[:][X][+HH:mm]")
            .optionalEnd()
            .optionalStart()
            .appendLiteral('Z')
            .optionalEnd()
            .toFormatter();

        ZonedDateTime dateTime;

        try {
            if (Pattern.compile(".*[-+]\\d{2}(:?\\d{2})?$").matcher(timestamp).matches()) {
                final ZonedDateTime zonedDateTime = ZonedDateTime.parse(timestamp, formatter);
                final Instant instant = zonedDateTime.toInstant();
                dateTime = instant.atZone(ZoneOffset.UTC);
            } else {
                dateTime = LocalDateTime.parse(timestamp, formatter).atZone(ZoneOffset.UTC);
            }
        } catch (final DateTimeParseException exception) {
            logger.warn("Unable to parse the event creation time {}, using the current time instead.", timestamp);
            return Time.getSystemTime(VES_EVENT_TIMESTAMP_FORMAT);
        }

        final ZonedDateTime utcDateTime = dateTime.withZoneSameInstant(ZoneOffset.UTC);

        return utcDateTime.format(DateTimeFormatter.ofPattern(VES_EVENT_TIMESTAMP_FORMAT));
    }

    private boolean checkIsConversionRequired(final String timestampString) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(VES_EVENT_TIMESTAMP_FORMAT);
        try {
            final LocalDateTime timestamp = LocalDateTime.parse(timestampString, formatter);
            final String formattedTimestamp = timestamp.format(formatter);
            return !formattedTimestamp.equals(timestampString);
        } catch (DateTimeParseException e) {
            return true;
        }
    }

}
