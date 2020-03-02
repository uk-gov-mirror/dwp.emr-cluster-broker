FROM openjdk:13-alpine
ENV APP_NAME=emr-cluster-broker
ENV APP_HOME=/opt/${APP_NAME}
ENV USER=cb_user
RUN mkdir ${APP_HOME} ${APP_HOME}/keystore
WORKDIR ${APP_HOME}
COPY build/libs/*.jar ./${APP_NAME}.jar
RUN addgroup -S ${USER} && adduser -S ${USER} && \
        chown -R ${USER}.${USER} . && \
        chmod +x ./${APP_NAME}.jar && ls -l && pwd

# Generate a self-signed certificate JKS
RUN keytool -genkey \
        -keystore ${APP_HOME}/emr-cluster-broker.keystore \
        -keyalg RSA -keysize 4096 -validity 3650 -alias emr-cluster-broker \
        -dname "cn=emr-cluster-broker, ou=emr-cluster-broker, o=emr-cluster-broker, c=emr-cluster-broker" \
        -storepass changeit -keypass changeit

RUN chown -R ${USER}.${USER} ${APP_HOME}/emr-cluster-broker.keystore

USER ${USER}
ENTRYPOINT ["sh", "-c", "java -jar ./${APP_NAME}.jar \"$@\"", "--"]
