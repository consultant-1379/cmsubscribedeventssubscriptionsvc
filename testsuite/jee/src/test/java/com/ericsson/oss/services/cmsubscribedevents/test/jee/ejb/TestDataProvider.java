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
package com.ericsson.oss.services.cmsubscribedevents.test.jee.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.itpf.datalayer.dps.notification.event.AttributeChangeData;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsAttributeChangedEvent;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsDataChangedEvent;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectCreatedEvent;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectDeletedEvent;
import com.ericsson.oss.mediation.network.api.notifications.NodeNotification;
import com.ericsson.oss.mediation.network.api.notifications.NotificationType;
import com.ericsson.oss.mediation.notifications.ComEcimNodeNotification;
import com.ericsson.oss.mediation.notifications.ComEcimNotificationType;

/**
 * Methods for data driven tests will be added here.
 *
 */
public class TestDataProvider {

    private final static String NAMESPACE = "OSS_NE_DEF";
    private final static String NAME = "EUtranCellFDD";
    private final static String VERSION = "2.0.0";
    private final static Long PO_ID = 12345678910L;
    private final static String BUCKET_NAME = "LIVE";
    private final static String DPS_FDN = "NetworkElement=LTE01ERBS00001";

    /**
     * Returns a 2d array containing the Event object and corresponding notification type
     * that would be placed in the VES when moiChanges is not used for notificationType in a subscription
     * There will be an array each ChangeEvent type (comEcim, Cpp, Dps) with each operationType (create, replace, delete)
     */
    public static Object[][] getNotificationEventData() {
        ComEcimNodeNotification comEcimNodeCreateNotification = createComEcimNodeNotification();
        comEcimNodeCreateNotification.setAction(ComEcimNotificationType.CREATE);
        ComEcimNodeNotification comEcimNodeUpdateNotification = createComEcimNodeNotification();
        comEcimNodeUpdateNotification.setAction(ComEcimNotificationType.UPDATE);
        ComEcimNodeNotification comEcimNodeDeleteNotification = createComEcimNodeNotification();
        comEcimNodeDeleteNotification.setAction(ComEcimNotificationType.DELETE);

        NodeNotification createNodeNotification = createNodeNotification();
        createNodeNotification.setAction(NotificationType.CREATE);
        createNodeNotification.setUpdateAttributes(new HashMap<>());
        NodeNotification updateNodeNotification = createNodeNotification();
        updateNodeNotification.setAction(NotificationType.UPDATE);
        updateNodeNotification.setUpdateAttributes(new HashMap<>());
        NodeNotification deleteNodeNotification = createNodeNotification();
        deleteNodeNotification.setAction(NotificationType.DELETE);

        DpsDataChangedEvent dpsDataChangedEvent = new DpsObjectCreatedEvent(NAMESPACE, NAME, VERSION, PO_ID, DPS_FDN, BUCKET_NAME, false, new HashMap<>());
        final Collection<AttributeChangeData> attributeChangeDataCollection = new ArrayList<>();
        final AttributeChangeData attributeChangeData = new AttributeChangeData();
        attributeChangeData.setName("userLabel");
        attributeChangeData.setNewValue("testValue");
        attributeChangeDataCollection.add(attributeChangeData);
        DpsDataChangedEvent dpsDataReplaceEvent = new DpsAttributeChangedEvent(NAMESPACE, NAME, VERSION, PO_ID, DPS_FDN, BUCKET_NAME, attributeChangeDataCollection);
        DpsDataChangedEvent dpsDataDeleteEvent = new DpsObjectDeletedEvent(NAMESPACE, NAME, VERSION, PO_ID, DPS_FDN, BUCKET_NAME, false, new HashMap<>());

        return new Object[][] {{comEcimNodeCreateNotification, "notifyMOICreation"}
            ,{comEcimNodeUpdateNotification, "notifyMOIAttributeValueChanges"}
            ,{comEcimNodeDeleteNotification, "notifyMOIDeletion"}
            ,{createNodeNotification, "notifyMOICreation"}
            ,{updateNodeNotification, "notifyMOIAttributeValueChanges"}
            ,{deleteNodeNotification, "notifyMOIDeletion"}
            ,{dpsDataChangedEvent, "notifyMOICreation"}
            ,{dpsDataReplaceEvent, "notifyMOIAttributeValueChanges"}
            ,{dpsDataDeleteEvent, "notifyMOIDeletion"}};
    }

    private static NodeNotification createNodeNotification() {
        String fdn = "MeContext=LTE02ERBS00006";
        Date time = new Date();
        NodeNotification nodeNotification = new NodeNotification();
        nodeNotification.setFdn(fdn);
        nodeNotification.setCreationTimestamp(time);
        return nodeNotification;
    }

    private static ComEcimNodeNotification createComEcimNodeNotification() {
        String dn = "SubNetwork=sub1,ManagedElement=LTE04dg2ERBS00035";
        String time = new Date().toString();
        ComEcimNodeNotification comEcimNodeNotification = new ComEcimNodeNotification(dn, 3L, time, 1L, false);
        Map<String, Object> updateAttributesCreate = new HashMap<>();
        updateAttributesCreate.put("userLabel", "47");
        comEcimNodeNotification.setUpdateAttributes(updateAttributesCreate);
        comEcimNodeNotification.setDn(dn);
        return comEcimNodeNotification;
    }
}
