/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import com.versionone.apiclient.IMetaModel;
import com.versionone.apiclient.MetaException;
import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.SDKException;
import com.versionone.om.V1Instance;

import java.util.regex.Pattern;

public final class V1Config {

    public final String url;
    public final String userName;
    public final String password;
    public final Pattern pattern;
    public final String referenceField;
    public final Boolean isFullyQualifiedBuildName;

    private V1Instance v1Instance;

    public V1Config() {
        url = "http://localhost/VersionOne/";
        userName = "admin";
        password = "admin";
        pattern = Pattern.compile("[A-Z]{1,2}-[0-9]+");
        referenceField = "Number";
        isFullyQualifiedBuildName = true;
    }

    public V1Config(String url, String userName, String password) {
        this.url = url;
        this.userName = userName;
        this.password = password;
        pattern = Pattern.compile("[A-Z]{1,2}-[0-9]+");
        referenceField = "Number";
        isFullyQualifiedBuildName = true;
    }

    /**
     * @param url
     * @param userName
     * @param password
     * @param pattern                 RegEx
     * @param referenceField
     * @param fullyQualifiedBuildName
     */
    public V1Config(String url, String userName, String password, String pattern,
                    String referenceField, Boolean fullyQualifiedBuildName) {
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.pattern = Pattern.compile(pattern);
        this.referenceField = referenceField;
        isFullyQualifiedBuildName = fullyQualifiedBuildName;
    }

    /**
     * Validate connection to the VersionOne server
     *
     * @return true if all settings is correct and connection to V1 is valid, false - otherwise
     */
    public boolean isConnectionValid() {
        try {
            checkConnectionValid();
            return true;
        } catch (SDKException e) {
            return false;
        }
    }

    public void checkConnectionValid() throws AuthenticationException, ApplicationUnavailableException {
        getV1Instance().validate();
    }

    /**
     * Get instance of connection to VersionOne with settings from this class.
     *
     * @return connection to VersionOne
     */
    public V1Instance getV1Instance() {
        if (v1Instance == null) {
            if (userName == null) {
                v1Instance = new V1Instance(url);
            } else {
                v1Instance = new V1Instance(url, userName, password);
            }
            v1Instance.validate();
        }
        return v1Instance;
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
        try {
            final IMetaModel meta = getV1Instance().getApiClient().getMetaModel();
            meta.getAssetType("PrimaryWorkitem").getAttributeDefinition(referenceField);
            return true;
        } catch (MetaException e) {
            return false;
        }
    }
}
