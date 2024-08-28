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
 *
 */

package com.ericsson.oss.services.cmsubscribedevents.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains methods to convert notification content into required format for VES notification conversion.
 */
public final class DataConverter {

    private static final Pattern NETWORK_ELEMENT_OR_MECONTEXT_NAME_PATTERN = Pattern.compile("(?:NetworkElement|MeContext)=([^,=]*)");
    private static final Pattern MANAGED_ELEMENT_NAME_PATTERN = Pattern.compile("ManagedElement=([^,=]*)");

    private static final String ID = "[A-Za-z0-9-._:/?%&!#~@$^|\\s()\\[\\]*\\\\]+?";

    private static final Pattern NRM_TCIM_LINK_MO_PATTERN = Pattern.compile("Network=1(?:,Link=(" + ID + "))+$");

    private DataConverter() {
    }

    /**
     * Retrieves the target node name from an FDN.
     *
     * @param fdnFieldToExtractTargetFrom
     *     - Contains FDN field to extract target from.
     * @return target name
     */
    public static String retrieveTargetName(String fdnFieldToExtractTargetFrom) {
        if (fdnFieldToExtractTargetFrom == null) {
            fdnFieldToExtractTargetFrom = "";
        }
        final ArrayList<Pattern> patterns = new ArrayList<>();
        patterns.add(NETWORK_ELEMENT_OR_MECONTEXT_NAME_PATTERN);
        patterns.add(MANAGED_ELEMENT_NAME_PATTERN);
        patterns.add(NRM_TCIM_LINK_MO_PATTERN);


        for (final Pattern pattern : patterns) {
            final int groupToReturn = 1;
            final Matcher matcher = pattern.matcher(fdnFieldToExtractTargetFrom);
            if (matcher.find()) {
                return matcher.group(groupToReturn);
            }
        }
        throw new IllegalArgumentException(
            "FDN field of the event doesn't contain a valid name - expected one of NetworkElement|MeContext|ManagedElement|Link, FDN - "
                + fdnFieldToExtractTargetFrom);
    }

}