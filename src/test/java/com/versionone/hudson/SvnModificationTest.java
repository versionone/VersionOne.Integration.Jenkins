package com.versionone.hudson;

import hudson.scm.SubversionChangeLogSet;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SvnModificationTest {
    private Mockery mockery = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    @Test
    public void getDate() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        final String gmtDate = "2012-01-12T08:43:41.359375Z";//SVN plugin returns date in GMT
        final String localDateWithoutMicrosecond = "2012-01-12T11:43:41.359Z";
        SvnModification modification = CreateSvnModification(gmtDate);

        Date date = modification.getDate();
        Assert.assertEquals(dateFormat.parse(localDateWithoutMicrosecond), date);
    }

    @Test
    public void getDateWithoutMicrosecond() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        final String gmtDate = "2012-01-12T08:43:41.359Z";//SVN plugin returns date in GMT
        final String localDate = "2012-01-12T11:43:41.359Z";
        SvnModification modification = CreateSvnModification(gmtDate);

        Date date = modification.getDate();
        Assert.assertEquals(dateFormat.parse(localDate), date);
    }

    private SvnModification CreateSvnModification(final String strDate) {
        final SubversionChangeLogSet.LogEntry changeSet = mockery.mock(SubversionChangeLogSet.LogEntry.class);

        SvnModification modification = new SvnModification(changeSet);
        mockery.checking(new Expectations() {
            {
                allowing(changeSet).getDate();
                will(returnValue(strDate));
            }
        });
        return modification;
    }
}
