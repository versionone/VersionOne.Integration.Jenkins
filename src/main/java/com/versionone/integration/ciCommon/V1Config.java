/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import com.versionone.apiclient.IMetaModel;
import com.versionone.apiclient.MetaException;
import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.SDKException;
import com.versionone.om.V1Instance;

import java.util.regex.Pattern;

public class V1Config {

    protected String url = "";
    protected String userName;
    protected String password;
    protected Pattern pattern;
    protected String referenceField;
    private V1Instance v1Instance;
    protected Boolean isFullyQualifiedBuildName;

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public Pattern getPatternObj() {
        return pattern;
    }

    public String getReferenceField() {
        return referenceField;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public void setReferenceField(String referenceField) {
        this.referenceField = referenceField;
    }

    public Boolean isFullyQualifiedBuildName() {
        return isFullyQualifiedBuildName;
    }

    public void setFullyQualifiedBuildName(Boolean fullyQualifiedBuildName) {
        isFullyQualifiedBuildName = fullyQualifiedBuildName;
    }

    /**
     * Validate connection to the VersionOne server
     *
     * @return true if all settings is correct and connection to V1 is valid, false - otherwise
     */
    public boolean isConnectionValid() {
        try {
            connect();
            return true;
        } catch (SDKException e) {
            v1Instance = null;
            return false;
        }
    }

    /**
     * getting connection to VersionOne server this method MAY BE called ONLY after {@link #isConnectionValid()}
     *
     * @return connection to VersionOne
     */
    public V1Instance getV1Instance() {
        if (v1Instance == null) {
            throw new IllegalStateException("You must call isConnectionValid() before calling getV1Instance()");
        }
        return v1Instance;
    }

    public void setDefaults() {
        url = "http://localhost/VersionOne/";
        userName = "admin";
        password = "admin";
        pattern = Pattern.compile("[A-Z]{1,2}-[0-9]+");
        referenceField = "Number";
        isFullyQualifiedBuildName = true;
    }

    private V1Instance connect() throws AuthenticationException, ApplicationUnavailableException {
        if (v1Instance == null) {
            if (getUserName() == null) {
                v1Instance = new V1Instance(getUrl());
            } else {
                v1Instance = new V1Instance(getUrl(), getUserName(), getPassword());
            }
            v1Instance.validate();
        }
        return v1Instance;
    }

    @Override
    public String toString() {
        return "Config{" +
                "referenceField='" + referenceField + '\'' +
                ", pattern=" + pattern +
                ", password='" + password + '\'' +
                ", userName='" + userName + '\'' +
                ", url='" + url + '\'' +
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
