package com.versionone.hudson;


import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;

@SuppressWarnings({
    "",
    "PMD"
})
public class MessagesRes {

    private final static ResourceBundleHolder holder = new ResourceBundleHolder(MessagesRes.class);

    /**
     * Associated JIRA
     *
     */
    public static String VersionOne_Notifier() {
        return holder.format("VersionOne.Notifier");
    }

    /**
     * Associated JIRA
     *
     */
    public static Localizable _VersionOne_Notifier() {
        return new Localizable(holder, "VersionOne.Notifier");
    } 

}
