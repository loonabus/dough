#!/bin/sh

set -xe

kill -15 $(cat ${TEST_HOME}/loona.pid)
