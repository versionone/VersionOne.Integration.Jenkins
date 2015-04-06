package com.versionone.hudson;
import com.versionone.integration.ciCommon.VcsModification;
import hudson.scm.ChangeLogSet;

import java.util.Date;

public class UnknownModification implements VcsModification {

    private final ChangeLogSet.Entry entry;

    public UnknownModification(ChangeLogSet.Entry entry) {
        this.entry = entry;
    }

    public String getUserName() {
        return entry.getAuthor().getFullName();
    }

    public String getComment() {
        return entry.getMsg();
    }

    public Date getDate() {
        return new Date();
    }

    public String getId() {
        return null;
    }

    public ChangeLogSet.Entry getEntry() {
        return null;
    }
}
