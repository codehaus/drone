/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.seenmanagement.databasedrivers;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.modules.exceptions.SeenManagerException;
import com.uwyn.drone.modules.seenmanagement.DatabaseSeen;
import com.uwyn.drone.modules.seenmanagement.SeenData;
import com.uwyn.rife.database.Datasource;
import com.uwyn.rife.database.queries.CreateTable;
import com.uwyn.rife.database.queries.DropTable;
import com.uwyn.rife.database.queries.Insert;
import com.uwyn.rife.database.queries.Select;
import com.uwyn.rife.database.queries.Update;

public class org_postgresql_Driver extends DatabaseSeen
{
	private static CreateTable		sCreateTableSeen = null;
	private static Select			sGetSeen = null;
	private static Insert			sAddSeen = null;
	private static Update			sUpdateSeen = null;
	private static Select			sSearchSeenMessage = null;
	private static DropTable		sDropTableSeen = null;
	
	public org_postgresql_Driver(Datasource datasource)
	{
		super(datasource);
		initializeQueries();
	}

	protected void initializeQueries()
	{
		if (null == sCreateTableSeen)
		{
			sCreateTableSeen = new CreateTable(getDatasource());
			sCreateTableSeen
				.table("Seen")
				.column("botname", String.class, 30, CreateTable.NOTNULL)
				.column("channel", String.class, 30, CreateTable.NOTNULL)
				.column("servername", String.class, 60, CreateTable.NOTNULL)
				.columns(SeenData.class)
				.primaryKey("SEEN_PK", new String[] {"botname", "channel", "servername", "nickname"});
		}

		if (null == sGetSeen)
		{
			sGetSeen = new Select(getDatasource());
			sGetSeen
				.from("Seen")
				.whereParameter("botname", "=")
				.whereParameterAnd("channel", "=")
				.whereParameterAnd("servername", "=")
				.whereParameterAnd("lower(nickname)", "nickname", "=");
		}

		if (null == sAddSeen)
		{
			sAddSeen = new Insert(getDatasource());
			sAddSeen
				.into("Seen")
				.fieldParameter("botname")
				.fieldParameter("channel")
				.fieldParameter("servername")
				.fieldsParameters(SeenData.class);
		}

		if (null == sUpdateSeen)
		{
			sUpdateSeen = new Update(getDatasource());
			sUpdateSeen
				.table("Seen")
				.fieldsParameters(SeenData.class)
				.whereParameter("botname", "=")
				.whereParameterAnd("channel", "=")
				.whereParameterAnd("servername", "=")
				.whereParameterAnd("lower(nickname)", "currentnickname", "=");
		}

		if (null == sDropTableSeen)
		{
			sDropTableSeen = new DropTable(getDatasource());
			sDropTableSeen
				.table("Seen");
		}
	}
	
	public boolean install()
	throws SeenManagerException
	{
		return _install(sCreateTableSeen);
	}

	public void recordSeen(Bot bot, Channel channel, SeenData seenData)
	throws SeenManagerException
	{
		_recordSeen(sAddSeen, sUpdateSeen, bot, channel, seenData);
	}
	
	public SeenData getSeen(Bot bot, Channel channel, String nickname)
	throws SeenManagerException
	{
		return _getSeen(sGetSeen, bot, channel, nickname);
	}
	
	public boolean remove()
	throws SeenManagerException
	{
		return _remove(sDropTableSeen);
	}
}
