FROM openjdk:8-alpine
ENV APP_NAME=emr-cluster-broker
ENV APP_HOME=/opt/${APP_NAME}
ENV USER=cb_user
RUN mkdir ${APP_HOME}
WORKDIR ${APP_HOME}
COPY build/libs/*.jar ./${APP_NAME}.jar
RUN addgroup -S ${USER} && adduser -S ${USER} && \
        chown -R ${USER}.${USER} . && \
        chmod +x ./${APP_NAME}.jar && ls -l && pwd

USER ${USER}
ENTRYPOINT ["sh", "-c", "java -DclusterBroker.awsRegion=${AWS_REGION} -DclusterBroker.amiSearchPattern=${AMI_SEARCH_PATTERN} -DclusterBroker.amiOwnerIds=${AMI_OWNER_IDS} -DclusterBroker.emrReleaseLabel=${EMR_RELEASE_LABEL} -DclusterBroker.s3LogUri=${S3_LOG_URI} -DclusterBroker.securityConfiguration=${SECURITY_CONFIGURATION} -DclusterBroker.jobFlowRoleBlacklist=${JOB_FLOW_ROLE_BLACKLIST} -jar ./${APP_NAME}.jar \"$@\"", "--"]
