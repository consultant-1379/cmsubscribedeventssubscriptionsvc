FROM armdocker.rnd.ericsson.se/proj_oss_releases/enm/jboss-eap7-dps:2.34.113

COPY standalone-enm.xml /ericsson/3pp/jboss/standalone/configuration/standalone-enm.xml
COPY cmeventskey.jks /ericsson/cmsubscribedevents/data/certs/cmeventskey.jks
COPY cmeventstrust.jks /ericsson/cmsubscribedevents/data/certs/cmeventstrust.jks


COPY create_schema.sql /create_schema.sql

ADD jboss-as.conf /ericsson/3pp/jboss/**

ADD neo4j.properties /ericsson/3pp/jboss/standalone/data/dps/credentials/neo4j.properties

USER root
RUN mkdir -p /ericsson/3pp/jboss/domain/configuration
RUN touch /ericsson/3pp/jboss/domain/configuration/mgmt-groups.properties
RUN touch /ericsson/3pp/jboss/domain/configuration/mgmt-users.properties
RUN chown jboss_user:jboss /ericsson/3pp/jboss/domain/configuration/mgmt-users.properties
RUN chown jboss_user:jboss /ericsson/3pp/jboss/domain/configuration/mgmt-groups.properties
RUN /ericsson/3pp/jboss/bin/add-user.sh -u root -p shroot
