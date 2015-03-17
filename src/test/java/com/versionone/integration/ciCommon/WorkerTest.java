/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.versionone.DB;
import com.versionone.om.BuildProject;
import com.versionone.om.BuildRun;
import com.versionone.om.ChangeSet;
import com.versionone.om.PrimaryWorkitem;
import com.versionone.om.V1Instance;
import com.versionone.om.filters.BuildRunFilter;

public class WorkerTest {
	// connection credentials
	private static final String URL_TO_V1 = "https://www14.v1host.com/v1sdktesting/";
	private static final String PASSWORD_TO_V1 = "admin";
	private static final String LOGIN_TO_V1 = "admin";

	private static final String BUILDPROJECT_ID = "BuildProject:1016";
	private static final String BUILDPROJECT_REFERENCE = "Sample: Call Center";
	private static final String STORY1 = "B-01001";

	/**
	 * To run this test BuildProject must be created on V1 server. A reference
	 * of the BuildProject must be set to <b>WorkerTest</b>. BuildProject must
	 * be connected to a Project. The Project must contains Stories
	 */
	@Test
	public void test() {
		final Date now = new Date();
		int random = new Random().nextInt();
		final V1Config cfg = new V1Config(URL_TO_V1, LOGIN_TO_V1,
				PASSWORD_TO_V1);
		final Worker w = new V1Worker(cfg, System.out, null);
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
		info.changes.put(id, new VcsModificationMock("User1", "Comment2 - "
				+ STORY1, now, id));
		id = "Id" + random;
		info.changes.put(id, new VcsModificationMock("User9", "Comment8", now,
				id));

		Assert.assertEquals(Worker.Result.SUCCESS, w.submitBuildRun(info));

		final V1Instance v1 = cfg.getV1Instance();
		final BuildProject x = v1.get().buildProjectByID(BUILDPROJECT_ID);
		Assert.assertEquals(BUILDPROJECT_REFERENCE, x.getReference());
		final BuildRunFilter filter = new BuildRunFilter();
		filter.references.add(String.valueOf(info.buildId));
		final Collection<BuildRun> y = x.getBuildRuns(filter);
		Assert.assertEquals(1, y.size());
	}

	private void checkBuildRun(BuildInfoMock info, BuildRun z) {
		Assert.assertEquals(BUILDPROJECT_REFERENCE + " - build."
				+ info.buildName, z.getName());
		Assert.assertEquals(info.forced ? "Forced" : "Trigger", z.getSource()
				.getCurrentValue());
		Assert.assertEquals(String.valueOf(info.buildId), z.getReference());
		Assert.assertEquals(new DB.DateTime(info.startTime), z.getDate());
		Assert.assertEquals(info.successful ? "Passed" : "Failed", z
				.getStatus().getCurrentValue());
		Assert.assertEquals((double) info.elapsedTime, z.getElapsed(), 0.001);

		checkWorkitemCollection(STORY1, z.getAffectedPrimaryWorkitems(null),
				Boolean.TRUE);
		checkWorkitemCollection(STORY1, z.getCompletedPrimaryWorkitems(null),
				Boolean.FALSE);

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
			Assert.assertTrue(change.getName().contains(
					info.changes.get(id).getUserName()));
			final Date date = info.changes.get(id).getDate();
			final String d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(date);
			Assert.assertTrue(change.getName().contains(d));
			Assert.assertTrue(change.getDescription().contains(
					info.changes.get(id).getComment()));
		}
	}

	private void checkWorkitemCollection(String storyName,
			Collection<PrimaryWorkitem> z, Boolean flag) {

		if (flag) {
			Assert.assertEquals(6012, z.size());
		} else {
			Assert.assertEquals(0, z.size());
		}
		Assert.assertEquals(storyName, z.iterator().next().getDisplayID());
	}

	private static final String ASSETDETAIL = "assetdetail.v1?oid=";

	/**
	 * This is integration test to use this test need to: 1. Setup credentials
	 * for connection 2. Create story in the VersionOne 3. Copy display ID of
	 * story and set it to displayId variable 4. Copy name of story to storyName
	 * variable 4. Copy token of story to storyId variable
	 */
	@Test
	public void testWorkitemData() {
		final String displayId = "B-01012";
		final String storyName = "Sample: Enter Order Total";
		final String storyId = "Story:1066";

		final V1Config cfg = new V1Config(URL_TO_V1, LOGIN_TO_V1,
				PASSWORD_TO_V1);
		final Worker w = new V1Worker(cfg, System.out, null);

		WorkitemData workitemData = w.getWorkitemData(displayId);

		Assert.assertEquals(storyId, workitemData.getId());
		Assert.assertEquals(storyName, workitemData.getName());
		Assert.assertEquals(URL_TO_V1 + ASSETDETAIL + storyId,
				workitemData.getUrl());
	}
}
