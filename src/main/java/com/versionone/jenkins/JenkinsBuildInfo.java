package com.versionone.jenkins;

import hudson.model.Action;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Hudson;
import hudson.scm.ChangeLogSet;

import java.io.PrintStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import com.versionone.integration.ciCommon.BuildInfo;
import com.versionone.integration.ciCommon.VcsModification;

public class JenkinsBuildInfo implements BuildInfo {

    private final AbstractBuild build;
    private final long elapsedTime;
    private final PrintStream logger;

    public JenkinsBuildInfo(AbstractBuild build, PrintStream logger) {
        this.build = build;
        this.logger = logger;
        GregorianCalendar now = new GregorianCalendar();
        elapsedTime = now.getTime().getTime() - build.getTimestamp().getTime().getTime();
    }

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
            	logger.println("VersionOne: Using " + entry.getClass() + " which is supported");            	
                VcsModification wrapper = VcsModificationWrapperFactory.getInstance().createWrapper(entry);
                supportedChanges.add(wrapper);
            }
            else
            	logger.println("VersionOne: Tried to use " + entry.getClass() + " which is NOT supported");
        }

        return supportedChanges;
    }

    /**
     * Return URL to the current build results.
     *
     * @return url to the system with info about build
     */
    public String getUrl() {
        return Hudson.getInstance().getRootUrl() + build.getUrl();
    }

    public String getBuildName() {
        return build.getProject().getLastBuild().getDisplayName();
    }
}