package com.versionone.hudsonnotifier;
import hudson.Launcher;
import hudson.Extension;
import hudson.scm.ChangeLogSet;
import hudson.util.FormValidation;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Describable;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerResponse;
import com.versionone.apiclient.*;
import com.versionone.om.V1Instance;


import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.net.URL;
import java.net.MalformedURLException;


public class HelloWorldNotifier extends Notifier {

    /*
    @DataBoundConstructor
    public HelloWorldNotifier(Boolean enable) {
        //this.pathToVersionOne = pathToVersionOne;
        this.enable = enable;
    }
    */

    //public String getPathToVersionOne() {
        //return pathToVersionOne;
    //}
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        // this is where you 'build' the project
        // since this is a dummy, we just say 'hello world' and call that a build
        //V1APIConnector aaa = new V1APIConnector("http://jsdksrv01:8080","admin","admin");
        V1Instance aaa = new V1Instance(getDescriptor().getPathToVersionOne(),"admin","admin");
        try {
            aaa.validate();
            //System.out.println("Version One connection is ok");
            listener.getLogger().println("Version One connection is ok");
        } catch (Exception ex) {
            //System.out.println("Version One connection is NOT ok:\n"+ex.getMessage());
            listener.getLogger().println("Version One connection is NOT ok:\n"+ex.getMessage());
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

    //@Extension // this marker indicates Hudson that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String pathToVersionOne;

        /*
        @DataBoundConstructor
        public DescriptorImpl(String pathToVersionOne) {
            this.pathToVersionOne = pathToVersionOne;
        }
        */
        public DescriptorImpl() {
            super(HelloWorldNotifier.class);
            load();
        }

        
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;  //To change body of implemented methods use File | Settings | File Templates.
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
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckPathToVersionOne(@QueryParameter String value) throws IOException, ServletException {
            if(value.length()==0) {
                return FormValidation.error("VersionOne server URL must not be empty");
            }
            if(value.length()<4) {
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
            final V1Instance aaa = new V1Instance(path, "admin", "admin");

            try {
                aaa.validate();
                return FormValidation.ok("Connection is valid.");
            } catch (Exception ex) {
                return FormValidation.error("Connection is not valid.");

            }

        }

        public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
            // to persist global configuration information,
            // set that to properties and call save().
            pathToVersionOne = o.getString("pathToVersionOne");
            save();
            return super.configure(req,o);
        }

        public String getPathToVersionOne() {
            return pathToVersionOne;
        }

        /**
         * Creates a new instance of {@link HelloWorldNotifier} from a submitted form.
         */
        public HelloWorldNotifier newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new HelloWorldNotifier();
        }

    }




}

