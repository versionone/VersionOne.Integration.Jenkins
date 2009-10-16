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
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	@Override
	public DescriptorImpl getDescriptor() {
		return DESCRIPTOR;
	}

	// This is where you 'build' the project
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		final V1Instance instance = new V1Instance(getDescriptor().getPathToVersionOne(), "admin", "admin");
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

		listener.getLogger().println(getDescriptor().getPathToVersionOne());
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

	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		private String pathToVersionOne;

		public DescriptorImpl() {
			super(VersionOneNotifier.class);
			load();
		}


		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		public String getDisplayName() {
			return "VersionOne integration";
		}

		public String getHelpFile() {
			return "/plugin/HudsonIntegration/help-globalConfig.html";
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
				FormValidation.warning("invalidUrl", "Invalid server URL format");
			}

			return FormValidation.ok();
		}

		public FormValidation doTestConnection(StaplerRequest req, StaplerResponse rsp,
											   @QueryParameter("pathToVersionOne") final String path) throws IOException, ServletException {
			final V1Instance v1 = new V1Instance(path, "admin", "admin");

			try {
				v1.validate();
				return FormValidation.ok("Connection is valid.");
			} catch (Exception ex) {
				return FormValidation.error("Connection is not valid.");
			}
		}

		public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
			pathToVersionOne = o.getString("pathToVersionOne");
			save();
			return super.configure(req, o);
		}

		public String getPathToVersionOne() {
			return pathToVersionOne;
		}

		/**
		 * Creates a new instance of {@link VersionOneNotifier} from a submitted form.
		 */
		public VersionOneNotifier newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			return new VersionOneNotifier();
		}
	}
}

