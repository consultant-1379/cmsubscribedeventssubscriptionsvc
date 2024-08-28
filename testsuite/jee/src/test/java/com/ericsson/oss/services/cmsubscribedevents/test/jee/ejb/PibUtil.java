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

package com.ericsson.oss.services.cmsubscribedevents.test.jee.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;

public class PibUtil {
    private static final String UPDATE_REQUEST = "http://{0}:8080/pib/configurationService/updateConfigParameterValue?paramName={1}&paramValue={2}";
    private static final String READ_REQUEST = "http://{0}:8080/pib/configurationService/getConfigParameter?paramName={1}";
    private static final String USERNAME = "pibUser";
    private static final String PASSWORD = "3ric550N*";

    @Inject
    private Logger logger;

    public void updateParameter(final String paramName, final String paramValue) {
        try {
            final String ip = System.getProperty("jboss.host.name");
            final String readUrl = MessageFormat.format(READ_REQUEST, ip, paramName);
            final String updateUrl = MessageFormat.format(UPDATE_REQUEST, ip, paramName, paramValue);
            final String oldValue = executeRequest(readUrl);
            executeRequest(updateUrl);
            logger.info("Updated configuration parameter: [{}], old value: [{}], new value: [{}]", paramName, oldValue, paramValue);
            TimeUnit.SECONDS.sleep(2);
        } catch (final Exception ex) {
            logger.error("Exception updating PIB parameter: " + paramName, ex);
            Thread.currentThread().interrupt();
        }
    }

    private String executeRequest(final String url) throws IOException {

        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
            new AuthScope(AuthScope.ANY),
            new UsernamePasswordCredentials(USERNAME, PASSWORD));
        final HttpGet httpget = new HttpGet(url);
        final CloseableHttpClient httpclient = HttpClients.custom()
            .setDefaultCredentialsProvider(credsProvider)
            .build();

        final HttpResponse response = httpclient.execute(httpget);
        final InputStream content = response.getEntity().getContent();
        if (content != null) {
            final Scanner scanner = new Scanner(content);
            scanner.useDelimiter("\\A");
            final String parameterDescription = scanner.hasNext() ? scanner.next() : null;
            logger.info("Response: [{}] for request: [{}]", parameterDescription, url);
            scanner.close();
            content.close();
            return retrieveParameterValue(parameterDescription);
        } else {
            return null;
        }
    }

    private static String retrieveParameterValue(final String parameterDescription) {
        String result = "";
        final Matcher matcher = Pattern.compile(".*value=(.*?),.*").matcher(parameterDescription);
        if (matcher.matches() && (1 <= matcher.groupCount())) {
            result = matcher.group(1);
        }
        return result;
    }
}