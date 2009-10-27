package com.versionone.hudson;

import com.versionone.apiclient.IMetaModel;
import com.versionone.apiclient.MetaException;
import com.versionone.integration.ciCommon.BuildInfo;
import com.versionone.integration.ciCommon.V1Config;
import com.versionone.integration.ciCommon.V1Worker;
import com.versionone.integration.ciCommon.VcsModification;
import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.V1Instance;
import com.versionone.om.PrimaryWorkitem;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.Set;

public class VersionOneNotifier extends Notifier {

    @Extension
    public static final Descriptor DESCRIPTOR = new Descriptor();

    @Override
    public Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        //System.setProperty("javax.xml.parsers.DocumentBuilderFactory","com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImp");


        //String name = System.getProperty("javax.xml.transform.TransformerFactory");
        //String name2 = System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
        V1Config config = new V1Config(getDescriptor().getV1Path(), getDescriptor().getV1Username(), getDescriptor().getV1Password(), getDescriptor().getV1Pattern(), getDescriptor().getV1RefField(), false);
        V1Worker worker = new V1Worker(config);
        BuildInfo buildInfo = new HudsonBuildInfo(build);
        listener.getLogger().println("hasChanges: " + buildInfo.hasChanges());
        for (VcsModification change : buildInfo.getChanges()) {
            listener.getLogger().println("id: " + change.getId());
            listener.getLogger().println("date: " + change.getDate());
            listener.getLogger().println("comment: " + change.getComment());
            listener.getLogger().println("user name: " + change.getUserName());
        }

        int result = worker.submitBuildRun(buildInfo);
        if (result == V1Worker.NOTIFY_SUCCESS) {
            listener.getLogger().println("Information was transfered to the VersionOne successfully");
        }



        /*
		final V1Instance instance = new V1Instance(getDescriptor().getV1Path(), getDescriptor().getV1Login(), getDescriptor().getV1Password());
		try {
			instance.validate();
			listener.getLogger().println("VersionOne connection validated.");
		} catch (ApplicationUnavailableException e) {
			listener.getLogger().println("VersionOne connection failed:");
			e.printStackTrace(listener.getLogger());
		} catch (AuthenticationException e) {
			listener.getLogger().println("VersionOne authentication failed:");
			e.printStackTrace(listener.getLogger());
		}

		listener.getLogger().println(getDescriptor().getV1Path());
		// this also shows how you can consult the global configuration of the builder

        BuildInfo buildInfo = new HudsonBuildInfo(build);
        listener.getLogger().println("Result: " + buildInfo.isSuccessful());
        listener.getLogger().println("BuildId: " + buildInfo.getBuildId());
        listener.getLogger().println("getElapsedTime: " + buildInfo.getElapsedTime());
        listener.getLogger().println("getStartTime: " + buildInfo.getStartTime());
        listener.getLogger().println("getBuildName: " + buildInfo.getBuildName());
        listener.getLogger().println("getProjectName: " + buildInfo.getProjectName());
        listener.getLogger().println("getUrl: " + buildInfo.getUrl());
        listener.getLogger().println("isForced: " + buildInfo.isForced());


        /*
		listener.getLogger().println("Result: " + build.getResult());
		listener.getLogger().println("Description: " + build.getComment());
		listener.getLogger().println("Project: " + build.getProject().getName());
		listener.getLogger().println("ChangeSet (kind): " + build.getChangeSet().getKind());
		GregorianCalendar now = new GregorianCalendar();

		listener.getLogger().println("Time: " + (now.getTime().getTime() - build.getTimestamp().getTime().getTime()));
		//SubversionChangeLogSet for Subversion
		for (Object item : build.getChangeSet().getItems()) {
			ChangeLogSet.Entry changeSetData = ((ChangeLogSet.Entry) item);
			listener.getLogger().println("Message: " + changeSetData.getMsg());
			listener.getLogger().println("Author: " + changeSetData.getAuthor());
			for (ChangeLogSet.AffectedFile file : changeSetData.getAffectedFiles()) {
				listener.getLogger().println("File: " + file.getPath());
			}
			//listener.getLogger().println("ChangeSet (Items): " + item.toString());
		}
		listener.getLogger().println("------------------------------------");
		// we can recognize who init this build user or triger by data in actions(build.getActions()):
		// hudson.model.Cause$UserCause - user
		// hudson.triggers.SCMTrigger$SCMTriggerCause - trigger by Subversion update
		// build.getActions().get(0); or verify all data in loop
		*/


        return true;
    }

    public static final class Descriptor extends BuildStepDescriptor<Publisher> {

        private static final String V1_PATH = "v1Path";
        private static final String V1_USERNAME = "v1Username";
        private static final String V1_PASSWORD = "v1Password";
        private static final String V1_REF_FIELD = "v1RefField";
        private static final String V1_PATTERN = "v1Pattern";

        private String v1Path;
        private String v1Username;
        private String v1Password;
        private String v1RefField;
        private String v1Pattern;

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
            return "/plugin/hudson-notifier/help-projectSettings.html";
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

        public FormValidation doTestConnection(StaplerRequest req, StaplerResponse rsp,
                                               @QueryParameter(V1_PATH) final String path,
                                               @QueryParameter(V1_USERNAME) final String username,
                                               @QueryParameter(V1_PASSWORD) final String password,
                                               @QueryParameter(V1_REF_FIELD) final String refField) {
            try {
                final V1Instance v1 = new V1Instance(path, username, password);
                v1.validate();
                final IMetaModel meta = v1.getApiClient().getMetaModel();
                meta.getAssetType("PrimaryWorkitem").getAttributeDefinition(refField);
                return FormValidation.ok(MessagesRes.connectionValid());
            } catch (ApplicationUnavailableException e) {
                return FormValidation.error(MessagesRes.connectionFailedPath());
            } catch (AuthenticationException e) {
                return FormValidation.error(MessagesRes.connectionFailedUsername());
            } catch (MetaException e) {
                return FormValidation.error(MessagesRes.connectionFailedRefField());
            }
        }

        public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
            v1Path = o.getString(V1_PATH);
            v1Username = o.getString(V1_USERNAME);
            v1Password = o.getString(V1_PASSWORD);
            v1RefField = o.getString(V1_REF_FIELD);
            v1Pattern = o.getString(V1_PATTERN);
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

        /**
         * Creates a new instance of {@link VersionOneNotifier} from a submitted form.
         */
        public VersionOneNotifier newInstance(StaplerRequest req, JSONObject formData) {
            return new VersionOneNotifier();
        }
    }
}