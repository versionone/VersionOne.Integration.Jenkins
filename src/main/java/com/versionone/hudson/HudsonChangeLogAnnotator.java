package com.versionone.hudson;

import hudson.MarkupText;
import hudson.Extension;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import com.versionone.integration.ciCommon.V1Config;
import com.versionone.integration.ciCommon.V1Worker;
import com.versionone.integration.ciCommon.WorkitemData;
import com.versionone.apiclient.Query;
import com.versionone.om.Workitem;

/**
 *
 */
public class HudsonChangeLogAnnotator extends ChangeLogAnnotator {

    private final V1Worker worker;
    private final Pattern pattern;

    public HudsonChangeLogAnnotator(V1Worker worker, Pattern pattern) {
        this.worker = worker;
        this.pattern = pattern;
    }


    public void annotate(AbstractBuild<?,?> build, Entry change, MarkupText text) {

        for(SubText token : text.findTokens(pattern)) {
            WorkitemData workitemData = worker.getWorkitemData(token.group(0));
            
            if(workitemData!=null) {
                String open = "window.open(\"" + workitemData.getUrl() + "\",\"V1Asset\", \"width=800,height=500,scrollbars=1,toolbar=0,directories=0,location=0\");return false;";
                token.surroundWith(
                    String.format("<a href='%s' tooltip='%s' onclick='%s' target='Asset'>", workitemData.getUrl(), workitemData.getName(), open),
                    "</a>");
            }

        }
    }

}
