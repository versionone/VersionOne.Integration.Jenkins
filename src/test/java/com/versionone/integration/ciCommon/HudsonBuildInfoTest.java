/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Test;

import hudson.model.Run;
import hudson.model.FreeStyleProject;
import hudson.model.FreeStyleBuild;
import hudson.model.Action;
import hudson.model.CauseAction;
import hudson.model.Cause;
import hudson.scm.ChangeLogSet;
import hudson.scm.SubversionChangeLogSet;
import hudson.scm.CVSChangeLogSet;
import com.versionone.hudson.HudsonBuildInfo;
import com.versionone.hudson.VcsChanges;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;


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
        //final String url = "http://url_to_hudson/Project_name/";
        final int buildId = 1;
        //final long elapsedTime = 11111111;
        final Date startDate = new Date();
        final boolean isForced = true; // user initiated build  
        final GregorianCalendar timestamp = new GregorianCalendar();
        final String[] comments = new String[]{"message 1", "message 3"};
        timestamp.setGregorianChange(startDate);
        timestamp.add(GregorianCalendar.MINUTE, -10);


        final FreeStyleBuild build = mockery.mock(FreeStyleBuild.class, "build");
        final FreeStyleProject project = mockery.mock(FreeStyleProject.class, "project");
        final Run lastBuild = mockery.mock(Run.class, "last build");
        final Action action = mockery.mock(CauseAction.class, "Action");
        final Cause.UserCause userCause = mockery.mock(Cause.UserCause.class, "cause UserCause");
        final ChangeLogSet changeLogSet = mockery.mock(ChangeLogSet.class, "ChangeLogSet");
        final SubversionChangeLogSet.LogEntry modification1 = mockery.mock(SubversionChangeLogSet.LogEntry.class, "changelist 1 svn");
        final CVSChangeLogSet.Entry modification2 = mockery.mock(CVSChangeLogSet.Entry.class, "changelist 2 cvs");
        final SubversionChangeLogSet.LogEntry modification3 = mockery.mock(SubversionChangeLogSet.LogEntry.class, "changelist 3 svn");
        final List<Action> actions = Arrays.asList(action);
        final List userCauses = Arrays.asList(userCause);
        //final Object[] allModifications = new Object[] {modification1, modification2, modification3}; 

        final Iterator iterator = mockery.mock(Iterator.class, "iterator"); 

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

                // mock who start build
                allowing(build).getActions();
                will(returnValue(actions));
                ((CauseAction)allowing(action)).getCauses();
                will(returnValue(userCauses));

                // mock change sets
                allowing(build).getChangeSet();
                will(returnValue(changeLogSet));
                allowing((changeLogSet)).iterator();
                will(returnValue(iterator));
                one(iterator).hasNext();
                will(returnValue(true));
                one(iterator).next();
                will(returnValue(modification1));

                one(iterator).hasNext();
                will(returnValue(true));
                one(iterator).next();
                will(returnValue(modification2));

                one(iterator).hasNext();
                will(returnValue(true));
                one(iterator).next();
                will(returnValue(modification3));

                one(iterator).hasNext();
                will(returnValue(false));

                one(modification1).getMsg();
                will(returnValue(comments[0]));

                one(modification3).getMsg();
                will(returnValue(comments[1]));

        }}
        );
        
        BuildInfo info = new HudsonBuildInfo(build);
        Iterable<VcsModification> supportedVcsChange = info.getChanges();
        int i = 0;
        for (Iterator<VcsModification> change = supportedVcsChange.iterator(); change.hasNext();) {
            VcsModification mod = change.next();
            Assert.assertEquals(comments[i], mod.getComment());
            i++;
        }

        //Assert.assertEquals(2, i);

        Assert.assertEquals(buildName, info.getBuildName());
        Assert.assertEquals(projectName, info.getProjectName());
        Assert.assertEquals(new Long(buildId), new Long(info.getBuildId()));
        Assert.assertEquals(new Long(timestamp.getTime().getTime()), new Long(info.getStartTime().getTime()));
        Assert.assertEquals(isForced, info.isForced());
        //Assert.assertEquals(new Date().getTime() - timestamp.getTime().getTime(), info.getElapsedTime());
        //System.out.println(new Date().getTime() - timestamp.getTime().getTime());
        //info.getUrl();
    }

}
