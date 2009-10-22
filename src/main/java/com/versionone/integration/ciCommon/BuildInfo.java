/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

//import jetbrains.buildServer.vcs.SVcsModification;

import java.util.Date;
import java.util.List;

/**
 * This interface provides information about build to {@lin com.versionone.integration.ciCommon.V1Worker}.
 */
public interface BuildInfo {

    /**
     * Project name is used for finding corresponding {@link com.versionone.om.BuildProject}.
     *
     * @return name of Project witch was build.
     */
    String getProjectName();

    /**
     * This ID is written to the reference field of the {@link com.versionone.om.BuildRun}.
     *
     * @return build ID.
     */
    long getBuildId();

    /**
     * StartTime is set as {@link com.versionone.om.BuildRun} creation time.
     *
     * @return start time.
     */
    Date getStartTime();

    /**
     * ElapsedTime is used for setting {@link com.versionone.om.BuildRun} elapsed time.
     *
     * @return building time in milliseconds.
     */
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
     * Gets list of VCS changes included in the build.
     *
     * @return list of changes.
     */
    //List<SVcsModification> getChanges();

    /**
     * @return url of build results web page.
     */
    String getUrl();

    /**
     * @return name of build. (may be equals to {@link #getBuildId()})
     */
    String getBuildName();
}
