## Introduction

The VersionOne Integration for Jenkins creates a record of builds in VersionOne, so development teams can associate stories	and defects to a particular build. This visibility is useful when identifying problem builds or generating release notes. Once the VersionOne Plugin has been installed, team members include a VersionOne identifier, such as `S-01454` or `TK-01234`, in the comments of their SCM commit. Every time	a build executes the publisher creates a BuildRun asset in VersionOne with details of the build. The VersionOne BuildRun is visible in the Relationships for the associated Story/Defect Details page.

Using this integration you can better answer the following questions:

### Defects

* Which build the defect was reported against?
* Which build contained the fix for the defect?
* Which builds contain work for the defect?

### For Stories (Backlog Item)

* Which builds contain work for the story?
* Which build contained the completed story?

### For Build Runs

* Which defects were fixed?
* Which stories were completed?
* Which defects were introduced?
* When work for a story or defect was included?
* Which Change-sets were included?

### For a range of Build Runs

* Which stories were completed?
* Which defects were fixed?
* Which defects were introduced?

The following sequence diagram illustrates how the VersionOne Integration for Jenkins interacts with Jenkins and VersionOne.

![Jenkins Integration Sequence Diagram](images/Jenkins_Integration_Sequence.png)
