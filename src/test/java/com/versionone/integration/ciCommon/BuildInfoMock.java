package com.versionone.integration.ciCommon;

import java.util.Date;
import java.util.HashMap;

public class BuildInfoMock implements BuildInfo {

    public String projectName;
    public long buildId;
    public Date startTime;
    public long elapsedTime;
    public boolean successful;
    public boolean forced;
    public HashMap<String, VcsModification> changes = new HashMap<String, VcsModification>();
    public String url;
    public String buildName;

    public String getProjectName() {
        return projectName;
    }

    public long getBuildId() {
        return buildId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public boolean isForced() {
        return forced;
    }

    public boolean hasChanges() {
        return changes != null && !changes.isEmpty();
    }

    public Iterable<VcsModification> getChanges() {
        return changes.values();
    }

    public String getUrl() {
        return url;
    }

    public String getBuildName() {
        return buildName;
    }
}
