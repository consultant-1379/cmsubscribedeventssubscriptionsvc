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

public class NtfSubscriptionControl {

    private int id;
    private String notificationRecipientAddress;
    private String[] notificationTypes;
    private String notificationFilter;
    private Scope scope;
    private String objectClass;
    private String objectInstance;

    public NtfSubscriptionControl() {}

    public NtfSubscriptionControl(final int id,final  String notificationRecipientAddress,final  String[] notificationTypes,final  String objectInstance,final  String objectClass, final String notificationFilter,final  Scope scope) {
        this.id = id;
        this.notificationRecipientAddress = notificationRecipientAddress;
        this.notificationTypes = notificationTypes;
        this.objectInstance = objectInstance;
        this.objectClass = objectClass;
        this.notificationFilter = notificationFilter;
        this.scope = scope;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getNotificationRecipientAddress() {
        return notificationRecipientAddress;
    }

    public void setNotificationRecipientAddress(final String notificationRecipientAddress) {
        this.notificationRecipientAddress = notificationRecipientAddress;
    }

    public String[] getNotificationTypes() {
        return notificationTypes;
    }

    public void setNotificationType(final String[] notificationTypes) {
        this.notificationTypes = notificationTypes;
    }

    public String getNotificationFilter() {
        return notificationFilter;
    }

    public void setNotificationFilter(final String notificationFilter) {
        this.notificationFilter = notificationFilter;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(final Scope scope) {
        this.scope = scope;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(final String objectClass) {
        this.objectClass = objectClass;
    }

    public String getObjectInstance() {
        return objectInstance;
    }

    public void setObjectInstance(final String objectInstance) {
        this.objectInstance = objectInstance;
    }

}