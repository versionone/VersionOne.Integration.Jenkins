package com.versionone.integration.ciCommon;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Test;

import hudson.model.AbstractBuild;

import hudson.model.Run;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.Project;
import hudson.model.FreeStyleProject;
import hudson.model.FreeStyleBuild;
import hudson.model.Job;
import com.versionone.hudson.HudsonBuildInfo;

import java.util.Date;
import java.util.GregorianCalendar;


public class HudsonBuildInfoTester {
    private Mockery mockery = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    @Test
    public void test() {
        final String buildName = "Build name";
        final String projectName = "Project name";
        final String url = "http://url_to_hudson/Project_name/";
        final int buildId = 1;
        //final long elapsedTime = 11111111;
        final Date startDate = new Date();
        final boolean isForced = false;
        final GregorianCalendar timestamp = new GregorianCalendar();
        timestamp.setGregorianChange(startDate);
        timestamp.add(GregorianCalendar.MINUTE, -10);

        @SuppressWarnings("unchecked")

        final FreeStyleBuild build = mockery.mock(FreeStyleBuild.class, "build");
        final AbstractProject<FreeStyleProject, FreeStyleBuild> project = mockery.mock(AbstractProject.class, "project");
        final Run lastBuild = mockery.mock(Run.class, "last build");
        mockery.checking(new Expectations() {
        {
                allowing(build).getTimestamp();
                will(returnValue(timestamp));

                allowing(build).getParent();
                will(returnValue(project));
                allowing(project).getName();
                will(returnValue(projectName));
                allowing(project).getLastBuild();
                will(returnValue(lastBuild));
                allowing(lastBuild).getDisplayName();
                will(returnValue(buildName));
                allowing(lastBuild).getNumber();
                will(returnValue(buildId));

        }}
        );
        
        BuildInfo info = new HudsonBuildInfo(build);


        Assert.assertEquals(buildName, info.getBuildName());
        Assert.assertEquals(projectName, info.getProjectName());
        Assert.assertEquals(buildId, info.getBuildId());
        Assert.assertEquals(timestamp.getTime().getTime(), info.getStartTime().getTime());
        //Assert.assertEquals(new Date().getTime() - timestamp.getTime().getTime(), info.getElapsedTime());
        //System.out.println(new Date().getTime() - timestamp.getTime().getTime());
        //info.getUrl();
    }

}
