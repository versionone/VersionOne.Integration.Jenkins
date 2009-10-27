package com.versionone.hudson;

import com.versionone.integration.ciCommon.BuildInfo;
import com.versionone.integration.ciCommon.VcsModification;
import hudson.model.*;
import hudson.scm.SubversionChangeLogSet;

import java.util.Date;
import java.util.GregorianCalendar;


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
        //return build.getBuildStatus().isSuccessful();
        return build.getResult() == Result.SUCCESS;
    }

    public boolean isForced() {
        //return build.getTriggeredBy().isTriggeredByUser();
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
        if (!build.getChangeSet().isEmptySet() && isSupportedVcs()) {
            return true;
        }
        return false;
    }

    public Iterable<VcsModification> getChanges() {
        if (isSupportedVcs()) {
            return new VcsChanges(build.getChangeSet());
        }

        return null;
    }


    /**
     *
     * @return true - if any commits were to SVN 
     *         false - if no one commit was found or all commits was made not to SVN
     */
    private boolean isSupportedVcs() {
        for(Object change : build.getChangeSet()) {
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
        //return build.getProject().getLastBuild().getAbsoluteUrl();        
    }

    public String getBuildName() {
        return build.getProject().getLastBuild().getDisplayName();       
    }

    /*
    public boolean isCorrect() {
        //return build.getBuildStatus() != Status.UNKNOWN && build.getBuildType() != null;
    }
    */


}
