package com.versionone.integration.ciCommon;

import com.versionone.apiclient.ProxyProvider;
import com.versionone.apiclient.Query;
import com.versionone.apiclient.Services;
import com.versionone.apiclient.V1Connector;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.interfaces.IMetaModel;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public final class V1Config {

    public final String url;
    public final String accessToken;
    public final Pattern pattern;
    public final String referenceField;
    public final Boolean isFullyQualifiedBuildName;

    public final boolean useProxy;
    public final String proxyUrl;
    public final String proxyUsername;
    public final String proxyPassword;

    private Services services;
    private PrintStream logger;

    public V1Config() {
        this("http://localhost/VersionOne/", "");
    }

    /**
     * @param url VersionOne server URL
     * @param accessToken access token for VersionOne
     */
    public V1Config(String url, String accessToken) {
        this(url, accessToken, "[A-Z]{1,2}-[0-9]+", "Number", true);
    }

    /**
     * @param url VersionOne server URL
     * @param accessToken access token for VersionOne
     * @param pattern Regular expression to find VersionOne workitem IDs
     * @param referenceField Name of field used in VCS commit comments to reference VersionOne items
     * @param fullyQualifiedBuildName use full name of build
     */
    public V1Config(String url, String accessToken, String pattern,
                    String referenceField, Boolean fullyQualifiedBuildName) {
        this(url, accessToken, pattern, referenceField, fullyQualifiedBuildName, false, "", "", "");
    }

    /**
     * @param url VersionOne server URL
     * @param accessToken access token for VersionOne
     * @param pattern Regular expression to find VersionOne workitem IDs
     * @param referenceField Name of field used in VCS commit comments to reference VersionOne items
     * @param fullyQualifiedBuildName use full name of build
     * @param useProxy Flag indicating whether we should use proxy server to connect to VersionOne
     * @param proxyUrl Proxy server URL
     * @param proxyUsername Proxy server username
     * @param proxyPassword Proxy server password
     */
    public V1Config(String url, String accessToken, String pattern,
                    String referenceField, Boolean fullyQualifiedBuildName,
                    boolean useProxy, String proxyUrl, String proxyUsername, String proxyPassword) {

        this.url = url;
        this.accessToken = accessToken;
        this.pattern = Pattern.compile(pattern);
        this.referenceField = referenceField;
        isFullyQualifiedBuildName = fullyQualifiedBuildName;

        this.useProxy = useProxy;
        this.proxyUrl = proxyUrl;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }

    /**
     * Set logger so we could use it to populate messages to Jenkins
     * @param logger Logger to be used.
     */
    public void setLogger(PrintStream logger) {
        this.logger = logger;
    }

    /**
     * Get instance of connection to VersionOne with settings from this class.
     *
     * @return connection to VersionOne
     * @throws V1Exception
     */
    public Services getV1Services() throws V1Exception, MalformedURLException {
        if (services == null) {

            V1Connector.IsetProxyOrEndPointOrConnector connectorBuilder = V1Connector
                    .withInstanceUrl(url)
                    .withUserAgentHeader("VersionOne.Integration.Jenkins", "0.1")
                    .withAccessToken(accessToken);

            if(useProxy) {
                try {
                    URI proxyURI = new URI(proxyUrl);
                    ProxyProvider proxyProvider = new ProxyProvider(proxyURI, proxyUsername, proxyPassword);
                    connectorBuilder.withProxy(proxyProvider);
                } catch(URISyntaxException e) {
                    if(logger != null) {
                        logger.printf("Invalid proxy server URI %1$, skipping proxy connection.", proxyUrl);
                    }
                }
            }

            V1Connector connector = connectorBuilder.useOAuthEndpoints().build();

            services = new Services(connector);
        }
        return services;
    }

    @Override
    public String toString() {
        return "Config{" +
                "url='" + url + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", referenceField='" + referenceField + '\'' +
                ", pattern=" + pattern +
                '}';
    }
}