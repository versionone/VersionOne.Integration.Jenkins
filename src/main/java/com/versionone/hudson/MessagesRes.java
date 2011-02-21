/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.hudson;


import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;

public class MessagesRes {

    private final static ResourceBundleHolder holder = ResourceBundleHolder.get(MessagesRes.class);

    public static String VersionOne_Notifier() {
        return holder.format("VersionOne.Notifier");
    }

    public static Localizable _VersionOne_Notifier() {
        return new Localizable(holder, "VersionOne.Notifier");
    }

    public static String connectionValid() {
        return holder.format("connectionValid");
    }

    public static String connectionFailedPath() {
        return holder.format("connectionFailedPath");
    }

    public static String connectionFailedUsername() {
        return holder.format("connectionFailedUsername");
    }

    public static String connectionFailedRefField(Object string) {
        return holder.format("connectionFailedRefField", string);
    }

    public static String connectionFailedProxyUrlMalformed() {
        return holder.format("connectionFailedProxyUrlMalformed");
    }

    public static String cannotBeEmpty() {
        return holder.format("cannotBeEmpty");
    }

    public static String pathWrong() {
        return holder.format("pathWrong");
    }

    public static String pattternWrong() {
        return holder.format("patternWrong");
    }

    public static String processSuccess() {
        return holder.format("processedSuccessfully");
    }

    public static String connectionIsNotCorrect() {
        return holder.format("connectionIsNotCorrect");
    }

    public static String buildRunAlreadyExist() {
        return holder.format("buildRunAlreadyExist");
    }

    public static String buildProjectNotFound() {
        return holder.format("buildProjectNotFound");
    }

    public static String workitemClosedCannotAttachData(String workitemId) {
        return holder.format("workitemClosedCannotAttachData", workitemId);
    }
}
