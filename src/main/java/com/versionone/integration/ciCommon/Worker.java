package com.versionone.integration.ciCommon;

import com.versionone.apiclient.exceptions.APIException;
import com.versionone.apiclient.exceptions.ConnectionException;
import com.versionone.apiclient.exceptions.OidException;
import com.versionone.apiclient.exceptions.V1Exception;

import java.net.MalformedURLException;

public interface Worker {

    Result submitBuildRun(BuildInfo info) throws V1Exception, MalformedURLException;

    WorkitemData getWorkitemData(String id) throws V1Exception, MalformedURLException;

    public static enum Result {
        SUCCESS,
        /**
         * If there is no connection to VersionOne server.
         */
        FAIL_CONNECTION,
        /**
         * If there is no BuildProjects with appropriate reference.
         */
        FAIL_NO_BUILDPROJECT,
        /**
         * If BuildProject with such reference and name already exist.
         */
        FAIL_DUPLICATE
    }
}
