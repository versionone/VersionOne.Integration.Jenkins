## Team Edition Prerequisites

You need to manually create the Build Project asset. Here are some examples for creating a Build Project:

### Example 1: Using Windows PowerShell Script

```
# Modify these parameters for your instance 
$url = "http://localhost/VersionOneTeam/" 
$username = "user" 
$password = "user" 
$buildProjectName = "V1 Build Project Name" 
$buildProjectReference = "CCNet.Build.Project.Name" 

# Do Not modify below this line 
$apiClient = resolve-path "versionone.sdk.apiclient.dll" 
$objectModel = resolve-path "versionone.sdk.objectmodel.dll" 
[Reflection.Assembly]::LoadFrom($apiClient) 
[Reflection.Assembly]::LoadFrom($objectModel) 
$v1 = new-object VersionOne.SDK.ObjectModel.V1Instance( $url, $username, $password) 
$v1.Validate() 
$v1.Create.BuildProject($buildProjectName, $buildProjectReference)
```

### Example 2: Using the VersionOne .NET SDK

```
// Modify these parameters for your instance 
string url = "http://localhost/VersionOneTeam/"; 
string user = "user"; 
string password = "user"; 
string buildProjectName = "V1 Build Project Name"; 
string buildProjectReference = "CCNet.Build.Project.Name"; 

// do not modify below this line 
V1Instance server = new V1Instance(url, user, password); 
server.Validate(); 
server.Create.BuildProject(buildProjectName, buildProjectReference);
```

### Example 3: Using the VersionOne Java APIClient

```
// Modify these parameters for your instance 
String url = "http://localhost/VersionOneTeam/"; 
String user = "user"; 
String password = "user"; 
String buildProjectName = "V1 Build Project Name"; 
String buildProjectReference = "CCNet.Build.Project.Name"; 

// Do Not Modify below this line 
V1APIConnector dataConnector = new V1APIConnector(url + "rest-1.v1/", user, password); 
V1APIConnector metaConnector = new V1APIConnector(url + "meta.v1/"); 
_metaModel = new MetaModel(metaConnector); 
_services = new Services(_metaModel, dataConnector); 
IAssetType buildProjectAssetType = _metaModel.getAssetType("BuildProject"); 
IAttributeDefinition suiteAssetName = buildProjectAssetType.getAttributeDefinition("Name"); 
IAttributeDefinition suiteAssetReference = buildProjectAssetType.getAttributeDefinition("Reference"); 
Asset buildProjectAsset = _services.createNew(buildProjectAssetType, null); 
buildProjectAsset.setAttributeValue(suiteAssetName, buildProjectName); 
buildProjectAsset.setAttributeValue(suiteAssetReference, buildProjectReference); 
_services.save(buildProjectAsset);
```

### Example 4: Using the VersionOne Core API 

To use the API directly you need to POST the following XML to http://{server}/{application}/rest-1.v1/Data/BuildProject 

```
<Asset> 
  <Attribute name="Name" act="set"> V1 Build Project Name </Attribute> 
  <Attribute name="Reference" act="set"> CCNet.Build.Project.Name </Attribute> 
</Asset>
```
