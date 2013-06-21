/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import com.versionone.om.Workitem;

/**
 * Representation of workitem from the VersionOne
 */
public class WorkitemData {

    private final Workitem workitem;
    private final String url;
    private static final String ASSETDETAIL = "assetdetail.v1?oid=";

    /**
     *
     * @param workitem Workitem from the VersionOne
     * @param url url to the VersionOne server
     */
    public WorkitemData(Workitem workitem, String url) {
        this.workitem = workitem;
        if (!url.endsWith("/")) {
            url += "/";
        }
        this.url = url;
    }

    public String getId() {
        return workitem.getID().toString();
    }

    public String getName() {
        return workitem.getName();
    }

    public String getUrl() {
        return url + ASSETDETAIL + getId();
    }

    public boolean hasValue() {
        return workitem != null;
    }
}
