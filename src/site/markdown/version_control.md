## Adding Support for a new Version Control System

### 1. Add plugin reference to pom.xml. 

Make sure that this dependency could be successfully resolved.

### 2. Add a class wrapping native changeset type.

SvnModification	or PerforceModification are good examples on how to do it. New class must inherit VcsModification interface and provide parameterless public constructor.

### 3. Modify VcsModificationWrapperFactory class to support new changeset type.

It is required to add line similar to `classNameMappings.put("hudson.plugins.perforce.PerforceChangeLogEntry", "com.versionone.hudson.PerforceModification")`. String literals are mappings of native changeset log entry classes to our custom wrappers in format supported by Java Reflection, so that instances and class objects could be successfully created. Changesets will be processed as soon as user installs the corresponding plugin and restarts Jenkins server. In fact, our plugin won't start without its dependencies.
