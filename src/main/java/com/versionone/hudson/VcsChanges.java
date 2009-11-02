/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.hudson;

import com.versionone.integration.ciCommon.VcsModification;
import hudson.scm.SubversionChangeLogSet;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

/**
 * Adapter of {@link SubversionChangeLogSet.LogEntry) iterable to {@link VcsModification) iterable.
 */
public class VcsChanges implements Iterable<VcsModification> {
    private final Iterable<SubversionChangeLogSet.LogEntry> changes;

    public VcsChanges(Iterable<SubversionChangeLogSet.LogEntry> changeSet) {
        this.changes = changeSet;
    }

    public Iterator<VcsModification> iterator() {
        return new VcsIterator(changes.iterator());
    }

    private static class VcsIterator implements Iterator<VcsModification> {

        private final Iterator<SubversionChangeLogSet.LogEntry> iterator;

        public VcsIterator(Iterator<SubversionChangeLogSet.LogEntry> iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public VcsModification next() {
            return new SvnModification(iterator.next());
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class SvnModification implements VcsModification {

        private final SubversionChangeLogSet.LogEntry entry;

        public SvnModification(SubversionChangeLogSet.LogEntry logEntry) {
            entry = logEntry;
        }

        public String getUserName() {
            return entry.getAuthor().getFullName();
        }

        public String getComment() {
            return entry.getMsg();
        }

        /**
         * @return date of commit or null if date cannot be parsed.
         */
        public Date getDate() {
            try {
                final DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss.S'Z'");
                df.setTimeZone(TimeZone.getDefault());
                return df.parse(entry.getDate());
            } catch (ParseException e) {
                return null;
            }
        }

        public String getId() {
            return String.valueOf(entry.getRevision());
        }
    }
}
