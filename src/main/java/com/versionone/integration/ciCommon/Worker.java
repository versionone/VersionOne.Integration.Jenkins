/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

public interface Worker {

    //Statuses of notify
    int NOTIFY_SUCCESS = 0;
    int NOTIFY_FAIL_CONNECTION = 1;
    int NOTIFY_FAIL_DUPLICATE = 2;
    int NOTIFY_FAIL_NO_BUILDPROJECT = 3;

    int submitBuildRun(BuildInfo info);

    WorkitemData getWorkitemData(String id);
}
