SET V1_SDK_VERSION=8.0-SNAPSHOT
SET MAVEN=.\apache-maven-2.2.1\bin\mvn.bat
call %MAVEN% install:install-file -DgroupId=com.versionone -DartifactId=apiclient -Dversion=%V1_SDK_VERSION% -Dpackaging=jar -Dfile=VersionOne.APIClient.jar
call %MAVEN% install:install-file -DgroupId=com.versionone -DartifactId=objectmodel -Dversion=%V1_SDK_VERSION% -Dpackaging=jar -Dfile=VersionOne.ObjectModel.jar
call %MAVEN% install:install-file -DgroupId=com.versionone -DartifactId=apiclient -Dversion=%V1_SDK_VERSION% -Dpackaging=jar -Dclassifier=sources -Dfile=VersionOne.JavaSDK.Source-8.0.zip
call %MAVEN% install:install-file -DgroupId=com.versionone -DartifactId=objectmodel -Dversion=%V1_SDK_VERSION% -Dpackaging=jar -Dclassifier=sources -Dfile=VersionOne.JavaSDK.Source-8.0.zip
pause