set -e
set -a
source ../env/common.local.env
source ../env/notification-service.local.env
set +a

cd ../services/notification-service
mvn spring-boot:run