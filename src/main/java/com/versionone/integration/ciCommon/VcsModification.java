package com.versionone.integration.ciCommon;

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
}
