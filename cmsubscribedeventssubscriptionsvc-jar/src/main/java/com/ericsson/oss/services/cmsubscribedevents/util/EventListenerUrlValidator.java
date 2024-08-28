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

import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Validates URL.
 */
public class EventListenerUrlValidator {

    /**
     * Verifies if inputted URL is a valid URL and complies with eventListener URL naming convention.
     *
     * @param eventListenerUrl
     *     - EventListener URL.
     * @throws MalformedURLException
     *     - Thrown if the URL is not valid or does not contain /eventListener/v{apiVersion}.
     */
    public void validateEventListenerUrl(final String eventListenerUrl) throws MalformedURLException {
        final Pattern eventListenerApiUrlPattern = Pattern.compile("/eventListener/v[^/*]");
        final Matcher matcher = eventListenerApiUrlPattern.matcher(eventListenerUrl);
        final UrlValidator validator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
        if (!validator.isValid(eventListenerUrl) || !matcher.find()) {
            throw new MalformedURLException();
        }
    }

}
