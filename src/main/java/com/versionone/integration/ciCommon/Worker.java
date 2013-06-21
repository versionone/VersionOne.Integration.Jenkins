/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

public interface Worker {

    Result submitBuildRun(BuildInfo info);

    WorkitemData getWorkitemData(String id);

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
