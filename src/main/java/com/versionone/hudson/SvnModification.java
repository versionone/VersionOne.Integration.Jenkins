package com.versionone.hudson;

import com.versionone.integration.ciCommon.VcsModification;
import hudson.scm.SubversionChangeLogSet;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SvnModification implements VcsModification {

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
