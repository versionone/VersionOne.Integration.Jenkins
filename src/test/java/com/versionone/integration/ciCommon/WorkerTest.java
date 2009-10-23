/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import com.versionone.om.BuildProject;
import com.versionone.om.V1Instance;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.Random;

/**
 * To run this test BuildProject must be created on V1 server.
 * A reference of the BuildProject must be set to <b>WorkerTest</b>.
 * BuildProject must be connected to a Project.
 * The Project must contains Stories
 */
public class WorkerTest {
    private static final String BUILDPROJECT_ID = "BuildProject:1083";
    private static final String BUILDPROJECT_REFERENCE = "WorkerTest";
    private static final String STORY1 = "B-01007";

    @Test
    public void test() {
        final Date now = new Date();
        V1Config cfg = new V1Config("http://integsrv01/VersionOne", "admin", "admin");
        Worker w = new V1Worker(cfg);
        BuildInfoMock info = new BuildInfoMock();
        info.buildId = new Random().nextInt();
        info.buildName = String.valueOf(info.buildId);
        info.elapsedTime = 4567;
        info.forced = false;
        info.projectName = BUILDPROJECT_REFERENCE;
        info.startTime = now;
        info.successful = true;
        info.url = "localhost";
        info.changes.add(new VcsModificationMock("User1", "Comment2 - " + STORY1, now, "Id3"));
        info.changes.add(new VcsModificationMock("User1", "Comment2 - " + STORY1, now, "Id3"));
        info.changes.add(new VcsModificationMock("User9", "Comment8", now, "Id7"));

        Assert.assertEquals(Worker.NOTIFY_SUCCESS, w.submitBuildRun(info));

        V1Instance v1 = cfg.getV1Instance();
        BuildProject x = v1.get().buildProjectByID(BUILDPROJECT_ID);

    }
}
