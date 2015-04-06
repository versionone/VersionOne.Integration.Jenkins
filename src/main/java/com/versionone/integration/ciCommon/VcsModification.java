/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import hudson.scm.ChangeLogSet;

import java.util.Date;

public interface VcsModification {

    String getUserName();

    String getComment();

    /**
     * Get modification date.
     *
     * @return Date and time the modification occur. Or null.
     */
    Date getDate();

    String getId();

    ChangeLogSet.Entry getEntry();
}
