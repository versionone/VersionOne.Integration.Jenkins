set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n -Xms64m -Xmx128m
mvn hpi:run -Djetty.port=8090