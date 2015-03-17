/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import com.versionone.DB;
import com.versionone.hudson.MessagesRes;
import com.versionone.om.*;
import com.versionone.om.filters.BuildProjectFilter;
import com.versionone.om.filters.BuildRunFilter;
import com.versionone.om.filters.ChangeSetFilter;
import com.versionone.om.filters.WorkitemFilter;
import hudson.scm.ChangeLogSet;
import hudson.scm.RepositoryBrowser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class V1Worker implements Worker {

    private static final Logger LOGGER = Logger.getLogger(V1Worker.class.getName());

    private final V1Config config;
    private final PrintStream logger;
    private final RepositoryBrowser repoBrowser;

    public V1Worker(V1Config config, PrintStream logger, RepositoryBrowser repoBrowser) {
        this.config = config;
        this.logger = logger;
        this.repoBrowser = repoBrowser;
    }

    /**
     * Adds to the VersionOne BuildRun and ChangesSet.
     */
    public Result submitBuildRun(final BuildInfo info) {
        //cancel notification if connection is not valid
        if (!config.isConnectionValid()) {
            return Result.FAIL_CONNECTION;
        }
        final BuildProject buildProject = getBuildProject(info);
        if (buildProject == null) {
            return Result.FAIL_NO_BUILDPROJECT;
        }
        if (isBuildExist(buildProject, info)) {
            return Result.FAIL_DUPLICATE;
        }
        final BuildRun buildRun = createBuildRun(buildProject, info);
        if (info.hasChanges()) {
            setChangeSets(buildRun, info);
        }
        return Result.SUCCESS;
    }

    private static String getBuildName(final BuildInfo info) {
        return info.getProjectName() + " - build." + info.getBuildName();
    }

    private boolean isBuildExist(BuildProject buildProject, BuildInfo info) {
        BuildRunFilter filter = new BuildRunFilter();
        filter.references.add(Long.toString(info.getBuildId()));
        filter.name.add(getBuildName(info));
        filter.buildProjects.add(buildProject);

        Collection<BuildRun> buildRuns = config.getV1Instance().get().buildRuns(filter);
        return buildRuns != null && buildRuns.size() != 0;
    }

    /**
     * Find the first BuildProject where the Reference matches the projectName.
     *
     * @param info information about build run
     * @return V1 representation of the project if match; otherwise - null.
     */
    private BuildProject getBuildProject(BuildInfo info) {
        BuildProjectFilter filter = new BuildProjectFilter();

        filter.references.add(info.getProjectName());
        Collection<BuildProject> projects = config.getV1Instance().get().buildProjects(filter);
        if (projects.isEmpty()) {
            return null;
        }
        return projects.iterator().next();
    }


    private static BuildRun createBuildRun(BuildProject buildProject, BuildInfo info) {
        // Generate the BuildRun instance to be saved to the recipient
        BuildRun run = buildProject.createBuildRun(getBuildName(info), new DB.DateTime(info.getStartTime()));

        run.setElapsed((double) info.getElapsedTime());
        run.setReference(Long.toString(info.getBuildId()));
        run.getSource().setCurrentValue(getSourceName(info.isForced()));
        run.getStatus().setCurrentValue(getStatusName(info.isSuccessful()));

        if (info.hasChanges()) {
            run.setDescription(getModificationDescription(info.getChanges()));
        }
        run.save();

        run.createLink("Build Report", info.getUrl(), true);
        return run;
    }

    /**
     * Returns the V1 BuildRun source name.
     *
     * @param isForced true - if build was forced.
     * @return V1 source name, "trigger" or "forced".
     */
    private static String getSourceName(boolean isForced) {
        return isForced ? "forced" : "trigger";
    }

    /**
     * Returns the V1 BuildRun status name.
     *
     * @param isSuccessful true - if build is successful.
     * @return V1 source name, "trigger" or "forced".
     */
    private static String getStatusName(boolean isSuccessful) {
        return isSuccessful ? "passed" : "failed";
    }

    /**
     * Evaluates BuildRun description.
     *
     * @param changes - set of changes affected by this BuildRun.
     * @return description string.
     */
    public static String getModificationDescription(Iterable<VcsModification> changes) {
        //Create Set to filter changes unique by User and Comment
        StringBuilder result = new StringBuilder(256);
        for (Iterator<VcsModification> it = changes.iterator(); it.hasNext();) {
            VcsModification mod = it.next();
            result.append(mod.getUserName());
            result.append(": ");
            result.append(mod.getComment());
            if (it.hasNext()) {
                result.append("<br>");
            }
        }
        return result.toString();
    }

    private void setChangeSets(BuildRun buildRun, BuildInfo info) {
        for (VcsModification change : info.getChanges()) {
            // See if we have this ChangeSet in the system.
            ChangeSetFilter filter = new ChangeSetFilter();
            String id = change.getId();

            filter.reference.add(id);
            Collection<ChangeSet> changeSetList = config.getV1Instance().get().changeSets(filter);
            if (changeSetList.isEmpty()) {
                // We don't have one yet. Create one.
                StringBuilder name = new StringBuilder();
                name.append('\'');
                name.append(change.getUserName());
                if (change.getDate() != null) {
                    name.append("\' on \'");
                    name.append(new DB.DateTime(change.getDate()));
                }
                name.append('\'');
                
                Map<String, Object> attributes = new HashMap<String, Object>();

                attributes.put("Description", getComment(change));
                ChangeSet changeSet = config.getV1Instance().create().changeSet(name.toString(), id, attributes);

                changeSetList = new ArrayList<ChangeSet>(1);
                changeSetList.add(changeSet);
            }

            Set<PrimaryWorkitem> workitems = determineWorkitems(change.getComment());
            associateWithBuildRun(buildRun, changeSetList, workitems);
        }
    }

    private String getComment(VcsModification change) {
        StringBuilder comment = new StringBuilder();
        comment.append(change.getComment());

        ChangeLogSet.Entry entry = change.getEntry();
        if (repoBrowser != null && entry != null) {
            URL url = null;
            try {
                url = repoBrowser.getChangeSetLink(entry);
            } catch (IOException e) {
                LOGGER.warning("Failed to calculate SCM repository browser link " + e.getMessage());
            }
            if (url != null && StringUtils.isNotBlank(url.toExternalForm())) {
                comment.append(" (").append("<a href=\"").append(url.toExternalForm()).append("\">");
                comment.append(repoBrowser.getDescriptor().getDisplayName()).append("</a>").append(")");
            }
            if (CollectionUtils.isNotEmpty(entry.getAffectedFiles())) {
                comment.append("<br><ul>");
                for (ChangeLogSet.AffectedFile affectedFile : entry.getAffectedFiles()) {
                    comment.append("<li>").append(affectedFile.getPath()).append("</li>");
                }
                comment.append("</ul>");
            }
        }

        return comment.toString();
    }

    private void associateWithBuildRun(BuildRun buildRun, Collection<ChangeSet> changeSets,
                                              Set<PrimaryWorkitem> workitems) {
        for (ChangeSet changeSet : changeSets) {
            buildRun.getChangeSets().add(changeSet);
            for (PrimaryWorkitem workitem : workitems) {
                if(workitem.isClosed()) {
                    logger.println(MessagesRes.workitemClosedCannotAttachData(workitem.getDisplayID()));
                    continue;
                }

                final Collection<BuildRun> completedIn = workitem.getCompletedIn();
                final List<BuildRun> toRemove = new ArrayList<BuildRun>(completedIn.size());

                changeSet.getPrimaryWorkitems().add(workitem);

                for (BuildRun otherRun : completedIn) {
                    if (otherRun.getBuildProject().equals(buildRun.getBuildProject())) {
                        toRemove.add(otherRun);
                    }
                }

                for (BuildRun buildRunDel : toRemove) {
                    completedIn.remove(buildRunDel);
                }

                completedIn.add(buildRun);
            }
        }
    }

    private Set<PrimaryWorkitem> determineWorkitems(String comment) {
        List<String> ids = getWorkitemsIds(comment, config.pattern);
        Set<PrimaryWorkitem> result = new HashSet<PrimaryWorkitem>(ids.size());

        for (String id : ids) {
            result.addAll(getPrimaryWorkitemsByReference(id));
        }
        return result;
    }

    /**
     * Resolve a check-in comment identifier to a PrimaryWorkitem. if the reference matches a SecondaryWorkitem, we need
     * to navigate to the parent.
     *
     * @param reference The identifier in the check-in comment.
     * @return A collection of matching PrimaryWorkitems.
     */
    private List<PrimaryWorkitem> getPrimaryWorkitemsByReference(String reference) {
        List<PrimaryWorkitem> result = new ArrayList<PrimaryWorkitem>();

        WorkitemFilter filter = new WorkitemFilter();
        filter.find.setSearchString(reference);
        filter.find.fields.add(config.referenceField);
        Collection<Workitem> workitems = config.getV1Instance().get().workitems(filter);
        for (Workitem workitem : workitems) {
            if (workitem instanceof PrimaryWorkitem) {
                result.add((PrimaryWorkitem) workitem);
            } else if (workitem instanceof SecondaryWorkitem) {
                result.add( (PrimaryWorkitem)((SecondaryWorkitem) workitem).getParent() );
            } else {
                throw new RuntimeException("Found unexpected Workitem type: " + workitem.getClass());
            }
        }

        return result;
    }

    /**
     * Return list of workitems got from the comment string.
     *
     * @param comment         string with some text with ids of tasks which cut using pattern set in the
     *                        referenceexpression attribute.
     * @param v1PatternCommit regular expression for comment parse and getting data from it.
     * @return list of cut ids.
     */
    public static List<String> getWorkitemsIds(String comment, Pattern v1PatternCommit) {
        final List<String> result = new LinkedList<String>();

        if (v1PatternCommit != null) {
            Matcher m = v1PatternCommit.matcher(comment);
            while (m.find()) {
                result.add(m.group());
            }
        }

        return result;
    }

    /**
     * Create short information about workitem by display id
     *
     * @param id idsplay id of workitem
     * @return short information about workitem
     */
    public WorkitemData getWorkitemData(String id) {
        Workitem workitem = config.getV1Instance().get().workitemByDisplayID(id);

        return new WorkitemData(workitem, config.url);
    }
}
