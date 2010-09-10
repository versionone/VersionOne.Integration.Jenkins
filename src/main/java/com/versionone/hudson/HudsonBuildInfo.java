/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.hudson;

import com.versionone.integration.ciCommon.BuildInfo;
import com.versionone.integration.ciCommon.VcsModification;
import hudson.model.*;
import hudson.scm.ChangeLogSet;

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
        return !build.getChangeSet().isEmptySet();
    }

    public Iterable<VcsModification> getChanges() {
        List<VcsModification> supportedChanges = new LinkedList<VcsModification>();

        ChangeLogSet<ChangeLogSet.Entry> changeSet = build.getChangeSet();

        for(ChangeLogSet.Entry entry : changeSet) {
            if(VcsModificationWrapperFactory.getInstance().isSupported(entry)) {
                VcsModification wrapper = VcsModificationWrapperFactory.getInstance().createWrapper(entry);
                supportedChanges.add(wrapper);
            }
        }

        return supportedChanges;
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