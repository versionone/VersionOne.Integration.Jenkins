package com.versionone.jenkins;

import com.versionone.apiclient.ProxyProvider;
import com.versionone.apiclient.Query;
import com.versionone.apiclient.Services;
import com.versionone.apiclient.V1Connector;
import com.versionone.apiclient.filters.FilterTerm;
import com.versionone.apiclient.interfaces.*;
import com.versionone.apiclient.exceptions.*;
import com.versionone.integration.ciCommon.BuildInfo;
import com.versionone.integration.ciCommon.V1Config;
import com.versionone.integration.ciCommon.V1Worker;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.ChangeLogAnnotator;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class VersionOneNotifier extends Notifier {

    @Extension
    public static final Descriptor DESCRIPTOR = new Descriptor();

    @Override
    public Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    private PrintStream logger;

    /*
     * Entry point for Jenkins to call the integration.
     *
     * @see hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)
     */
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        logger = listener.getLogger();

        logger.println("VersionOne: Integration initialized");
        Descriptor descriptor = getDescriptor();

        logger.println("About to configure with:");
        logger.printf("Path: %s. \n",descriptor.getV1Path());
        logger.printf("Access Token: %s. \n",descriptor.getV1AccessToken());
        logger.printf("Pattern: %s. \n",descriptor.getV1Pattern());
        logger.printf("RefField: %s. \n",descriptor.getV1RefField());
        V1Config config = new V1Config(descriptor.getV1Path(), descriptor.getV1AccessToken(),
                descriptor.getV1Pattern(), descriptor.getV1RefField(), false,
                descriptor.getV1UseProxy(), descriptor.getV1ProxyUrl(), descriptor.getV1ProxyUsername(), descriptor.getV1ProxyPassword());

        config.setLogger(logger);

        V1Worker worker = null;
        try {
            worker = new V1Worker(config, logger);
        } catch (Exception e) {
            e.printStackTrace(logger);
        }

        for (ChangeLogAnnotator annot : ChangeLogAnnotator.all()) {
            if (annot instanceof JenkinsChangeLogAnnotator) {
                ((JenkinsChangeLogAnnotator) annot).setData(worker, config.pattern);
            }
        }

        BuildInfo buildInfo = new JenkinsBuildInfo(build, logger);
        logger.println("VersionOne: Processing build " + buildInfo.getBuildId() + ":" + buildInfo.getBuildName());

        try {
            switch (worker.submitBuildRun(buildInfo)) {
                case SUCCESS:
                    logger.println(MessagesRes.processSuccess());
                    break;
                case FAIL_CONNECTION:
                    logger.println(MessagesRes.connectionIsNotCorrect());
                    break;
                case FAIL_DUPLICATE:
                    logger.println(MessagesRes.buildRunAlreadyExist());
                    break;
                case FAIL_NO_BUILDPROJECT:
                    logger.println(MessagesRes.buildProjectNotFound());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace(logger);
        }
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public static final class Descriptor extends BuildStepDescriptor<Publisher> {

        private static final String V1_PATH = "v1Path";
        private static final String V1_ACCESSTOKEN = "v1AccessToken";
        private static final String V1_REF_FIELD = "v1RefField";
        private static final String V1_PATTERN = "v1Pattern";

        private static final String V1_USE_PROXY = "v1UseProxy";
        private static final String V1_PROXY_URL = "v1ProxyUrl";
        private static final String V1_PROXY_USERNAME = "v1ProxyUsername";
        private static final String V1_PROXY_PASSWORD = "v1ProxyPassword";

        private String v1Path;
        private String v1AccessToken;
        private String v1RefField;
        private String v1Pattern;
        private boolean v1UseProxy;
        private String v1ProxyUrl;
        private String v1ProxyUsername;
        private String v1ProxyPassword;

        public Descriptor() {
            super(VersionOneNotifier.class);
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return MessagesRes.VersionOne_Notifier();
        }

        public String getHelpFile() {
            return "/plugin/versionone/help-projectSettings.html";
        }

        /**
         * Performs on-the-fly validation of the form field 'Pattern'.
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckV1Pattern(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error(MessagesRes.cannotBeEmpty());
            }
            try {
                Pattern.compile(value);
            } catch (PatternSyntaxException e) {
                return FormValidation.error(MessagesRes.pattternWrong());
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'path'.
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckV1Path(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.warning(MessagesRes.cannotBeEmpty());
            }
            try {
                new URL(value);
            } catch (MalformedURLException e) {
                return FormValidation.error(MessagesRes.pathWrong());
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'proxy URL'.
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckV1ProxyUrl(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.ok("Proxy server will not be used");
            }

            try {
                new URL(value);
            } catch (MalformedURLException e) {
                return FormValidation.error(MessagesRes.pathWrong());
            }

            return FormValidation.ok();
        }

        /**
         * Verify connection and field
         *
         * @param req      request
         * @param rsp      respond
         * @param path     path to the VersionOne
         * @param accessToken access token for VersionOne
         * @param refField field will be used to connect buildruns and changesets to story
         * @return validation result.
         */
        public FormValidation doTestConnection(StaplerRequest req, StaplerResponse rsp,
                                               @QueryParameter(V1_PATH) final String path,
                                               @QueryParameter(V1_ACCESSTOKEN) final String accessToken,
                                               @QueryParameter(V1_REF_FIELD) final String refField,
                                               @QueryParameter(V1_USE_PROXY) final boolean useProxy,
                                               @QueryParameter(V1_PROXY_URL) final String proxyUrl,
                                               @QueryParameter(V1_PROXY_USERNAME) final String proxyUsername,
                                               @QueryParameter(V1_PROXY_PASSWORD) final String proxyPassword) {

//            Logger logger = Logger.getLogger("MyLog");
//            FileHandler fh;

            try {
//                fh = new FileHandler("C:/MyLogFile.log");
//                logger.addHandler(fh);
//                SimpleFormatter formatter = new SimpleFormatter();
//                fh.setFormatter(formatter);

                V1Connector.IsetProxyOrEndPointOrConnector connectorBuilder = V1Connector
                        .withInstanceUrl(path)
                        .withUserAgentHeader("VersionOne.Integration.Jenkins", "0.1")
                        .withAccessToken(accessToken);

                if(useProxy) {
                    ProxyProvider proxyProvider = new ProxyProvider(createUri(proxyUrl), proxyUsername, proxyPassword);
                    connectorBuilder.withProxy(proxyProvider);
                }
                V1Connector connector = connectorBuilder.build();

                Services services = new Services(connector);
                IAssetType memberType = services.getAssetType("Member");
                Query query = new Query(memberType);
                IAttributeDefinition isSelfAttrDef = memberType.getAttributeDefinition("IsSelf");
                query.getSelection().add(isSelfAttrDef);
                FilterTerm filter = new FilterTerm(isSelfAttrDef);
                filter.equal(true);
                query.setFilter(filter);
                services.retrieve(query);

                return FormValidation.ok(MessagesRes.connectionValid());
            } catch(URISyntaxException e) {
                return FormValidation.error(MessagesRes.connectionFailedProxyUrlMalformed());
            } catch (MalformedURLException e) {
                return FormValidation.error(MessagesRes.connectionFailedPath());
            } catch (MetaException e) {
                return FormValidation.error(MessagesRes.connectionFailedRefField(refField));
            } catch (V1Exception e) {
                return FormValidation.error(MessagesRes.connectionFailedRefField(refField));
            } catch (Exception e) {
                return FormValidation.error(e.getMessage());
            }
        }

        private URI createUri(String uriString) throws URISyntaxException {
            return new URI(uriString);
        }

        public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
            v1Path = o.getString(V1_PATH);
            v1AccessToken = o.getString(V1_ACCESSTOKEN);
            v1RefField = o.getString(V1_REF_FIELD);
            v1Pattern = o.getString(V1_PATTERN);

            if(o.keySet().contains(V1_USE_PROXY)) {
                JSONObject proxySettings = o.getJSONObject(V1_USE_PROXY);

                v1ProxyUrl = proxySettings.get(V1_PROXY_URL).toString();
                v1ProxyUsername = proxySettings.get(V1_PROXY_USERNAME).toString();
                v1ProxyPassword = proxySettings.get(V1_PROXY_PASSWORD).toString();
                v1UseProxy = true;
            } else {
                v1UseProxy = false;
            }

            save();
            return true;
        }

        public String getV1Path() {
            return v1Path;
        }

        public String getV1AccessToken() {
            return v1AccessToken;
        }

        public String getV1RefField() {
            return v1RefField;
        }

        public String getV1Pattern() {
            return v1Pattern;
        }

        public boolean getV1UseProxy() {
            return v1UseProxy;
        }

        public String getV1ProxyUrl() {
            return v1ProxyUrl;
        }

        public String getV1ProxyUsername() {
            return v1ProxyUsername;
        }

        public String getV1ProxyPassword() {
            return v1ProxyPassword;
        }

        /**
         * Creates a new instance of {@link VersionOneNotifier} from a submitted form.
         */
        public VersionOneNotifier newInstance(StaplerRequest req, JSONObject formData) {
            return new VersionOneNotifier();
        }

        void setData(String v1Path, String v1AccessToken, String v1RefField, String v1Pattern) {
            this.v1Path = v1Path;
            this.v1AccessToken = v1AccessToken;
            this.v1RefField = v1RefField;
            this.v1Pattern = v1Pattern;
        }
    }
}