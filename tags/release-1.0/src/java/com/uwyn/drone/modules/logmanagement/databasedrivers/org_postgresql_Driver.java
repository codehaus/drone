/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.logmanagement.databasedrivers;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.modules.exceptions.LogManagerException;
import com.uwyn.drone.modules.logmanagement.DatabaseLogs;
import com.uwyn.drone.modules.logmanagement.LogResultProcessor;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.rife.database.Datasource;
import com.uwyn.rife.database.queries.CreateTable;
import com.uwyn.rife.database.queries.DropTable;
import com.uwyn.rife.database.queries.Insert;
import com.uwyn.rife.database.queries.Select;
import java.sql.Timestamp;
import java.util.Calendar;

public class org_postgresql_Driver extends DatabaseLogs
{
	private static CreateTable		sCreateTableLog = null;
	private static String			sCreateGetLogMessagesIndex = null;
	private static Insert			sAddLog = null;
	private static Select			sSearchLog = null;
	private static Select			sGetLogMessages = null;
	private static DropTable		sDropTableLog = null;
	private static String			sDropGetLogMessagesIndex = null;
	
	public org_postgresql_Driver(Datasource datasource)
	{
		super(datasource);
		initializeQueries();
	}

	protected void initializeQueries()
	{
		if (null == sCreateTableLog)
		{
			sCreateTableLog = new CreateTable(getDatasource());
			sCreateTableLog
				.table("Log")
				.column("moment", Timestamp.class, CreateTable.NOTNULL)
				.column("botname", String.class, 30, CreateTable.NOTNULL)
				.column("channel", String.class, 30, CreateTable.NOTNULL)
				.column("servername", String.class, 60, CreateTable.NOTNULL)
				.column("nickname", String.class, 30, CreateTable.NOTNULL)
				.column("username", String.class, 30, CreateTable.NOTNULL)
				.column("hostname", String.class, 255, CreateTable.NOTNULL)
				.column("message", String.class, CreateTable.NOTNULL)
				.column("raw", String.class, CreateTable.NOTNULL);
		}

		if (null == sCreateGetLogMessagesIndex)
		{
			sCreateGetLogMessagesIndex = "CREATE INDEX log_getlog_idx ON "+sCreateTableLog.getTable()+" (botname, channel, servername, moment)";
		}

		if (null == sAddLog)
		{
			sAddLog = new Insert(getDatasource());
			sAddLog
				.into(sCreateTableLog.getTable())
				.fieldCustom("moment", "now()")
				.fieldParameter("botname")
				.fieldParameter("channel")
				.fieldParameter("servername")
				.fieldParameter("nickname")
				.fieldParameter("username")
				.fieldParameter("hostname")
				.fieldParameter("message")
				.fieldParameter("raw");
		}

		if (null == sSearchLog)
		{
			sSearchLog = new Select(getDatasource());
			sSearchLog
				.from(sCreateTableLog.getTable())
				.field("moment")
				.field("raw")
				.whereParameter("botname", "=")
				.whereParameterAnd("channel", "=")
				.whereParameterAnd("servername", "=")
				.orderBy("moment", Select.DESC);
		}

		if (null == sGetLogMessages)
		{
			sGetLogMessages = new Select(getDatasource());
			sGetLogMessages
				.from(sCreateTableLog.getTable())
				.field("moment")
				.field("raw")
				.whereParameter("botname", "=")
				.whereParameterAnd("channel", "=")
				.whereParameterAnd("servername", "=")
				.whereParameterAnd("moment", "begin", ">=")
				.whereParameterAnd("moment", "end", "<")
				.orderBy("moment", Select.ASC);
		}

		if (null == sDropTableLog)
		{
			sDropTableLog = new DropTable(getDatasource());
			sDropTableLog
				.table(sCreateTableLog.getTable());
		}

		if (null == sDropGetLogMessagesIndex)
		{
			sDropGetLogMessagesIndex = "DROP INDEX log_getlog_idx";
		}
	}
	
	public boolean install()
	throws LogManagerException
	{
		return _install(sCreateTableLog, sCreateGetLogMessagesIndex);
	}

	public void addLog(Bot bot, Channel channel, ServerMessage serverMessage)
	throws LogManagerException
	{
		_addLog(sAddLog, bot, channel, serverMessage);
	}

	public boolean searchLog(LogResultProcessor processor, Bot bot, Channel channel, String search)
	throws LogManagerException
	{
		return _searchLog(sSearchLog, processor, bot, channel, search);
	}

	public boolean getLogMessages(LogResultProcessor processor, Bot bot, Channel channel, Calendar day)
	throws LogManagerException
	{
		return _getLogMessages(sGetLogMessages, processor, bot, channel, day);
	}
	
	public boolean remove()
	throws LogManagerException
	{
		return _remove(sDropTableLog, sDropGetLogMessagesIndex);
	}
}
