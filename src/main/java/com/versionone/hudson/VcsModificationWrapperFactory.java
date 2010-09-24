package com.versionone.hudson;

import com.versionone.integration.ciCommon.VcsModification;
import hudson.scm.ChangeLogSet;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class VcsModificationWrapperFactory {
    private static VcsModificationWrapperFactory instance;

    private final Map<String, String> classNameMappings = new HashMap<String, String>();
    private final Map<Class, Class> mappings = new HashMap<Class, Class>();

    private VcsModificationWrapperFactory() {
        classNameMappings.put("hudson.scm.SubversionChangeLogSet$LogEntry",
                              "com.versionone.hudson.SvnModification");
        classNameMappings.put("hudson.plugins.perforce.PerforceChangeLogEntry",
                              "com.versionone.hudson.PerforceModification");

        fillSupportedMappings();
    }

    private void fillSupportedMappings() {
        for(Map.Entry<String, String> entry : classNameMappings.entrySet()) {
            try {
                Class logEntryClass = Class.forName(entry.getKey());
                Class wrapperClass = Class.forName(entry.getValue());
                mappings.put(logEntryClass, wrapperClass);
            } catch(ClassNotFoundException e) {
                // do nothing, it is unsupported VCS changeset type
            }
        }
    }

    public static VcsModificationWrapperFactory getInstance() {
        if(instance == null) {
            instance = new VcsModificationWrapperFactory();
        }

        return instance;
    }

    public boolean isSupported(ChangeLogSet.Entry modification) {
        return modification != null && mappings.containsKey(modification.getClass());
    }

    public VcsModification createWrapper(ChangeLogSet.Entry modification) {
        Class modificationType = modification.getClass();

        if(mappings.containsKey(modificationType)) {
            Class wrapperType = mappings.get(modificationType);

            try {
                Constructor constructor = wrapperType.getConstructor(modificationType);
                return (VcsModification) constructor.newInstance(modification);
            } catch(Exception ex) {
                throw new UnsupportedOperationException("Provided changeset type is not supported: " + modificationType);
            }
        }

        throw new UnsupportedOperationException("Provided changeset type is not supported: " + modificationType);
    }
}
