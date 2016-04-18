package com.versionone.integration.ciCommon;

import org.apache.commons.lang.StringUtils;

/**
 * Representation of workitem from the VersionOne
 */
public class WorkitemData {

    private final String id;
    private final String name;
    private final String url;
    private static final String ASSETDETAIL = "assetdetail.v1?oid=";

    /**
     *
     * @param url url to the VersionOne server
     */
    public WorkitemData(String id, String name, String url) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        this.url = url;
        this.name = name;
        this.id = id;

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url + ASSETDETAIL + getId();
    }

    public boolean hasValue() {
        return StringUtils.isNotBlank(id);
    }
}
