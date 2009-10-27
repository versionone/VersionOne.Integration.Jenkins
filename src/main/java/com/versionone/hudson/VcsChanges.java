package com.versionone.hudson;

import com.versionone.integration.ciCommon.VcsModification;
import hudson.scm.ChangeLogSet;
import hudson.scm.CVSChangeLogSet;
import hudson.scm.SubversionChangeLogSet;

import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;

/**
 *
 * Store for Visual Control Source changes
 *
 */
public class VcsChanges implements Iterable<VcsModification> {
    private final ChangeLogSet changeSet;

    public VcsChanges(ChangeLogSet changeSet) {
        this.changeSet = changeSet;
    }

    public Iterator<VcsModification> iterator() {
        return new VcsIterator(changeSet.getItems());
    }

    private class VcsIterator implements Iterator<VcsModification>, VcsModification {
        private int i = -1;
        final Object[] items;

        public VcsIterator(Object[] items) {
            this.items = items;
        }

        public boolean hasNext() {
            return items.length >= i + 1 && items[i + 1] instanceof SubversionChangeLogSet.LogEntry;
        }

        public VcsModification next() {
            i++;
            return this;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        //==========================================================
        public String getUserName() {
            return ((ChangeLogSet.Entry)items[i]).getAuthor().getFullName();  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getComment() {
            return ((ChangeLogSet.Entry)items[i]).getMsg();  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Date getDate() {
            Date date = null;
            DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss.S'Z'");
            df.setTimeZone(TimeZone.getDefault());

            // for additing CVS need to work with CVSChangeLogSet.CVSChangeLog
            if (items[i] instanceof SubversionChangeLogSet.LogEntry) {
                try {
                    date = df.parse(((SubversionChangeLogSet.LogEntry)items[i]).getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            return date;
        }

        public String getId() {
            String revision = null;
            // for additing CVS need to work with CVSChangeLogSet.CVSChangeLog
            if (items[i] instanceof SubversionChangeLogSet.LogEntry) {
                revision = String.valueOf(((SubversionChangeLogSet.LogEntry) items[i]).getRevision());
            }
            return revision;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
