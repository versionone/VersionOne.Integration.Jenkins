package com.versionone.integration.ciCommon;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.versionone.DB;
import com.versionone.jenkins.MessagesRes;
import com.versionone.om.BuildProject;
import com.versionone.om.BuildRun;
import com.versionone.om.ChangeSet;
import com.versionone.om.PrimaryWorkitem;
import com.versionone.om.SecondaryWorkitem;
import com.versionone.om.Workitem;
import com.versionone.om.filters.BuildProjectFilter;
import com.versionone.om.filters.BuildRunFilter;
import com.versionone.om.filters.ChangeSetFilter;
import com.versionone.om.filters.WorkitemFilter;

public class V1Worker implements Worker {

    private final V1Config config;
    private final PrintStream logger;

    public V1Worker(V1Config config, PrintStream logger) {
        this.config = config;
        this.logger = logger;
    }

    /**
     * Adds to the VersionOne BuildRun and ChangesSet.
     */
    public Result submitBuildRun(final BuildInfo info) {
    	
        //Validate connection to V1.
        if (!config.isConnectionValid()) {
        	logger.println("VersionOne: Connection to VersionOne failed");
            return Result.FAIL_CONNECTION;
        }
        logger.println("VersionOne: Connection to VersionOne succeeded");
        
        //Find a matching BuildProject.
        final BuildProject buildProject = getBuildProject(info);
        
        //Validate that BuildProject exists.
        if (buildProject == null) {
        	logger.println("VersionOne: No matching BuildProject found in VersionOne");
            return Result.FAIL_NO_BUILDPROJECT;
        }
        
        //Validate that the BuildRun does not already exist.
        if (isBuildExist(buildProject, info)) {
        	logger.println("VersionOne: BuildRun already exists in VersionOne");
            return Result.FAIL_DUPLICATE;
        }
        
        //Create a BuildRun in the V1 BuildProject.
        final BuildRun buildRun = createBuildRun(buildProject, info);
        logger.println("VersionOne: Created BuildRun " + buildRun.getName());
        
        //If available, add ChangeSets.
        if (info.hasChanges()) {
        	logger.println("VersionOne: Found changesets to process");
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
    	
        //Generate the BuildRun asset.
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
    	
        //Create Set to filter changes unique by User and Comment.
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
        	
        	logger.println("VersionOne: Processing changeset: " + change.getId());
        	
            //See if we have this ChangeSet in the system.
            ChangeSetFilter filter = new ChangeSetFilter();
            String id = change.getId();
            filter.reference.add(id);
            Collection<ChangeSet> changeSetList = config.getV1Instance().get().changeSets(filter);
            
            if (changeSetList.isEmpty()) {
            	
                //We don't have one yet. Create one.
                StringBuilder name = new StringBuilder();
                name.append('\'');
                name.append(change.getUserName());
                if (change.getDate() != null) {
                    name.append("\' on \'");
                    name.append(new DB.DateTime(change.getDate()));
                }
                name.append('\'');
                
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("Description", change.getComment());
                ChangeSet changeSet = config.getV1Instance().create().changeSet(name.toString(), id, attributes);
                logger.println("VersionOne: Created changeset: " + changeSet.getName());

                changeSetList = new ArrayList<ChangeSet>(1);
                changeSetList.add(changeSet);
            }

            Set<PrimaryWorkitem> workitems = determineWorkitems(change.getComment());
            
            logger.println("VersionOne: Associating " + changeSetList.size() + " changesets and " + workitems.size() + " workitems to buildrun");
            associateWithBuildRun(buildRun, changeSetList, workitems);
        }
    }

    private void associateWithBuildRun(BuildRun buildRun, Collection<ChangeSet> changeSets, Set<PrimaryWorkitem> workitems) {
    	
        for (ChangeSet changeSet : changeSets) {
        	
            buildRun.getChangeSets().add(changeSet);
            logger.println("VersionOne: Added changeset " + changeSet.getName());
            
            for (PrimaryWorkitem workitem : workitems) {
            	
                if (workitem.isClosed()) {
                    logger.println("VersionOne: " + MessagesRes.workitemClosedCannotAttachData(workitem.getDisplayID()));
                    continue;
                }

                final Collection<BuildRun> completedIn = workitem.getCompletedIn();
                final List<BuildRun> toRemove = new ArrayList<BuildRun>(completedIn.size());

                changeSet.getPrimaryWorkitems().add(workitem);
                logger.println("VersionOne: Added workitem " + workitem.getDisplayID() + " to changset");

                for (BuildRun otherRun : completedIn) {
                    if (otherRun.getBuildProject().equals(buildRun.getBuildProject())) {
                        toRemove.add(otherRun);
                    }
                }

                for (BuildRun buildRunDel : toRemove) {
                    completedIn.remove(buildRunDel);
                }

                completedIn.add(buildRun);
                logger.println("VersionOne: Added workitem " + workitem.getDisplayID() + " to buildrun");
            }
        }
    }

    private Set<PrimaryWorkitem> determineWorkitems(String comment) {
    	
    	logger.println("VersionOne: Processing changeset comment: " + comment + " with pattern " + config.pattern.toString());
        List<String> ids = getWorkitemsIds(comment, config.pattern);
        
        logger.println("VersionOne: Found " + ids.size() + " workitems to process");
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
     * @param comment string with some text with ids of tasks which cut using pattern set in the reference expression attribute.
     * @param v1PatternCommit regular expression for comment parse and getting data from it.
     * @return list of ids.
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
