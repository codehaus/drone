#!/bin/sh
# Copyright 2003 Geert Bevin <gbevin@uwyn.com>
# Distributed under the terms of the GNU General Public License, v2 or later
# $Id$

PROGDIR="`dirname $0`"
case "${PROGDIR}" in
	[^/]*)
		export PROGDIR="`pwd`/${PROGDIR}"
		;;
esac
export PROGDIR="$PROGDIR/.."

java -classpath "$PROGDIR/config:$PROGDIR/lib/mysql-connector-java-3.0.11-stable-bin.jar:$PROGDIR/lib/postgres-jdbc-7.4.jar:$PROGDIR/lib/rife-0.7.2.jar:$PROGDIR/build/dist/drone-1.0.jar" com.uwyn.drone.Droned
