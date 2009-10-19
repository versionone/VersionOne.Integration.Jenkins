package com.versionone.hudson;

import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.V1Instance;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.scm.ChangeLogSet;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.GregorianCalendar;

public class VersionOneNotifier extends Notifier {

	@Extension
	public static final Descriptor DESCRIPTOR = new Descriptor();

	@Override
	public Descriptor getDescriptor() {
		return DESCRIPTOR;
	}

	// This is where you 'build' the project
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		final V1Instance instance = new V1Instance(getDescriptor().getV1Path(), "admin", "admin");
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
		listener.getLogger().println("Result: " + build.getResult());
		listener.getLogger().println("Description: " + build.getDescription());
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

		return true;
	}

	public static final class Descriptor extends BuildStepDescriptor<Publisher> {

		private static final String V1_PATH = "v1Path";
		private static final String V1_LOGIN = "v1Login";
		private static final String V1_PASSWORD = "v1Password";

		private String v1Path;
		private String v1Login;
		private String v1Password;

		public Descriptor() {
			super(VersionOneNotifier.class);
			load();
		}


		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		public String getDisplayName() {
			return "VersionOne";
		}

		public String getHelpFile() {
			return "/plugin/hudson-notifier/help-projectSettings.html";
		}


		/**
		 * Performs on-the-fly validation of the form field 'name'.
		 *
		 * @param value This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the browser.
		 */
		public FormValidation doCheckPathToVersionOne(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0) {
				return FormValidation.error("VersionOne server URL must not be empty");
			}
			if (value.length() < 4) {
				return FormValidation.warning("Isn't the path too short?");
			}
			try {
				new URL(value);
			} catch (MalformedURLException e) {
				return FormValidation.warning("Invalid server URL format");
			}

			return FormValidation.ok();
		}

		public FormValidation doTestConnection(StaplerRequest req, StaplerResponse rsp,
											   @QueryParameter(V1_PATH) final String path,
											   @QueryParameter(V1_LOGIN) final String login,
											   @QueryParameter(V1_PASSWORD) final String password) throws IOException, ServletException {
			try {
				new V1Instance(path, login, password).validate();
				return FormValidation.ok("Connection is valid.");
			} catch (ApplicationUnavailableException e) {
				return FormValidation.error("Cannot connect to specified path.");
			} catch (AuthenticationException e) {
				return FormValidation.error("Wrong login or password.");
			}
		}

		public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
			v1Path = o.getString(V1_PATH);
			v1Login = o.getString(V1_LOGIN);
			v1Password = o.getString(V1_PASSWORD);
			save();
			return true;
		}

		public String getV1Path() {
			return v1Path;
		}

		public String getV1Login() {
			return v1Login;
		}

		public String getV1Password() {
			return v1Password;
		}

		/**
		 * Creates a new instance of {@link VersionOneNotifier} from a submitted form.
		 */
		public VersionOneNotifier newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			return new VersionOneNotifier();
		}
	}
}

