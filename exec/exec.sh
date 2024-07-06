#!/bin/sh

set -xe

BASE_HOME=${APP_HOME:-${HOME}}
LOGS_HOME=${BASE_HOME}/logs

LOGGING_PROP=--logging.file.name=${LOGS_HOME}/dough
PROFILE_PROP=--spring.profiles.active=logger,server
PID_FILE_PATH_PROP=--spring.pid.file=${BASE_HOME}/dough.pid
JAVA_AGENT_PROP=-javaagent:${BASE_HOME}/jmx_prometheus_javaagent-0.16.1.jar=9210:${BASE_HOME}/exporter-config-embedded-tomcat.yml

JAVA_OPTS="${JAVA_OPTS} -ea -server"
JAVA_OPTS="${JAVA_OPTS} -Xms1024m -Xmx1024m"
JAVA_OPTS="${JAVA_OPTS} -verbose:gc -XX:+UseG1GC"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintHeapAtGC -Xloggc:${LOGS_HOME}/gc/gc.log"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOGS_HOME}/dump"
JAVA_OPTS="${JAVA_OPTS} -Dfile.encoding=UTF-8 -Dsun.net.inetaddr.ttl=0"

nohup java ${JAVA_AGENT_PROP} -jar ${JAVA_OPTS} ${BASE_HOME}/${JAR_NAME:-loona}.jar ${PROFILE_PROP} ${LOGGING_PROP} ${PID_FILE_PATH_PROP} > /dev/null &
