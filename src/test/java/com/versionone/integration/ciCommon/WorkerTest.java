/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import com.versionone.DB;
import com.versionone.om.*;
import com.versionone.om.filters.BuildRunFilter;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Collection;
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
//    @Ignore("This is integrational test. See WorkerTest description.")
public void test() {
        final Date now = new Date();
        int random = new Random().nextInt();
        final V1Config cfg = new V1Config("http://integsrv01/VersionOne", "admin", "admin");
        final Worker w = new V1Worker(cfg);
        final BuildInfoMock info = new BuildInfoMock();
        info.buildId = random++;
        info.buildName = String.valueOf(random++);
        info.elapsedTime = 4567;
        info.forced = false;
        info.projectName = BUILDPROJECT_REFERENCE;
        info.startTime = now;
        info.successful = true;
        info.url = "localhost";
        String id = "Id" + (random++);
        info.changes.put(id, new VcsModificationMock("User1", "Comment2 - " + STORY1, now, id));
        id = "Id" + random;
        info.changes.put(id, new VcsModificationMock("User9", "Comment8", now, id));

        Assert.assertEquals(Worker.NOTIFY_SUCCESS, w.submitBuildRun(info));

        final V1Instance v1 = cfg.getV1Instance();
        final BuildProject x = v1.get().buildProjectByID(BUILDPROJECT_ID);
        Assert.assertEquals(BUILDPROJECT_REFERENCE, x.getReference());
        final BuildRunFilter filter = new BuildRunFilter();
        filter.references.add(String.valueOf(info.buildId));
        final Collection<BuildRun> y = x.getBuildRuns(filter);
        Assert.assertEquals(1, y.size());
        checkBuildRun(info, y.iterator().next());
    }

    private void checkBuildRun(BuildInfoMock info, BuildRun z) {
        Assert.assertEquals(BUILDPROJECT_REFERENCE + " - build." + info.buildName, z.getName());
        Assert.assertEquals(info.forced ? "Forced" : "Trigger", z.getSource().getCurrentValue());
        Assert.assertEquals(String.valueOf(info.buildId), z.getReference());
        Assert.assertEquals(new DB.DateTime(info.startTime), z.getDate());
        Assert.assertEquals(info.successful ? "Passed" : "Failed", z.getStatus().getCurrentValue());
        Assert.assertEquals((double) info.elapsedTime, z.getElapsed(), 0.001);

        checkWorkitemCollection(STORY1, z.getAffectedPrimaryWorkitems(null));
        checkWorkitemCollection(STORY1, z.getCompletedPrimaryWorkitems(null));

        final String desc = z.getDescription();
        for (VcsModification change : info.getChanges()) {
            Assert.assertTrue(desc.contains(change.getUserName()));
            Assert.assertTrue(desc.contains(change.getComment()));
        }

        final Collection<ChangeSet> v1Changes = z.getChangeSets();
        Assert.assertEquals(info.changes.size(), v1Changes.size());
        for (ChangeSet change : v1Changes) {
            String id = change.getReference();
            Assert.assertTrue(info.changes.containsKey(id));
            Assert.assertTrue(change.getName().contains(info.changes.get(id).getUserName()));
            final Date date = info.changes.get(id).getDate();
            final String d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
            Assert.assertTrue(change.getName().contains(d));
            Assert.assertTrue(change.getDescription().contains(info.changes.get(id).getComment()));
        }
    }

    private void checkWorkitemCollection(String storyName, Collection<PrimaryWorkitem> z) {
        Assert.assertEquals(1, z.size());
        Assert.assertEquals(storyName, z.iterator().next().getDisplayID());
    }
}
