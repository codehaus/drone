@echo off
rem Copyright 2003 Geert Bevin <gbevin@uwyn.com>
rem Distributed under the terms of the GNU General Public License, v2 or later
rem $Id$

set PROGDIR=%~dp0\..\
set JETTY=%PROGDIR%\jetty-4.2.17
set JETTY_CONSOLE=%JETTY%\logs\out.log
set JETTY_HOME=%JETTY%

cd "%JETTY_HOME%"

java -classpath "%PROGDIR%/config:%JETTY_HOME%/lib/javax.servlet.jar:%JETTY_HOME%/lib/org.mortbay.jetty.jar:%PROGDIR%/lib/mysql-connector-java-3.0.11-stable-bin.jar:%PROGDIR%/lib/postgres-jdbc-7.4.jar:%PROGDIR%/lib/rife-0.7.2.jar:%PROGDIR%/build/dist/drone-1.0.jar" -Djetty.home="$JETTY_HOME" -Drife.webapp.path=../src:../build/dist:../lib org.mortbay.jetty.Server "$JETTY_HOME/etc/jetty.xml"
