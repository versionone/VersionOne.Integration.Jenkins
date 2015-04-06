/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.hudson;

import com.versionone.apiclient.IMetaModel;
import com.versionone.apiclient.MetaException;
import com.versionone.integration.ciCommon.BuildInfo;
import com.versionone.integration.ciCommon.V1Config;
import com.versionone.integration.ciCommon.V1Worker;
import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.ProxySettings;
import com.versionone.om.V1Instance;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.RepositoryBrowser;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class VersionOneNotifier extends Notifier {

    @Extension
    public static final Descriptor DESCRIPTOR = new Descriptor();

    @Override
    public Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        Descriptor descriptor = getDescriptor();
        V1Config config = new V1Config(descriptor.getV1Path(), descriptor.getV1Username(), descriptor.getV1Password(),
                descriptor.getV1Pattern(), descriptor.getV1RefField(), false,
                descriptor.getV1UseProxy(), descriptor.getV1ProxyUrl(), descriptor.getV1ProxyUsername(), descriptor.getV1ProxyPassword());
        config.setLogger(listener.getLogger());
        V1Worker worker = new V1Worker(config, listener.getLogger(), getRepositoryBrowser(build));

        for (ChangeLogAnnotator annot : ChangeLogAnnotator.all()) {
            if (annot instanceof HudsonChangeLogAnnotator) {
                ((HudsonChangeLogAnnotator) annot).setData(worker, config.pattern);
            }
        }
        BuildInfo buildInfo = new HudsonBuildInfo(build);

        switch (worker.submitBuildRun(buildInfo)) {
            case SUCCESS:
                listener.getLogger().println(MessagesRes.processSuccess());
                break;
            case FAIL_CONNECTION:
                listener.getLogger().println(MessagesRes.connectionIsNotCorrect());
                break;
            case FAIL_DUPLICATE:
                listener.getLogger().println(MessagesRes.buildRunAlreadyExist());
                break;
            case FAIL_NO_BUILDPROJECT:
                listener.getLogger().println(MessagesRes.buildProjectNotFound());
                break;
        }
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    private RepositoryBrowser<?> getRepositoryBrowser(AbstractBuild<?, ?> build) {
        if (build.getProject().getScm() != null) {
            return build.getProject().getScm().getEffectiveBrowser();
        }
        return null;
    }

    public static final class Descriptor extends BuildStepDescriptor<Publisher> {

        private static final String V1_PATH = "v1Path";
        private static final String V1_USERNAME = "v1Username";
        private static final String V1_PASSWORD = "v1Password";
        private static final String V1_REF_FIELD = "v1RefField";
        private static final String V1_PATTERN = "v1Pattern";

        private static final String V1_USE_PROXY = "v1UseProxy";
        private static final String V1_PROXY_URL = "v1ProxyUrl";
        private static final String V1_PROXY_USERNAME = "v1ProxyUsername";
        private static final String V1_PROXY_PASSWORD = "v1ProxyPassword";

        private String v1Path;
        private String v1Username;
        private String v1Password;
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
         * @param username user name to VersionOne
         * @param password password to VersionOne
         * @param refField field will be used to connect buildruns and changesets to story
         * @return validation result.
         */
        public FormValidation doTestConnection(StaplerRequest req, StaplerResponse rsp,
                                               @QueryParameter(V1_PATH) final String path,
                                               @QueryParameter(V1_USERNAME) final String username,
                                               @QueryParameter(V1_PASSWORD) final String password,
                                               @QueryParameter(V1_REF_FIELD) final String refField,
                                               @QueryParameter(V1_USE_PROXY) final boolean useProxy,
                                               @QueryParameter(V1_PROXY_URL) final String proxyUrl,
                                               @QueryParameter(V1_PROXY_USERNAME) final String proxyUsername,
                                               @QueryParameter(V1_PROXY_PASSWORD) final String proxyPassword) {
            try {
                ProxySettings proxySettings = null;

                if(useProxy) {
                    proxySettings = new ProxySettings(createUri(proxyUrl), proxyUsername, proxyPassword);
                }

                final V1Instance v1 = new V1Instance(path, username, password, proxySettings);
                v1.validate();
                final IMetaModel meta = v1.getApiClient().getMetaModel();
                meta.getAssetType("PrimaryWorkitem").getAttributeDefinition(refField);
                return FormValidation.ok(MessagesRes.connectionValid());
            } catch(URISyntaxException e) {
                return FormValidation.error(MessagesRes.connectionFailedProxyUrlMalformed());
            } catch (ApplicationUnavailableException e) {
                return FormValidation.error(MessagesRes.connectionFailedPath());
            } catch (AuthenticationException e) {
                return FormValidation.error(MessagesRes.connectionFailedUsername());
            } catch (MetaException e) {
                return FormValidation.error(MessagesRes.connectionFailedRefField(refField));
            }
        }

        private URI createUri(String uriString) throws URISyntaxException {
            return new URI(uriString);
        }

        public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
            v1Path = o.getString(V1_PATH);
            v1Username = o.getString(V1_USERNAME);
            v1Password = o.getString(V1_PASSWORD);
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

        public String getV1Username() {
            return v1Username;
        }

        public String getV1Password() {
            return v1Password;
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

        void setData(String v1Path, String v1Username, String v1Password, String v1RefField, String v1Pattern) {
            this.v1Path = v1Path;
            this.v1Username = v1Username;
            this.v1Password = v1Password;
            this.v1RefField = v1RefField;
            this.v1Pattern = v1Pattern;
        }
    }
}