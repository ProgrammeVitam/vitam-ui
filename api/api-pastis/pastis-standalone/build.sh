cd ../../../ui/
mvn clean install -DskipTests --projects ui-frontend,ui-frontend-common -Pstandalone
cd ../commons/
mvn clean install -DskipTests
cd ../api/
mvn clean install -DskipTests -Pstandalone
