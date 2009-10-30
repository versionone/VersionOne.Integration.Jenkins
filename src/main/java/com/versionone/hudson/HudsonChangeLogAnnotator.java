/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.hudson;

import hudson.MarkupText;
import hudson.Extension;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

import java.util.regex.Pattern;

import com.versionone.integration.ciCommon.V1Worker;
import com.versionone.integration.ciCommon.WorkitemData;

/**
 *
 */
@Extension
public class HudsonChangeLogAnnotator extends ChangeLogAnnotator {

    private V1Worker worker;
    private Pattern pattern;

    /**
     * Setter for processing data
     * @param worker DataLayer for working with VersionOne
     * @param pattern patter to find info about Workitems from the VersionOne
     */
    public void setData(V1Worker worker, Pattern pattern) {
        this.worker = worker;
        this.pattern = pattern;        
    }


    public void annotate(AbstractBuild<?,?> build, Entry change, MarkupText text) {

        for(SubText token : text.findTokens(pattern)) {
            WorkitemData workitemData = worker.getWorkitemData(token.group(0));
            
            if(workitemData.hasValue()) {
                String open = "window.open(\"" + workitemData.getUrl() + "\",\"V1Asset\", \"width=800,height=500,scrollbars=1,toolbar=0,directories=0,location=0\");return false;";
                token.surroundWith(
                    String.format("<a href='%s' tooltip='%s' onclick='%s' target='Asset'>", workitemData.getUrl(), workitemData.getName(), open),
                    "</a>");
            }

        }
    }

}
