#!/bin/sh

set -xe

kill -15 $(cat ${APP_HOME}/loona.pid)
