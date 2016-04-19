package com.versionone.integration.ciCommon;

import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.versionone.DB;

import com.versionone.apiclient.filters.*;
import com.versionone.apiclient.interfaces.IAttributeDefinition;
import com.versionone.jenkins.MessagesRes;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.interfaces.IAssetType;
import com.versionone.apiclient.interfaces.IServices;
import com.versionone.apiclient.services.QueryResult;
import com.versionone.apiclient.*;

import java.net.MalformedURLException;


public class V1Worker implements Worker {

    private final V1Config config;
    private final PrintStream logger;

    public V1Worker(V1Config config, PrintStream logger) throws V1Exception, MalformedURLException {
        this.config = config;
        this.logger = logger;
    }

    /**
     * Adds to the VersionOne BuildRun and ChangesSet.
     */
    public Result submitBuildRun(final BuildInfo info) throws V1Exception, MalformedURLException {
        //Validate connection to V1.
        // if (!config.isConnectionValid()) {
        // 	logger.println("VersionOne: Connection to VersionOne failed");
        //     return Result.FAIL_CONNECTION;
        // }
        // logger.println("VersionOne: Connection to VersionOne succeeded");

        //Find a matching BuildProject.

        final Asset buildProject = getBuildProject(info);

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
        final Asset buildRun = createBuildRun(buildProject, info);
        IAttributeDefinition buildRunNameAttrDef = buildRun.getAssetType().getAttributeDefinition("Name");
        logger.println("VersionOne: Created BuildRun " + buildRun.getAttribute(buildRunNameAttrDef));

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

    private boolean isBuildExist(Asset buildProject, BuildInfo info) throws V1Exception, MalformedURLException {
        IServices services = config.getV1Services();
        IAssetType buildRunAssetType = services.getAssetType("BuildRun");

        Query query = new Query(buildRunAssetType);

        FilterTerm filterReference = new FilterTerm(buildRunAssetType.getAttributeDefinition("Reference"));
        filterReference.equal(Long.toString(info.getBuildId()));
        FilterTerm filterName = new FilterTerm(buildRunAssetType.getAttributeDefinition("Name"));
        filterName.equal(getBuildName(info));
        FilterTerm filterBuildProject = new FilterTerm(buildRunAssetType.getAttributeDefinition("BuildProject"));
        filterBuildProject.equal(buildProject.getOid());

        GroupFilterTerm groupFilter = new AndFilterTerm(filterReference, filterName, filterBuildProject);
        query.setFilter(groupFilter);

        QueryResult result = config.getV1Services().retrieve(query);

        return result.getAssets() != null && result.getAssets().length != 0;
    }

    /**
     * Find the first BuildProject where the Reference matches the projectName.
     *
     * @param info information about build run
     * @return V1 representation of the project if match; otherwise - null.
     */

    private Asset getBuildProject(BuildInfo info) throws V1Exception, MalformedURLException {
        IServices services = config.getV1Services();
        IAssetType buildProject = services.getMeta().getAssetType("BuildProject");
        Query query = new Query(buildProject);
        FilterTerm filter = new FilterTerm(buildProject.getAttributeDefinition("Reference"));
        filter.equal(info.getProjectName());

        query.setFilter(filter);
        QueryResult result = services.retrieve(query);

        if (result.getAssets().length == 0) {
            return null;
        }
        return result.getAssets()[0];
    }


    private Asset createBuildRun(Asset buildProject, BuildInfo info) throws V1Exception, MalformedURLException {

        IServices services = config.getV1Services();
        //Generate the BuildRun asset.
        IAssetType buildRunType = services.getAssetType("BuildRun");
        IAttributeDefinition buildRunNameAttrDef = buildRunType.getAttributeDefinition("Name");
        IAttributeDefinition buildRunDateAttrDef = buildRunType.getAttributeDefinition("Date");
        IAttributeDefinition buildRunElapsedAttrDef = buildRunType.getAttributeDefinition("Elapsed");
        IAttributeDefinition buildRunReferenceAttrDef = buildRunType.getAttributeDefinition("Reference");
        IAttributeDefinition buildRunSourceAttrDef = buildRunType.getAttributeDefinition("Source");
        IAttributeDefinition buildRunStatusAttrDef = buildRunType.getAttributeDefinition("Status");

        Asset buildRun = services.createNew(buildRunType, buildProject.getOid());
        buildRun.setAttributeValue(buildRunNameAttrDef, getBuildName(info));
        buildRun.setAttributeValue(buildRunDateAttrDef, new DB.DateTime(info.getStartTime()));
        buildRun.setAttributeValue(buildRunElapsedAttrDef, (double) info.getElapsedTime());
        buildRun.setAttributeValue(buildRunReferenceAttrDef, Long.toString(info.getBuildId()));
        buildRun.setAttributeValue(buildRunSourceAttrDef, getSourceName(info.isForced()));
        buildRun.setAttributeValue(buildRunStatusAttrDef, getStatusName(info.isSuccessful()));

        if (info.hasChanges()) {
            IAttributeDefinition descriptionAttrDef = buildRunType.getAttributeDefinition("Description");
            buildRun.setAttributeValue(descriptionAttrDef, getModificationDescription(info.getChanges()));
        }

        services.save(buildRun);

        IAssetType linkType = services.getAssetType("Link");
        IAttributeDefinition linkAssetAttrDef = linkType.getAttributeDefinition("Asset");
        IAttributeDefinition linkNameAttrDef = linkType.getAttributeDefinition("Name");
        IAttributeDefinition linkUrlAttrDef = linkType.getAttributeDefinition("URL");
        IAttributeDefinition linkOnMenuAttrDef = linkType.getAttributeDefinition("OnMenu");

        Asset link = services.createNew(linkType, null);
        link.setAttributeValue(linkAssetAttrDef, buildRun.getOid());
        link.setAttributeValue(linkNameAttrDef, "Build Report");
        link.setAttributeValue(linkUrlAttrDef, info.getUrl());
        link.setAttributeValue(linkOnMenuAttrDef, true);

        services.save(link);

        return buildRun;
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
        for (Iterator<VcsModification> it = changes.iterator(); it.hasNext(); ) {
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

    private void setChangeSets(Asset buildRun, BuildInfo info) throws V1Exception, MalformedURLException {
        IServices services = config.getV1Services();
        for (VcsModification change : info.getChanges()) {

            logger.println("VersionOne: Processing changeset: " + change.getId());

            //See if we have this ChangeSet in the system.
            IAssetType changeSetType = services.getAssetType("ChangeSet");
            Query query = new Query(changeSetType);
            FilterTerm filter = new FilterTerm(changeSetType.getAttributeDefinition("Reference"));
            filter.equal(change.getId());
            query.setFilter(filter);

            QueryResult result = services.retrieve(query);
            Collection<Asset> changeSetList = null;

            if (result.getAssets().length == 0) {

                //We don't have one yet. Create one.
                StringBuilder name = new StringBuilder();
                name.append('\'');
                name.append(change.getUserName());
                if (change.getDate() != null) {
                    name.append("\' on \'");
                    name.append(new DB.DateTime(change.getDate()));
                }
                name.append('\'');

                IAttributeDefinition changeSetNameAttrDef = changeSetType.getAttributeDefinition("Name");
                IAttributeDefinition changeSetReferenceAttrDef = changeSetType.getAttributeDefinition("Reference");
                IAttributeDefinition changeSetDescriptionAttrDef = changeSetType.getAttributeDefinition("Description");

                Asset changeSet = services.createNew(changeSetType, null);
                changeSet.setAttributeValue(changeSetNameAttrDef, name.toString());
                changeSet.setAttributeValue(changeSetReferenceAttrDef, change.getId());
                changeSet.setAttributeValue(changeSetDescriptionAttrDef, change.getComment());

                services.save(changeSet);

                logger.println("VersionOne: Created changeset: " + changeSet.getAttribute(changeSetNameAttrDef).getValue());

                changeSetList = new ArrayList<Asset>(1);
                changeSetList.add(changeSet);
            }

            Set<Asset> workitems = determineWorkitems(change.getComment());

            logger.println("VersionOne: Associating " + changeSetList.size() + " changesets and " + workitems.size() + " workitems to buildrun");
            associateWithBuildRun(buildRun, changeSetList, workitems);
        }
    }

    private void associateWithBuildRun(Asset buildRun, Collection<Asset> changeSets, Set<Asset> workitems) throws V1Exception, MalformedURLException {
        IServices services = config.getV1Services();
        IAttributeDefinition buildRunChangeSetsAttrDef = buildRun.getAssetType().getAttributeDefinition("ChangeSets");
        //List<Object> buildRunChangeSets = Arrays.asList(buildRun.getAttribute(buildRunChangeSetsAttrDef).getValues());
        for (Asset changeSet : changeSets) {
            IAttributeDefinition changeSetNameAttrDef = changeSet.getAssetType().getAttributeDefinition("Name");
            IAttributeDefinition changeSetPrimaryWorkitemsAttrDef = changeSet.getAssetType().getAttributeDefinition("PrimaryWorkitems ");

            buildRun.addAttributeValue(buildRunChangeSetsAttrDef, changeSet.getOid());

            logger.println("VersionOne: Added changeset " + changeSet.getAttribute(changeSetNameAttrDef).getValue());

            for (Asset workitem : workitems) {
                IAttributeDefinition workItemIsClosedAttrDef = workitem.getAssetType().getAttributeDefinition("IsClosed");
                IAttributeDefinition workItemCompletedInBuildRunsAttrDef = workitem.getAssetType().getAttributeDefinition("CompletedInBuildRuns");

                if (Boolean.parseBoolean(workitem.getAttribute(workItemIsClosedAttrDef).getValue().toString())) {
                    logger.println("VersionOne: " + MessagesRes.workitemClosedCannotAttachData(workitem.getOid().toString()));
                    continue;
                }

                changeSet.addAttributeValue(changeSetPrimaryWorkitemsAttrDef, workitem.getOid());
                services.save(changeSet);
                logger.println("VersionOne: Added workitem " + workitem.getOid() + " to changset");

                Query query = new Query(buildRun.getAssetType());
                IAttributeDefinition buildRunBuildProjectAttrDef = buildRun.getAssetType().getAttributeDefinition("BuildProject");
                query.getSelection().add(buildRunBuildProjectAttrDef);
                List<IFilterTerm> filterTerms = new ArrayList<IFilterTerm>();

                for (Object buildRunOid : workitem.getAttribute(workItemCompletedInBuildRunsAttrDef).getValues()) {
                    FilterTerm filter = new FilterTerm(buildRun.getAssetType().getAttributeDefinition("ID"));
                    filter.equal(buildRunOid);
                    filterTerms.add(filter);
                }

                query.setFilter(new OrFilterTerm(filterTerms.toArray(new IFilterTerm[filterTerms.size()])));
                QueryResult queryResult = services.retrieve(query);

                for (Asset otherRun : queryResult.getAssets()) {
                    Object buildRunBuildProject = buildRun.getAttribute(buildRunBuildProjectAttrDef).getValue();
                    if (otherRun.getAttribute(buildRunBuildProjectAttrDef).getValue().equals(buildRunBuildProject)) {
                        workitem.removeAttributeValue(workItemCompletedInBuildRunsAttrDef, buildRunBuildProject);
                    }

                }


                workitem.addAttributeValue(workItemCompletedInBuildRunsAttrDef, buildRun.getOid());
                services.save(workitem);
                logger.println("VersionOne: Added workitem " + workitem.getOid() + " to buildrun");
            }
        }
    }

    private Set<Asset> determineWorkitems(String comment) throws V1Exception, MalformedURLException {

        logger.println("VersionOne: Processing changeset comment: " + comment + " with pattern " + config.pattern.toString());
        List<String> ids = getWorkitemsIds(comment, config.pattern);

        logger.println("VersionOne: Found " + ids.size() + " workitems to process");
        Set<Asset> result = new HashSet<Asset>(ids.size());

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
    private List<Asset> getPrimaryWorkitemsByReference(String reference) throws V1Exception, MalformedURLException {
        List<Asset> result = new ArrayList<Asset>();
        IServices services = config.getV1Services();

        IAssetType workItemType = services.getAssetType("Workitem");
        IAssetType primaryWorkItemType = services.getAssetType("PrimaryWorkitem");

        IAttributeDefinition primaryWorkItemParentAttrDef = primaryWorkItemType.getAttributeDefinition("Parent");

        Query query = new Query(workItemType);

        FilterTerm filter = new FilterTerm(workItemType.getAttributeDefinition(config.referenceField));
        filter.equal(reference);
        query.setFilter(filter);

        QueryResult queryResult = services.retrieve(query);

        for (Asset workitem : queryResult.getAssets()) {
            if (workitem.getAssetType().getBase().getToken().equals("PrimaryWorkitem")) {
                result.add(workitem);
            } else if (workitem.getAssetType().getBase().getToken().equals("Workitem")) {
                Query query2 = new Query(services.getOid(workitem.getAttribute(primaryWorkItemParentAttrDef).getValue().toString()));
                QueryResult queryResult2 = services.retrieve(query2);

                result.add(queryResult2.getAssets()[0]);
            } else {
                throw new RuntimeException("Found unexpected Workitem type: " + workitem.getAssetType().getBase().getToken());
            }
            result.add(workitem);
        }

        return result;
    }

    /**
     * Return list of workitems got from the comment string.
     *
     * @param comment         string with some text with ids of tasks which cut using pattern set in the reference expression attribute.
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
    public WorkitemData getWorkitemData(String id) throws V1Exception, MalformedURLException {
        IServices services = config.getV1Services();
        IAssetType workitemAssetType = services.getAssetType("Workitem");
        IAttributeDefinition workitemNameAttrDef = workitemAssetType.getAttributeDefinition("Name");
        IAttributeDefinition workitemNumberAttrDef = workitemAssetType.getAttributeDefinition("Number");

        Query query = new Query(workitemAssetType);
        query.getSelection().add(workitemNameAttrDef);
        FilterTerm filter = new FilterTerm(workitemNumberAttrDef);
        filter.equal(id);

        QueryResult queryResult = services.retrieve(query);

        Asset workitem = queryResult.getAssets()[0];
        return new WorkitemData(workitem.getOid().toString(),
                workitem.getAttribute(workitemNameAttrDef).getValue().toString(),
                config.url);
    }
}
