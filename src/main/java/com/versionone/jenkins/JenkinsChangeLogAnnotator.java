package com.versionone.jenkins;

import com.versionone.apiclient.exceptions.V1Exception;
import hudson.Extension;
import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

import java.net.MalformedURLException;
import java.util.regex.Pattern;

import com.versionone.integration.ciCommon.V1Worker;
import com.versionone.integration.ciCommon.WorkitemData;

/**
 *
 */
@Extension
public class JenkinsChangeLogAnnotator extends ChangeLogAnnotator {

    private V1Worker worker;
    private Pattern pattern;

    /**
     * Setter for data handlers
     * @param worker DataLayer for working with VersionOne
     * @param pattern pattern to find info about Workitems from the VersionOne
     */
    public void setData(V1Worker worker, Pattern pattern) {
        this.worker = worker;
        this.pattern = pattern;        
    }


    public void annotate(AbstractBuild<?,?> build, Entry change, MarkupText text) {
        if (worker == null || pattern == null ) {
            return;
        }

        for(SubText token : text.findTokens(pattern)) {
            WorkitemData workitemData = null;
            try {
                workitemData = worker.getWorkitemData(token.group(0));
            } catch (V1Exception e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if(workitemData.hasValue()) {
                String open = "window.open(\"" + workitemData.getUrl() + "\",\"V1Asset\", \"width=800,height=500,scrollbars=1,toolbar=0,directories=0,location=0\");return false;";
                token.surroundWith(
                    String.format("<a href='%s' tooltip='%s' onclick='%s' target='Asset'>", workitemData.getUrl(), workitemData.getName(), open),
                    "</a>");
            }

        }
    }

}
