package com.versionone.integration.ciCommon;

import com.versionone.DB;
import com.versionone.apiclient.Asset;
import com.versionone.apiclient.Attribute;
import com.versionone.apiclient.Query;
import com.versionone.apiclient.exceptions.APIException;
import com.versionone.apiclient.exceptions.ConnectionException;
import com.versionone.apiclient.exceptions.OidException;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.filters.*;
import com.versionone.apiclient.interfaces.IAssetType;
import com.versionone.apiclient.interfaces.IAttributeDefinition;
import com.versionone.apiclient.interfaces.IServices;
import com.versionone.apiclient.services.QueryResult;
import com.versionone.jenkins.MessagesRes;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class V1Worker implements Worker {

    private final V1Config config;
    private final PrintStream logger;
    private final IServices services;

    public V1Worker(V1Config config, PrintStream logger) throws V1Exception, MalformedURLException {
        this.config = config;
        this.logger = logger;
        services = config.getV1Services();
    }

    /**
     * Adds to the VersionOne BuildRun and ChangesSet.
     */
    public Result submitBuildRun(final BuildInfo info) throws V1Exception, MalformedURLException {
        //Find a matching BuildProject.

        final List<Asset> buildProjects = getBuildProjects(info);

        //Validate that BuildProject exists.
        if (buildProjects == null || buildProjects.isEmpty()) {
            logger.println("VersionOne: No matching BuildProject found in VersionOne. Name of the VersionOne pipeline has to match with name of this job.");
            return Result.FAIL_NO_BUILDPROJECT;
        }
        for(Asset buildProject : buildProjects) {
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
                setChangeSets(buildRun, info, buildProject.getAttribute(
                        buildProject.getAssetType().getAttributeDefinition("Reference")).getValue().toString());
            }
        }

        return Result.SUCCESS;
    }

    private static String getBuildName(final BuildInfo info) {
        return info.getProjectName() + " - build." + info.getBuildName();
    }

    private boolean isBuildExist(Asset buildProject, BuildInfo info) throws V1Exception, MalformedURLException {
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

    private List<Asset> getBuildProjects(BuildInfo info) throws V1Exception, MalformedURLException {
        IAssetType buildProject = services.getMeta().getAssetType("BuildProject");
        Query query = new Query(buildProject);
        IAttributeDefinition referenceAttrDef = buildProject.getAttributeDefinition("Reference");
        FilterTerm filter = new FilterTerm(referenceAttrDef);
        filter.equal(info.getProjectName());

        query.setFilter(filter);
        IAttributeDefinition workItemNumberAttrDef = buildProject.getAttributeDefinition("Scopes.Workitems.Number");
        query.getSelection().add(workItemNumberAttrDef);
        query.getSelection().add(referenceAttrDef);

        QueryResult result = services.retrieve(query);

        if (result.getAssets().length == 0) {
            return null;
        }

        return Arrays.asList(result.getAssets());
    }

    private Asset createBuildRun(Asset buildProject, BuildInfo info) throws V1Exception, MalformedURLException {
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

        buildRun.setAttributeValue(buildRunSourceAttrDef, getSourceOID(info.isForced()));
        buildRun.setAttributeValue(buildRunStatusAttrDef, getStatusOID(info.isSuccessful()));

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

    private String getSourceOID (boolean isForced) throws ConnectionException, APIException, OidException {
        IAssetType buildSourceAssetType = services.getAssetType("BuildSource");
        Query query = new Query(buildSourceAssetType);

        FilterTerm nameFilter = new FilterTerm(buildSourceAssetType.getAttributeDefinition("Name"));
        nameFilter.equal(isForced ? "Forced" : "Trigger");

        query.setFilter(nameFilter);
        QueryResult result = services.retrieve(query);
        return result.getAssets()[0].getOid().toString();
    }

    private String getStatusOID(boolean isSuccessful) throws ConnectionException, APIException, OidException {
        IAssetType buildSourceAssetType = services.getAssetType("BuildStatus");
        Query query = new Query(buildSourceAssetType);

        FilterTerm nameFilter = new FilterTerm(buildSourceAssetType.getAttributeDefinition("Name"));
        nameFilter.equal(isSuccessful ? "Passed" : "Failed");

        query.setFilter(nameFilter);
        QueryResult result = services.retrieve(query);
        return result.getAssets()[0].getOid().toString();
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

    private void setChangeSets(Asset buildRun, BuildInfo info, String buildProjectReference) throws V1Exception, MalformedURLException {
        for (VcsModification change : info.getChanges()) {

            logger.println("VersionOne: Processing changeset: " + change.getId());

            //See if we have this ChangeSet in the system.
            IAssetType changeSetType = services.getAssetType("ChangeSet");
            Query query = new Query(changeSetType);
            FilterTerm referenceFilter = new FilterTerm(changeSetType.getAttributeDefinition("Reference"));
            referenceFilter.equal(change.getId());
            IAttributeDefinition changeSetNameAttrDef = changeSetType.getAttributeDefinition("Name");
            FilterTerm nameFilter = new FilterTerm(changeSetNameAttrDef);
            nameFilter.equal(buildChangeSetName(change));

            query.getSelection().add(changeSetNameAttrDef);
            query.setFilter(new AndFilterTerm(referenceFilter, nameFilter));

            QueryResult result = services.retrieve(query);
            Collection<Asset> changeSetList = Arrays.asList(result.getAssets());

            if (changeSetList.isEmpty()) {

                //We don't have one yet. Create one.
                String name = buildChangeSetName(change);

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
            Set<Asset> workitems = determineWorkitems(change.getComment(), buildProjectReference);

            if (!workitems.isEmpty()) {
                logger.println("VersionOne: Associating " + changeSetList.size() + " changesets and " + workitems.size() + " workitems to buildrun");
                associateWithBuildRun(buildRun, changeSetList, workitems);
            }
        }
    }

    private String buildChangeSetName(VcsModification change) {
        StringBuilder name = new StringBuilder();
        name.append('\'');
        name.append(change.getUserName());
        if (change.getDate() != null) {
            name.append("\' on \'");
            name.append(new DB.DateTime(change.getDate()));
        }
        name.append('\'');
        return name.toString();
    }

    private void associateWithBuildRun(Asset buildRun, Collection<Asset> changeSets, Set<Asset> workitems) throws V1Exception, MalformedURLException {
        IAttributeDefinition buildRunChangeSetsAttrDef = buildRun.getAssetType().getAttributeDefinition("ChangeSets");
        for (Asset changeSet : changeSets) {
            IAttributeDefinition changeSetNameAttrDef = changeSet.getAssetType().getAttributeDefinition("Name");
            IAttributeDefinition changeSetPrimaryWorkitemsAttrDef = changeSet.getAssetType().getAttributeDefinition("PrimaryWorkitems");

            buildRun.addAttributeValue(buildRunChangeSetsAttrDef, changeSet.getOid());

            logger.println("VersionOne: Added changeset " + changeSet.getAttribute(changeSetNameAttrDef).getValue());

            for (Asset workitem : workitems) {
                IAttributeDefinition workItemIsClosedAttrDef = workitem.getAssetType().getAttributeDefinition("IsClosed");
                IAttributeDefinition workItemCompletedInBuildRunsAttrDef = workitem.getAssetType().getAttributeDefinition("CompletedInBuildRuns");

                if (Boolean.parseBoolean(workitem.getAttribute(workItemIsClosedAttrDef).getValue().toString())) {
                    logger.println("VersionOne: " + MessagesRes.workitemClosedCannotAttachData(getWorkitemDisplayString(workitem)));
                    continue;
                }

                changeSet.addAttributeValue(changeSetPrimaryWorkitemsAttrDef, workitem.getOid());
                services.save(changeSet);
                logger.println("VersionOne: Added workitem " + getWorkitemDisplayString(workitem) + " to changset");

                Query query = new Query(buildRun.getAssetType());
                IAttributeDefinition buildRunBuildProjectAttrDef = buildRun.getAssetType().getAttributeDefinition("BuildProject");
                query.getSelection().add(buildRunBuildProjectAttrDef);
                List<IFilterTerm> filterTerms = new ArrayList<IFilterTerm>();

                Object[] values = workitem.getAttribute(workItemCompletedInBuildRunsAttrDef).getValues();
                if (!filterTerms.isEmpty()) {
                    for (Object buildRunOid : values) {
                        FilterTerm filter = new FilterTerm(buildRun.getAssetType().getAttributeDefinition("ID"));
                        filter.equal(buildRunOid);
                        filterTerms.add(filter);
                    }
                    query.setFilter(new OrFilterTerm(filterTerms.toArray(new IFilterTerm[filterTerms.size()])));
                    QueryResult queryResult = services.retrieve(query);

                    for (Asset otherRun : queryResult.getAssets()) {
                        Object buildRunBuildProject = buildRun.getAttribute(buildRunBuildProjectAttrDef).getValue();
                        if (otherRun.getAttribute(buildRunBuildProjectAttrDef).getValue().equals(buildRunBuildProject)) {
                            workitem.removeAttributeValue(workItemCompletedInBuildRunsAttrDef, buildRun.getOid());
                        }
                    }
                }

                workitem.addAttributeValue(workItemCompletedInBuildRunsAttrDef, buildRun.getOid());
                services.save(workitem);
                services.save(buildRun);
                logger.println("VersionOne: Added workitem " + getWorkitemDisplayString(workitem) + " to buildrun");
            }
        }
    }

    private Set<Asset> determineWorkitems(String comment, String buildProjectReference) throws V1Exception, MalformedURLException {

        logger.println("VersionOne: Processing changeset comment: " + comment + " with pattern " + config.pattern.toString());
        List<String> ids = getWorkitemsIds(comment, config.pattern);

        logger.println("VersionOne: Found " + ids.size() + " workitems to process");
        Set<Asset> result = new HashSet<Asset>(ids.size());

        for (String id : ids) {
            result.addAll(getPrimaryWorkitemsByReference(id, buildProjectReference));
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
    private List<Asset> getPrimaryWorkitemsByReference(String reference, String buildProjectReference) throws V1Exception, MalformedURLException {
        List<Asset> result = new ArrayList<Asset>();

        IAssetType primaryWorkItemType = services.getAssetType("PrimaryWorkitem");
        Query query = new Query(primaryWorkItemType);

        IAttributeDefinition completedInBuildRunsAttribute = primaryWorkItemType.getAttributeDefinition("CompletedInBuildRuns");
        IAttributeDefinition isClosedAttribute = primaryWorkItemType.getAttributeDefinition("IsClosed");
        IAttributeDefinition childrenAttribute = primaryWorkItemType.getAttributeDefinition("Children.Number");
        IAttributeDefinition numberAttribute = primaryWorkItemType.getAttributeDefinition("Number");
        IAttributeDefinition scopeAttribute = primaryWorkItemType.getAttributeDefinition("Scope");
        IAttributeDefinition buildProjectAttribute = primaryWorkItemType.getAttributeDefinition("Scope.BuildProjects.Reference");
        IAttributeDefinition buildProjectScopesAttribute = primaryWorkItemType.getAttributeDefinition("Scope.BuildProjects.Scopes");

        query.getSelection().add(completedInBuildRunsAttribute);
        query.getSelection().add(isClosedAttribute);
        query.getSelection().add(childrenAttribute);
        query.getSelection().add(numberAttribute);
        query.getSelection().add(buildProjectScopesAttribute);
        query.getSelection().add(scopeAttribute);

        FilterTerm filter = new FilterTerm(primaryWorkItemType.getAttributeDefinition(config.referenceField));
        filter.equal(reference);
        FilterTerm filter2 = new FilterTerm(childrenAttribute);
        filter2.equal(reference);
        FilterTerm filter3 = new FilterTerm(buildProjectAttribute);
        filter3.equal(buildProjectReference);

        query.setFilter(new AndFilterTerm(new OrFilterTerm(filter,filter2), filter3));
        QueryResult queryResult = services.retrieve(query);

        // discard workitems from other pipelines
        for(Asset primaryWorkitem : queryResult.getAssets()) {
            List<Object> scopes = Arrays.asList(primaryWorkitem.getAttribute(buildProjectScopesAttribute).getValues());
            if (scopes.contains(primaryWorkitem.getAttribute(scopeAttribute).getValue()))
                result.add(primaryWorkitem);
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
        IAssetType workitemAssetType = services.getAssetType("Workitem");
        IAttributeDefinition workitemNameAttrDef = workitemAssetType.getAttributeDefinition("Name");
        IAttributeDefinition workitemNumberAttrDef = workitemAssetType.getAttributeDefinition("Number");

        Query query = new Query(workitemAssetType);
        query.getSelection().add(workitemNameAttrDef);
        FilterTerm filter = new FilterTerm(workitemNumberAttrDef);
        filter.equal(id);
        query.setFilter(filter);
        QueryResult queryResult = services.retrieve(query);

        Asset workitem = queryResult.getAssets()[0];
        return new WorkitemData(workitem.getOid().toString(),
                workitem.getAttribute(workitemNameAttrDef).getValue().toString(),
                config.url);
    }

    private String getWorkitemDisplayString(Asset workitem) throws APIException {
        IAssetType assetType = workitem.getAssetType();
        Attribute number = workitem.getAttribute(assetType.getAttributeDefinition("Number"));

        return String.format("%s (%s)", workitem.getOid(), number.getValue().toString());
    }
}
