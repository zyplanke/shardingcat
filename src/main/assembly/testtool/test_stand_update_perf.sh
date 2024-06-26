#!/bin/bash

echo "check JAVA_HOME & java"
JAVA_CMD=$JAVA_HOME/bin/java
MAIN_CLASS=io.shardingcat.performance.TestUpdatePerf
if [ ! -d "$JAVA_HOME" ]; then
    echo ---------------------------------------------------
    echo WARN: JAVA_HOME environment variable is not set. 
    echo ---------------------------------------------------
    JAVA_CMD=java
fi

echo "---------set HOME_DIR------------"
CURR_DIR=`pwd`
cd ..
SHARDINGCAT_HOME=`pwd`
cd $CURR_DIR
$JAVA_CMD -Xms256M -Xmx1G -XX:MaxPermSize=64M  -DSHARDINGCAT_HOME=$SHARDINGCAT_HOME -cp "$SHARDINGCAT_HOME/conf:$SHARDINGCAT_HOME/lib/*" $MAIN_CLASS $1 $2 $3 $4 $5 $6 $7 $8 $9
