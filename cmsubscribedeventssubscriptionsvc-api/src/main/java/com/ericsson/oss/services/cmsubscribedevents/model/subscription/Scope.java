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

package com.ericsson.oss.services.cmsubscribedevents.model.subscription;

public class Scope {

    private String scopeType;
    private Integer scopeLevel;

    public Scope() {}

    public Scope(final String scopeType,final Integer scopeLevel) {
        this.scopeType = scopeType;
        this.scopeLevel = scopeLevel;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(final String scopeType) {
        this.scopeType = scopeType;
    }

    public Integer getScopeLevel() {
        return scopeLevel;
    }

    public void setScopeLevel(final Integer scopeLevel) {
        this.scopeLevel = scopeLevel;
    }

}