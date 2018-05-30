/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import com.versionone.jenkins.JenkinsBuildInfo;
import hudson.model.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class HudsonBuildInfoTest {
    private Mockery mockery = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };


    @Test
    public void test() {
        final String buildName = "Build name";
        final String projectName = "Project name";
        final int buildId = 1;
        final String externalizableId = projectName + "#" + buildId;
        final Date startDate = new Date();
        final boolean isForced = true; // user initiated build  
        final GregorianCalendar timestamp = new GregorianCalendar();
        timestamp.setGregorianChange(startDate);
        timestamp.add(GregorianCalendar.MINUTE, -10);

        final Run build = mockery.mock(FreeStyleBuild.class, "build");
        final Job project = mockery.mock(FreeStyleProject.class, "project");
        final Cause.UserIdCause userCause = mockery.mock(Cause.UserIdCause.class, "cause UserIdCause");
        final List userCauses = Arrays.asList(userCause);

        mockery.checking(new Expectations() {
        {
                allowing(build).getTimestamp();
                will(returnValue(timestamp));

                allowing(build).getParent();
                will(returnValue(project));

                allowing(project).getName();
                will(returnValue(projectName));

                allowing(project).getFullName();
                will(returnValue(projectName));

                allowing(project).getBuildByNumber(buildId);
                will(returnValue(build));

                allowing(build).getExternalizableId();
                will(returnValue(externalizableId));

                allowing(build).getDisplayName();
                will(returnValue(buildName));

                allowing(build).getNumber();
                will(returnValue(buildId));

                // mock who start build
                allowing(build).getCauses();
                will(returnValue(userCauses));
        }}
        );
        // TODO: Test for Changes sets.
        BuildInfo info = new JenkinsBuildInfo(build, System.out);

        Assert.assertEquals(buildName, info.getBuildName());
        Assert.assertEquals(projectName, info.getProjectName());
        Assert.assertEquals(new Long(buildId), new Long(info.getBuildId()));
        Assert.assertEquals(new Long(timestamp.getTime().getTime()), new Long(info.getStartTime().getTime()));
        Assert.assertEquals(isForced, info.isForced());
    }

}
