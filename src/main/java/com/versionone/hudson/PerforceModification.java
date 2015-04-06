package com.versionone.hudson;

import com.versionone.integration.ciCommon.VcsModification;
import hudson.plugins.perforce.PerforceChangeLogEntry;
import hudson.scm.ChangeLogSet;

import java.util.Date;

/**
 * Perforce changeset entry wrapper class
 */
public class PerforceModification implements VcsModification {
    private final PerforceChangeLogEntry entry;

    public PerforceModification(PerforceChangeLogEntry entry) {
        this.entry = entry;
    }

    public String getUserName() {
        return entry.getAuthor().getFullName();
    }

    public String getComment() {
        return entry.getChange().getDescription();
    }

    public Date getDate() {
        return entry.getChange().getDate();
    }

    public String getId() {
        return entry.getChangeNumber();
    }

    public ChangeLogSet.Entry getEntry() {
        return entry;
    }
}
