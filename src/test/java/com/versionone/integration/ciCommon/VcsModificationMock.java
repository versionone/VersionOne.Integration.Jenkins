/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import hudson.scm.ChangeLogSet;

import java.util.Date;

public class VcsModificationMock implements VcsModification {

    public String userName;
    public String comment;
    public Date date;
    public String id;

    public VcsModificationMock(String userName, String comment, Date date, String id) {
        this.userName = userName;
        this.comment = comment;
        this.date = date;
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public String getComment() {
        return comment;
    }

    public Date getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public ChangeLogSet.Entry getEntry() {
        return null;
    }
}
