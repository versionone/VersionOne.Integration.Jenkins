package com.versionone.integration.ciCommon;

import java.util.Date;

/**
 * This interface provides information about build to {@link Worker}.
 */
public interface BuildInfo {

    String getProjectName();
    long getBuildId();
    Date getStartTime();
    long getElapsedTime();

    /**
     * Defines success of build.
     *
     * @return true if build is successful; otherwise - false.
     */
    boolean isSuccessful();

    /**
     * Defines whether build was manualy forced.
     *
     * @return true if build was forced; otherwise - false.
     */
    boolean isForced();

    /**
     * Check whether build have any VCS changes.
     *
     * @return
     */
    boolean hasChanges();

    /**
     * Gets list of VCS changes included in the build.
     *
     * @return Iterable of VCS changes.
     */
    Iterable<VcsModification> getChanges();

    /**
     * @return url of build results web page.
     */
    String getUrl();

    /**
     * @return name of build. (may be equals to {@link #getBuildId()})
     */
    String getBuildName();
}