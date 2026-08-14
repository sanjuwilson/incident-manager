set -e
set -a
source ../env/local/common.local.env
source ../env/local/incident-service.local.env
set +a
cd ../services/incident-service
mvn clean install
mvn spring-boot:run