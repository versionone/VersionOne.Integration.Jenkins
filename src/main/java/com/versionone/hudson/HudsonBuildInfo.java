/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.hudson;

import com.versionone.integration.ciCommon.BuildInfo;
import com.versionone.integration.ciCommon.VcsModification;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.scm.ChangeLogSet;
import hudson.scm.SubversionChangeLogSet;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

public class HudsonBuildInfo implements BuildInfo {

    private final AbstractBuild build;
    private final long elapsedTime;

    public HudsonBuildInfo(AbstractBuild build) {
        this.build = build;
        GregorianCalendar now = new GregorianCalendar();
        elapsedTime = now.getTime().getTime() - build.getTimestamp().getTime().getTime();
    }

    @SuppressWarnings({"ConstantConditions"})
    public String getProjectName() {
        return build.getProject().getName();
    }

    public long getBuildId() {
        return build.getProject().getLastBuild().getNumber();
    }

    public Date getStartTime() {
        return build.getTimestamp().getTime();
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public boolean isSuccessful() {
        return build.getResult() == Result.SUCCESS;
    }

    public boolean isForced() {
        for (Action action : build.getActions()) {
            if (action instanceof CauseAction) {
                for (Object cause : ((CauseAction) action).getCauses()) {
                    if (cause instanceof Cause.UserCause) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasChanges() {
        return !build.getChangeSet().isEmptySet() && isSupportedVcs();
    }

    public Iterable<VcsModification> getChanges() {
        final List<SubversionChangeLogSet.LogEntry> supportedVcs = getSuportedChangeSets(build.getChangeSet());
        if (!supportedVcs.isEmpty()) {
            return new VcsChanges(supportedVcs);
        }
        return Collections.EMPTY_LIST;
    }

    private List<SubversionChangeLogSet.LogEntry> getSuportedChangeSets(ChangeLogSet<ChangeLogSet.Entry> changeSet) {
        final List<SubversionChangeLogSet.LogEntry> supportedChanges;
        supportedChanges = new LinkedList<SubversionChangeLogSet.LogEntry>();
        for (ChangeLogSet.Entry change : changeSet) {
            if (change instanceof SubversionChangeLogSet.LogEntry) {
                supportedChanges.add((SubversionChangeLogSet.LogEntry) change);
            }
        }
        return supportedChanges;
    }

    /**
     * @return true - if any commits were to SVN
     *         false - if no one commit was found or all commits was made not to SVN
     */
    private boolean isSupportedVcs() {
        for (ChangeLogSet.Entry change : build.getChangeSet()) {
            if (change instanceof SubversionChangeLogSet.LogEntry) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return URL to the current build results.
     *
     * @return url to the TeamCity with info about build
     */
    public String getUrl() {
        return Hudson.getInstance().getRootUrl() + build.getUrl();
    }

    public String getBuildName() {
        return build.getProject().getLastBuild().getDisplayName();
    }
}