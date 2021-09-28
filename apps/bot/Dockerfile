FROM openjdk:8-jdk-alpine as build-env

# https://gist.github.com/gyndav/c8d65b59793566ee73ed2aa25aa10497
# устанавливаем scala и sbt

ENV SCALA_VERSION=2.13.3 \
    SCALA_VERSION_SHORT=2.13 \
    SCALA_HOME=/usr/share/scala

# NOTE: bash is used by scala/scalac scripts, and it cannot be easily replaced with ash.
RUN apk add --no-cache --virtual=.build-dependencies wget ca-certificates && \
    apk add --no-cache bash curl jq && \
    cd "/tmp" && \
    wget --no-verbose "https://downloads.typesafe.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz" && \
    tar xzf "scala-${SCALA_VERSION}.tgz" && \
    mkdir "${SCALA_HOME}" && \
    rm "/tmp/scala-${SCALA_VERSION}/bin/"*.bat && \
    mv "/tmp/scala-${SCALA_VERSION}/bin" "/tmp/scala-${SCALA_VERSION}/lib" "${SCALA_HOME}" && \
    ln -s "${SCALA_HOME}/bin/"* "/usr/bin/" && \
    apk del .build-dependencies && \
    rm -rf "/tmp/"*

#https://github.com/circleci/circleci-images/pull/552/commits/756068e91eb135b0f929f1c420d9d151b351a2f8
RUN export PATH="/usr/local/sbt/bin:$PATH" &&  apk update && apk add ca-certificates wget tar && mkdir -p "/usr/local/sbt" && wget -qO - --no-check-certificate "https://github.com/sbt/sbt/releases/download/v1.5.4/sbt-1.5.4.tgz" | tar xz -C /usr/local/sbt --strip-components=1 && sbt -Dsbt.rootdir=true sbtVersion

ENV PATH="/usr/local/sbt/bin:$PATH"

# подготовка к сборке
RUN mkdir -p /opt/src
RUN mkdir -p /opt/app

WORKDIR /opt/src

# копируем исходники проекта
COPY . /opt/src

# собираем проект
RUN ["sbt", ";project telegramBot ;compile ;assembly"]

#RUN apk add zip

# https://stackoverflow.com/questions/999489/invalid-signature-file-when-attempting-to-run-a-jar
#RUN zip -d apps/api/target/scala-${SCALA_VERSION_SHORT}/otus-sales-leads-generator-app-api.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'


WORKDIR /opt/src/apps/bot

#RUN ls
#RUN ls target/scala-${SCALA_VERSION_SHORT}

# копируем всё необходимое для работы в /opt/app/
RUN cp ./run_jar.sh ./target/scala-${SCALA_VERSION_SHORT}/otus-sales-leads-generator-app-bot.jar \
    ./src/main/resources/application.conf ./src/main/resources/application.dev.conf ./src/main/resources/application.test.conf ./src/main/resources/application.prod.conf \
    ./src/main/resources/logback.dev.xml ./src/main/resources/logback.test.xml ./src/main/resources/logback.prod.xml \
    /opt/app/


# конечный образ
FROM openjdk:8-jre-alpine

ENV MODE dev

#Директория с приложением
RUN mkdir -p /opt/app
#Логи
RUN mkdir -p /var/logs
#RockDB
RUN mkdir -p /tmp

#Основная папка
WORKDIR /opt/app

# копируем содержимое /opt/app из промежуточного образа для сборки в /opt/app текущего образа
COPY --from=build-env /opt/app .

#RUN mv /opt/app/certConfig.json /var/logs

RUN chmod +x /opt/app/run_jar.sh
RUN cp /opt/app/run_jar.sh /run_jar.sh

ENTRYPOINT ["./run_jar.sh"]
