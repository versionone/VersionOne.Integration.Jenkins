package com.versionone.jenkins;

import hudson.AbortException;
import hudson.model.*;
import hudson.scm.ChangeLogSet;

import java.io.PrintStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import com.versionone.integration.ciCommon.BuildInfo;
import com.versionone.integration.ciCommon.VcsModification;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper;

public class JenkinsBuildInfo implements BuildInfo {

    private final Run build;
    private final RunWrapper runWrapper;
    private final long elapsedTime;
    private final PrintStream logger;

    public JenkinsBuildInfo(Run build, PrintStream logger) {
        this.build = build;
        this.runWrapper = new RunWrapper(build,true);
        this.logger = logger;
        GregorianCalendar now = new GregorianCalendar();
        elapsedTime = now.getTime().getTime() - build.getTimestamp().getTime().getTime();
    }

    public String getProjectName() {
        return build.getParent().getName();
    }

    public long getBuildId() {
        return build.getNumber();
    }

    public Date getStartTime() {
        return build.getTimestamp().getTime();
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public boolean isSuccessful() {
        try {
            // Using RunnerWrapper because for pipeline jobs currentResult is not same as Run.getResult().
            return runWrapper.getCurrentResult().equals(Result.SUCCESS.toString());
        } catch (AbortException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isForced() {
        for (Object cause : build.getCauses()) {
            if (cause instanceof Cause.UserIdCause) {
                return true;
            }
        }
        return false;
    }

    public boolean hasChanges() {
        try {
            return !runWrapper.getChangeSets().isEmpty();
        } catch (Exception e) {
            logger.println(e.getMessage());
        }
        return false;
    }

    public Iterable<VcsModification> getChanges() {

    	List<VcsModification> supportedChanges = new LinkedList<VcsModification>();
        if (hasChanges()) {
            try {
                List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeSets = runWrapper.getChangeSets();
                for (ChangeLogSet<? extends ChangeLogSet.Entry> changeSet: changeSets) {
                    for (ChangeLogSet.Entry entry : changeSet) {
                        if (VcsModificationWrapperFactory.getInstance().isSupported(entry)) {
                            logger.println("VersionOne: Using " + entry.getClass() + " which is supported");
                            VcsModification wrapper = VcsModificationWrapperFactory.getInstance().createWrapper(entry);
                            supportedChanges.add(wrapper);
                        } else
                            logger.println("VersionOne: Tried to use " + entry.getClass() + " which is NOT supported");
                    }
                }
            } catch (Exception e) {
                logger.println(e.getMessage());
            }
        }
        return supportedChanges;
    }

    /**
     * Return URL to the current build results.
     *
     * @return url to the system with info about build
     */
    public String getUrl() {
        if (Jenkins.getInstanceOrNull() != null) {
            return Jenkins.getInstanceOrNull().getRootUrl() + build.getUrl();
        }
        return "";
    }

    public String getBuildName() {
        return build.getDisplayName();
    }
}