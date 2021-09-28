#!/usr/bin/env bash
#java $* -Dlogback.configurationFile=src/main/resources/logback.xml -jar target/scala-2.12/es-kafkatest-app-client.jar --process genericAvro
#java $* -Dlogback.configurationFile=logback.xml -jar target/scala-2.12/es-kafkatest-app-client.jar --process genericAvro
#java -Dlogback.configurationFile=logback.xmll -Dfile.ending=UTF8  -cp $SCALA_HOME/lib/scala-library.jar -Dconfig.file=application.conf  -jar target/scala-2.12/es-kafkatest-app-client.jar --process genericAvro
#java -Dlogback.configurationFile=logback.xml -Dfile.ending=UTF8  -cp $SCALA_HOME/lib/scala-library.jar  -jar target/scala-2.12/es-kafkatest-app-client.jar --process genericAvro
#java -Dlogback.configurationFile=src/main/resources/logback.xml -Dfile.ending=UTF8  -cp $SCALA_HOME/lib/scala-library.jar  -jar target/scala-2.12/es-kafkatest-app-client.jar --process genericAvro
#echo $SCALA_HOME
#java -Dlogback.configurationFile=logback.xml -Dfile.ending=UTF8  -cp $SCALA_HOME/lib/scala-library.jar  -jar target/scala-2.12/es-kafkatest-app-client.jar --process genericAvro
echo $MODE

LOGS_DIR="/var/logs"
LOGS_CONFIG="logback.dev.xml"
CONFIG="application.dev.conf"

if [ "$MODE" = "test" ];then
    LOGS_CONFIG="logback.test.xml"
    CONFIG="application.test.conf"
elif [ "$MODE" = "prod" ];then
    LOGS_CONFIG="logback.prod.xml"
    CONFIG="application.prod.conf"
fi

echo $LOGS_CONFIG
echo $LOGS_DIR
echo $CONFIG


#Исправляет отсутствие DNS host.docker.internal для доступа к хосту докера
#Можно будет убрать в новых версиях linux
#function fix_linux_internal_host() {
#  DOCKER_INTERNAL_HOST="host.docker.internal"
#
#  if ! grep $DOCKER_INTERNAL_HOST /etc/hosts > /dev/null ; then
#    DOCKER_INTERNAL_IP=`/sbin/ip route | awk '/default/ { print $3 }' | awk '!seen[$0]++'`
#    echo -e "$DOCKER_INTERNAL_IP\t$DOCKER_INTERNAL_HOST" | tee -a /etc/hosts > /dev/null
#    echo "Added $DOCKER_INTERNAL_HOST to hosts /etc/hosts"
#  fi
#}

#fix_linux_internal_host

    
#java -DPROCESS_NAME=$PROCESS -DLOGS_DIR=$LOGS_DIR -Dlogback.configurationFile=$LOGS_CONFIG -Dfile.ending=UTF8  -cp $SCALA_HOME/lib/scala-library.jar  -jar es-kafkatest-app-client.jar --process $PROCESS
java -Dconfig.file=$CONFIG -DMODE=$MODE -DLOGS_DIR=$LOGS_DIR -Dlogback.configurationFile=$LOGS_CONFIG -Dfile.ending=UTF8  -cp $SCALA_HOME/lib/scala-library.jar  -jar otus-sales-leads-generator-app-bot.jar