## Configuration

### Configure VersionOne

If you are using Team Edition, you need to manually create the Build Project. Follow the [Team Edition instructions](team_edition.html) before proceeding.

1.  Log into the VersionOne application as admin.
2.  Navigate to the Admin -> Configuration -> System page.
3.  Check the `Enable Build Integration` checkbox and click the `Apply` button.
![Enable Build Integration](images/EnableBuildIntegration.jpg)
4.  Navigate to the Admin -> Projects -> Build Project page
![Build Projects Page](images/BuildProjects.jpg)
5.  Click Add to add a new Build Project. Specify the following
    * Name - this is how the Build Project will be known to VersionOne users
    * Reference - this is how the Build Project is known to Jenkins
6.  Click Ok to save the new Build Project.
7.  Navigate to the Admin -> Projects -> Projects page.
8.  Click Edit on the row for the project you want associated with a Build Project.
9.  Using the `Build Project` dropdown add the appropriate Build Project.
![Add Build Project](images/AssignBuildProjectToProject.jpg)
10. Click Ok to accept the changes.
11. Logout.

### Configure Jenkins

These instructions assume that you are logged into Jenkins as an administrator.

1.  On the Jenkins Dashboard, Click `Manage Jenkins`.
2.  Click `Manage Plugins`.
3.  Click `Advanced`.
4.  Under `Upload Plugin` browse to your download location and select the file versionone.hpi
![Jenkins Plugin Manager](images/HudsonPluginManager.png)
5.  Click `Upload`.
6.  Restart your Jenkins instance in order to load the new plugin.
7.  On the Jenkins Dashboard, click `Manage Jenkins`.
8.  Click `Configure System`. There is a new VersionOne section at the end of this page.
9.  Provide your VersionOne connection parameters.
![Configure System](images/ConfigureSystem.png)
If you connect to VersionOne through a proxy, check the `Use proxy server` checkbox	and provide additional Proxy parameters
![Configure Proxy](images/ConfigureSystem-Proxy.png)
It is recommended that you do not change the `Reference Field` or `Comment RegEx` fields. The `Reference Field` is the system name of the attribute to search when matching the ID in change comments with workitems in VersionOne. The `Comment RegEx` is used to extract workitem identifiers from the change comments.
10. Test the connection.
11. Save the settings.
12. Choose the Job you wish to have published to VersionOne. Remember that this job name must be configured in VersionOne.
13. Click `Configure` to configure the workspace.
14. In the `Post-build Actions` click the `VersionOne Notifier` checkbox.
![Post Build Actions](images/PostBuildActions.png)
15. Click `Save`.
