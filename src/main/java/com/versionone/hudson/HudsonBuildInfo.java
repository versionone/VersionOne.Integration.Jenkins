package com.versionone.hudson;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Hudson;

import java.util.Date;
import java.util.GregorianCalendar;

import com.versionone.integration.ciCommon.BuildInfo;


public class HudsonBuildInfo implements BuildInfo {

    private final AbstractBuild build;

    public HudsonBuildInfo(AbstractBuild build) {
        this.build = build;
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
        GregorianCalendar now = new GregorianCalendar();
        return (now.getTime().getTime() - build.getTimestamp().getTime().getTime()) * 1000;
    }

    public boolean isSuccessful() {
        //return build.getBuildStatus().isSuccessful();
        return build.getResult().toString().equals("SUCCESS");
    }

    public boolean isForced() {
        //return build.getTriggeredBy().isTriggeredByUser();
        for (Action action : build.getActions()) {
            if (action instanceof CauseAction) {
                for (Object cause : ((CauseAction)action).getCauses()) {
                    if (cause instanceof Cause.UserCause) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*
    public List<SVcsModification> getChanges() {
        build.getChanges(SelectPrevBuildPolicy.SINCE_LAST_BUILD, true);
        return build.getContainingChanges();
    }
    */

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
