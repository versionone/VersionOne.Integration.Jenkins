/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;


import com.versionone.hudson.VcsChanges;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Test;

import hudson.scm.SubversionChangeLogSet;
import hudson.model.User;


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

        VcsChanges modifications = new VcsChanges(new Object[]{modification1, modification2});

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


}