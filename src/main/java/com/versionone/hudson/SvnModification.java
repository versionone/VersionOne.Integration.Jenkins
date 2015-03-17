package com.versionone.hudson;

import com.versionone.integration.ciCommon.VcsModification;
import hudson.scm.ChangeLogSet;
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
        String dateWithoutMicrosecond = removeMicrosecondFromDate(entry.getDate());
        try {
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            return df.parse(dateWithoutMicrosecond);
        } catch (ParseException e) {
            return null;
        }
    }

    private String removeMicrosecondFromDate(String date) {
        String[] dateParts = date.split("\\.");
        if (dateParts.length != 2 || dateParts[1].length() != 7) {
            return date;
        }
        String millisecond = dateParts[1].substring(0, 3);
        return dateParts[0] + "." + millisecond + "Z";
    }

    public String getId() {
        return String.valueOf(entry.getRevision());
    }

    public ChangeLogSet.Entry getEntry() {
        return entry;
    }

}
