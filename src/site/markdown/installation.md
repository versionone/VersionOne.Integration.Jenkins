## Installation

These instructions assume that [Jenkins](http://jenkins-ci.org/) is already installed, configured, and working properly.

### 1. Ensure Connectivity

Verify that you can connect to your VersionOne instance from the machine hosting Jenkins.

### 2. Extract Files

Download VersionOne Integration for Jenkins and extract it into a folder of your choice. This can be a temporary location since we will copy some of these files during configuration.

### 3. Configure

Instructions for configuring VersionOne Integration for Jenkins are located in the [Configuration](configuration.html) section.

### 4. Verify the installation

Once configuration is complete use the following steps to verify that the build integration is working

* Navigate to your Jenkins instance.
* Force a build on the project you configured.
* Wait for build to complete.
* Navigate to your VersionOne instance.
* Login.
* Select VersionOne project in My Projects dropdown.
* Navigate to the Reports -> Reports Overview page.
* Select the Build Run Quicklist Report.

Instructions for configuring VersionOne Integration for Git are located in the [Configuration](configuration.html) section. The default configuration provided with the integration is a working sample. Provided the integration server can reach the VersionOne SaaS environment and GitHub, you can proceed to test the integration prior to configuration for the local environment.
