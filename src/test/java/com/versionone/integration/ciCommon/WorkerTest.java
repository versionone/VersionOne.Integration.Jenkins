/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import com.versionone.apiclient.Asset;
import com.versionone.apiclient.Query;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.filters.FilterTerm;
import com.versionone.apiclient.interfaces.IAssetType;
import com.versionone.apiclient.interfaces.IAttributeDefinition;
import com.versionone.apiclient.interfaces.IServices;
import com.versionone.apiclient.services.QueryResult;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.Random;

public class WorkerTest {
	// connection credentials
	private static final String URL_TO_V1 = "https://www14.v1host.com/v1sdktesting/";
	private static final String ACCESS_TOKEN = "1.5HdYnSk8VPfWG80mlNtbCQZRiwk=";

	private static final String BUILDPROJECT_ID = "BuildProject:1016";
	private static final String BUILDPROJECT_REFERENCE = "Sample: Call Center";
	private static final String STORY1 = "B-01001";

	/**
	 * To run this test BuildProject must be created on V1 server. A reference
	 * of the BuildProject must be set to <b>WorkerTest</b>. BuildProject must
	 * be connected to a Project. The Project must contains Stories
	 */
	@Test
	public void test() throws V1Exception, MalformedURLException {
		final Date now = new Date();
		int random = new Random().nextInt();
		final V1Config cfg = new V1Config(URL_TO_V1, ACCESS_TOKEN);
		final Worker w = new V1Worker(cfg, System.out);
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

		final IServices services = cfg.getV1Services();
        Query query = new Query(services.getOid(BUILDPROJECT_ID));
        IAttributeDefinition attributeDefinition = services.getAssetType("BuildProject").getAttributeDefinition("Reference");
        query.getSelection().add(attributeDefinition);
        QueryResult result = services.retrieve(query);

        final Asset buildProject = result.getAssets()[0];
		Assert.assertEquals(BUILDPROJECT_REFERENCE, buildProject.getAttribute(attributeDefinition).getValue());

        IAssetType buildRunType = services.getAssetType("BuildRun");
        query = new Query(buildRunType);
        FilterTerm filter = new FilterTerm(buildRunType.getAttributeDefinition("Reference"));
        filter.equal(info.buildId);
        query.setFilter(filter);
        result = services.retrieve(query);

		Assert.assertEquals(1, result.getAssets().length);
	}

//	private void checkBuildRun(BuildInfoMock info, BuildRun z) {
//		Assert.assertEquals(BUILDPROJECT_REFERENCE + " - build."
//				+ info.buildName, z.getName());
//		Assert.assertEquals(info.forced ? "Forced" : "Trigger", z.getSource()
//				.getCurrentValue());
//		Assert.assertEquals(String.valueOf(info.buildId), z.getReference());
//		Assert.assertEquals(new DB.DateTime(info.startTime), z.getDate());
//		Assert.assertEquals(info.successful ? "Passed" : "Failed", z
//				.getStatus().getCurrentValue());
//		Assert.assertEquals((double) info.elapsedTime, z.getElapsed(), 0.001);
//
//		checkWorkitemCollection(STORY1, z.getAffectedPrimaryWorkitems(null),
//				Boolean.TRUE);
//		checkWorkitemCollection(STORY1, z.getCompletedPrimaryWorkitems(null),
//				Boolean.FALSE);
//
//		final String desc = z.getDescription();
//		for (VcsModification change : info.getChanges()) {
//			Assert.assertTrue(desc.contains(change.getUserName()));
//			Assert.assertTrue(desc.contains(change.getComment()));
//		}
//
//		final Collection<ChangeSet> v1Changes = z.getChangeSets();
//		Assert.assertEquals(info.changes.size(), v1Changes.size());
//		for (ChangeSet change : v1Changes) {
//			String id = change.getReference();
//			Assert.assertTrue(info.changes.containsKey(id));
//			Assert.assertTrue(change.getName().contains(
//					info.changes.get(id).getUserName()));
//			final Date date = info.changes.get(id).getDate();
//			final String d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//					.format(date);
//			Assert.assertTrue(change.getName().contains(d));
//			Assert.assertTrue(change.getDescription().contains(
//					info.changes.get(id).getComment()));
//		}
//	}

//	private void checkWorkitemCollection(String storyName,
//			Collection<PrimaryWorkitem> z, Boolean flag) {
//
//		if (flag) {
//			Assert.assertEquals(6012, z.size());
//		} else {
//			Assert.assertEquals(0, z.size());
//		}
//		Assert.assertEquals(storyName, z.iterator().next().getDisplayID());
//	}

	private static final String ASSETDETAIL = "assetdetail.v1?oid=";

	/**
	 * This is integration test to use this test need to: 1. Setup credentials
	 * for connection 2. Create story in the VersionOne 3. Copy display ID of
	 * story and set it to displayId variable 4. Copy name of story to storyName
	 * variable 4. Copy token of story to storyId variable
	 */
	@Test
	public void testWorkitemData() throws V1Exception, MalformedURLException {
		final String displayId = "B-01012";
		final String storyName = "Sample: Enter Order Total";
		final String storyId = "Story:1066";

		final V1Config v1Config = new V1Config(URL_TO_V1, ACCESS_TOKEN);
		final Worker w = new V1Worker(v1Config, System.out);

		WorkitemData workitemData = w.getWorkitemData(displayId);

		Assert.assertEquals(storyId, workitemData.getId());
		Assert.assertEquals(storyName, workitemData.getName());
		Assert.assertEquals(URL_TO_V1 + ASSETDETAIL + storyId,
				workitemData.getUrl());
	}
}
