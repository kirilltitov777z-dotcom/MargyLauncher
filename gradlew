#!/bin/sh

APP_HOME=`cd "$(dirname "$0")" && pwd`

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

if [ ! -x "$JAVACMD" ] ; then
    echo "Error: JAVA_HOME is not set correctly." >&2
    exit 1
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
