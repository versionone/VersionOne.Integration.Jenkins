package com.versionone.integration.ciCommon;

import com.versionone.apiclient.ProxyProvider;
import com.versionone.apiclient.Services;
import com.versionone.apiclient.V1Connector;
import com.versionone.apiclient.interfaces.IMetaModel;
import com.versionone.apiclient.exceptions.*;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import com.versionone.apiclient.IMetaModel;
import com.versionone.apiclient.MetaException;
import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.ProxySettings;
import com.versionone.om.SDKException;
import com.versionone.om.V1Instance;

public final class V1Config {

    public final String url;
    public final String userName;
    public final String password;
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
        this("http://localhost/VersionOne/", "admin", "admin");
    }

    /**
     * @param url VersionOne server URL
     * @param userName VersionOne user name
     * @param password VersionOne password
     */
    public V1Config(String url, String userName, String password) {
        this(url, userName, password, "[A-Z]{1,2}-[0-9]+", "Number", true);
    }

    /**
     * @param url VersionOne server URL
     * @param userName VersionOne user name
     * @param password VersionOne password
     * @param pattern Regular expression to find VersionOne workitem IDs
     * @param referenceField Name of field used in VCS commit comments to reference VersionOne items
     * @param fullyQualifiedBuildName use full name of build
     */
    public V1Config(String url, String userName, String password, String pattern,
                    String referenceField, Boolean fullyQualifiedBuildName) {
        this(url, userName, password, pattern, referenceField, fullyQualifiedBuildName, false, "", "", "");
    }

    /**
     * @param url VersionOne server URL
     * @param userName VersionOne user name
     * @param password VersionOne password
     * @param pattern Regular expression to find VersionOne workitem IDs
     * @param referenceField Name of field used in VCS commit comments to reference VersionOne items
     * @param fullyQualifiedBuildName use full name of build
     * @param useProxy Flag indicating whether we should use proxy server to connect to VersionOne
     * @param proxyUrl Proxy server URL
     * @param proxyUsername Proxy server username
     * @param proxyPassword Proxy server password
     */
    public V1Config(String url, String userName, String password, String pattern,
                    String referenceField, Boolean fullyQualifiedBuildName,
                    boolean useProxy, String proxyUrl, String proxyUsername, String proxyPassword) {

        this.url = url;
        this.userName = userName;
        this.password = password;
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
     * @throws MalformedURLException
     */
    public Services getV1Instance() throws MalformedURLException, V1Exception {
        if (services == null) {
        	ProxyProvider proxyProvider = null;

            if(useProxy) {
                try {
                    URI proxyURI = new URI(proxyUrl);
                    proxyProvider = new ProxyProvider(proxyURI, proxyUsername, proxyPassword);
                } catch(URISyntaxException e) {
                    if(logger != null) {
                        logger.printf("Invalid proxy server URI %1$, skipping proxy connection.", proxyUrl);
                    }
                }
            }

            String passwordToApply = userName != null? password : null;

            V1Connector connector = V1Connector
                	.withInstanceUrl(url)
                	.withUserAgentHeader("VersionOne.Integration.Jenkins", "0.1")
                	.withUsernameAndPassword(userName, password)
                	.withProxy(proxyProvider)
                	.build();

            services = new Services(connector);
        }
        return services;
    }

    @Override
    public String toString() {
        return "Config{" +
                "url='" + url + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", referenceField='" + referenceField + '\'' +
                ", pattern=" + pattern +
                '}';
    }

    /**
     * Checks whether {@link #referenceField} value is valid. Can be called only after {@link #isConnectionValid()
     * returned true}.
     *
     * @return true if reference field is valid, otherwise - false
     */
    public boolean isReferenceFieldValid() {
            final IMetaModel meta = services.getMeta();
            meta.getAssetType("PrimaryWorkitem").getAttributeDefinition(referenceField);
            return true;
    }
}