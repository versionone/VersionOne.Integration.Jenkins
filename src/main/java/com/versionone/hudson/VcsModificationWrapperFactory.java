package com.versionone.hudson;

import com.versionone.integration.ciCommon.VcsModification;
import hudson.plugins.perforce.PerforceChangeLogEntry;
import hudson.scm.ChangeLogSet;
import hudson.scm.SubversionChangeLogSet;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class VcsModificationWrapperFactory {
    private static VcsModificationWrapperFactory instance;

    private Map<Class, Class> mappings = new HashMap();

    private VcsModificationWrapperFactory() {
        mappings.put(SubversionChangeLogSet.LogEntry.class, SvnModification.class);
        mappings.put(PerforceChangeLogEntry.class, PerforceModification.class);
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
