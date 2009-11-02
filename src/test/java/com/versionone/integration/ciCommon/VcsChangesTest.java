/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import com.versionone.hudson.VcsChanges;
import hudson.model.User;
import hudson.scm.SubversionChangeLogSet;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class VcsChangesTest {

    private Mockery mockery = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    @Test
    public void testGetModificationDescription() {
        final String userName1 = "user name 1";
        final String userName2 = "user name 2";
        final String desc1 = "description 1";
        final String desc2 = "description 2";

        final SubversionChangeLogSet.LogEntry modification1 = mockery.mock(SubversionChangeLogSet.LogEntry.class, "changelist 1");
        final User user1 = mockery.mock(User.class, "user 1");
        final SubversionChangeLogSet.LogEntry modification2 = mockery.mock(SubversionChangeLogSet.LogEntry.class, "changelist 2");
        final User user2 = mockery.mock(User.class, "user 2");

        VcsChanges modifications = new VcsChanges(Arrays.asList(modification1, modification2));

        mockery.checking(new Expectations() {
            {
                allowing(modification1).getMsg();
                will(returnValue(desc1));
                allowing(modification1).getAuthor();
                will(returnValue(user1));
                allowing(user1).getFullName();
                will(returnValue(userName1));

                allowing(modification2).getMsg();
                will(returnValue(desc2));
                allowing(modification2).getAuthor();
                will(returnValue(user2));
                allowing(user2).getFullName();
                will(returnValue(userName2));
            }
        });

        final String result = V1Worker.getModificationDescription(modifications);

        Assert.assertTrue(result.contains(userName1));
        Assert.assertTrue(result.contains(userName2));
        Assert.assertTrue(result.contains(desc1));
        Assert.assertTrue(result.contains(desc2));
    }

    @Test
    public void test() {
        final String[] userNames = new String[]{"user name 1", "user name 2"};
        final String[] descriptions = new String[]{"description 1", "description 2"};
        final int[] revision = new int[]{1, 2};


        final SubversionChangeLogSet.LogEntry modification1 = mockery.mock(SubversionChangeLogSet.LogEntry.class, "changelist 1");
        final User user1 = mockery.mock(User.class, "user 1");
        final SubversionChangeLogSet.LogEntry modification2 = mockery.mock(SubversionChangeLogSet.LogEntry.class, "changelist 2");
        final User user2 = mockery.mock(User.class, "user 2");

        VcsChanges modifications = new VcsChanges(Arrays.asList(modification1, modification2));

        mockery.checking(new Expectations() {
            {
                allowing(modification1).getMsg();
                will(returnValue(descriptions[0]));
                allowing(modification1).getAuthor();
                will(returnValue(user1));
                allowing(user1).getFullName();
                will(returnValue(userNames[0]));
                allowing(modification1).getRevision();
                will(returnValue(revision[0]));


                allowing(modification2).getMsg();
                will(returnValue(descriptions[1]));
                allowing(modification2).getAuthor();
                will(returnValue(user2));
                allowing(user2).getFullName();
                will(returnValue(userNames[1]));
                allowing(modification2).getRevision();
                will(returnValue(revision[1]));
            }
        });

        int i = 0;
        for (VcsModification mod : modifications) {
            Assert.assertEquals(descriptions[i], mod.getComment());
            Assert.assertEquals(userNames[i], mod.getUserName());
            Assert.assertEquals(String.valueOf(revision[i]), mod.getId());
            i++;
        }

    }
}