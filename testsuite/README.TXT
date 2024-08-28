Overview ----------------------------------------------------------------------------------------
=================================================================================================
Cm Subscribed Events NBI integration test will execute with docker containers

jboss

model-deployment

access-control

postgres

=================================================================================================
Test Execution ----------------------------------------------------------------------------------
=================================================================================================
Integration test integrating all subscribed events components will be executed from cmsubscribedeventssubscriptionsvc repo

As part of root level build, ERICcmsubscribedeventssubscriptionsvc_CXP9042698 rpm will be copied into jboss rpms folder, so ,container has latest code updates from repo.

In testsuite/jee

Build docker images with below

sudo docker-compose build jboss

sudo docker-compose build postgres

sudo docker-compose up --force-recreate


when "jboss-enm is ready" message is seen in console output, tests can be executed from separate tab

mvn clean install -Dintegration.remote

=========================
IMPORTANT----------------
=========================

2 test cases, subscribedEventLicenseRuntimeException and subscribedEventNoValidLicense will always fail on second and subsequent runs
This is due to how cm events checks the license is valid, and once valid, does not check with license management until a certain time during the day again.


After tests have completed, docker containers and images can be cleaned up with following commands

•    docker stop $(docker ps -a -q);
•    docker rm $(docker ps -a -q);
•    docker rmi $(docker images -q);
•    docker run -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/docker:/var/lib/docker --rm martin/docker-cleanup-volumes
•    docker volume ls -qf dangling=true | xargs -r docker volume rm
•    docker ps --filter status=dead --filter status=exited -aq | xargs docker rm -v
•    docker ps -q -a -f status=exited | xargs -n 100 docker rm -v
•    docker images --no-trunc | grep '<none>' | awk '{ print $3 }' | xargs -r docker rmi
•    docker images -q --filter "dangling=true" | xargs -n 100 docker rmi

==================================================================================================
Testing with changes across multiple cm subscribed projects --------------------------------------
==================================================================================================
If a task required changes to the nbi also for example, you can test those snapshot changes with the docker setup in the service project before release

Build nbi changes and rpm

Copy built rpm to folder in service project cmsubscribedeventssubscriptionsvc\testsuite\jee\src\test\docker\jboss\config\build\rpms\nexus

Update rpms.txt in above folder to comment out nbi rpm e.g. #ERICcmsubscribedeventsnbi_CXP9042641

Jboss image should then be rebuilt and test/retest can be executed

====================================================================================================
Troubleshooting ------------------------------------------------------------------------------------
====================================================================================================
To access jboss container and view logs

sudo docker ps

This will list the active containers, the first column will be the container id

Then use sudo docker exec -it <first 2 to 4 characters of container id> bash

To access container and view logs etc like physical enm vm.



