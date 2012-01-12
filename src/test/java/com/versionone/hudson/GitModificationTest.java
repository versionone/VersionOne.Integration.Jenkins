package com.versionone.hudson;

import hudson.plugins.git.GitChangeSet;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GitModificationTest {
    private Mockery mockery = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.ENGLISH);

    @Test
    public void getDate() throws ParseException {
        final String strDate = "1979-12-05 23:20:00 +0300";

        GitModification modification = CreateGitModification(strDate);

        Date date = modification.getDate();
        Assert.assertEquals(dateFormat.parse(strDate), date);
    }

    @Test
    public void getDateIncorrectFormat() throws ParseException {
        final String stringDate = "1979-12-05 23:20:00";
        final String stringDateTimeTimeZone = "1979-12-05 23:20:00 +0300";

        GitModification modification = CreateGitModification(stringDate);

        Date date = modification.getDate();
        Assert.assertFalse(dateFormat.parse(stringDateTimeTimeZone).equals(date));
    }

    private GitModification CreateGitModification(final String strDate) {
        final GitChangeSet changeSet = mockery.mock(GitChangeSet.class);
        GitModification modification = new GitModification(changeSet);
        mockery.checking(new Expectations() {
            {
                allowing(changeSet).getDate();
                will(returnValue(strDate));
            }
        });
        return modification;
    }
}
