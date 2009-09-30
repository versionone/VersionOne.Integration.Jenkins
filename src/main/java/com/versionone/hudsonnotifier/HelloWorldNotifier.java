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
import com.versionone.om.V1Instance;


import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;


@Extension
public class HelloWorldNotifier extends Notifier {
    private String pathToVersionOne;

    @DataBoundConstructor
    public HelloWorldNotifier(String pathToVersionOne) {
        this.pathToVersionOne = pathToVersionOne;
    }

    public String getPathToVersionOne() {
        return pathToVersionOne;
    }

    public HelloWorldNotifier() {
        int i = 0;
    }

    @Override
    public BuildStepDescriptor getDescriptor() {
        return (BuildStepDescriptor)super.getDescriptor();
    }

    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        // this is where you 'build' the project
        // since this is a dummy, we just say 'hello world' and call that a build
        //V1APIConnector aaa = new V1APIConnector("http://jsdksrv01:8080","admin","admin");
        V1Instance aaa = new V1Instance(pathToVersionOne,"admin","admin");
        try {
            aaa.validate();
            //System.out.println("Version One connection is ok");
            listener.getLogger().println("Version One connection is ok");
        } catch (Exception ex) {
            //System.out.println("Version One connection is NOT ok:\n"+ex.getMessage());
            listener.getLogger().println("Version One connection is NOT ok:\n"+ex.getMessage());
        }
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

        // we can recognize who init this build user or triger by data in actions(build.getActions()):
        // hudson.model.Cause$UserCause - user
        // hudson.triggers.SCMTrigger$SCMTriggerCause - trigger by Subversion update
        // build.getActions().get(0); or verify all data in loop
        
        return true;
    }

    @Extension // this marker indicates Hudson that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getDisplayName() {
            return "VersionOne integration settings";
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
            if(value.length()==0)
                return FormValidation.error("Please set a path");
            if(value.length()<4)
                return FormValidation.warning("Isn't the path too short?");
            return FormValidation.ok();
        }

        public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
            // to persist global configuration information,
            // set that to properties and call save().
            save();
            return super.configure(req,o);
        }
    }

}

