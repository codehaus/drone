/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.seenmanagement;

import com.uwyn.rife.database.queries.*;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.modules.exceptions.SeenManagerException;
import com.uwyn.drone.modules.seenmanagement.SeenManager;
import com.uwyn.drone.modules.seenmanagement.exceptions.GetSeenErrorException;
import com.uwyn.drone.modules.seenmanagement.exceptions.InstallErrorException;
import com.uwyn.drone.modules.seenmanagement.exceptions.RecordSeenErrorException;
import com.uwyn.drone.modules.seenmanagement.exceptions.RemoveErrorException;
import com.uwyn.rife.database.Datasource;
import com.uwyn.rife.database.DbPreparedStatement;
import com.uwyn.rife.database.DbPreparedStatementHandler;
import com.uwyn.rife.database.DbQueryManager;
import com.uwyn.rife.database.exceptions.DatabaseException;

public abstract class DatabaseSeen extends DbQueryManager implements SeenManager
{
    protected DatabaseSeen(Datasource datasource)
    {
        super(datasource);
    }

	public abstract boolean install()
	throws SeenManagerException;
	
	public abstract boolean remove()
	throws SeenManagerException;

	protected boolean _install(CreateTable createTableSeen)
	throws SeenManagerException
	{
		assert createTableSeen != null;
		
		try
		{
			executeUpdate(createTableSeen);
		}
		catch (DatabaseException e)
		{
			throw new InstallErrorException(e);
		}
		
		return true;
	}
	
	protected void _recordSeen(Insert addSeen, Update updateSeen, final Bot bot, final Channel channel, final SeenData seenData)
	throws SeenManagerException
	{
		assert addSeen != null;
		assert updateSeen != null;
		
		if (null == bot)		throw new IllegalArgumentException("bot can't be null.");
		if (null == channel)	throw new IllegalArgumentException("channel can't be null.");
		if (null == seenData)	throw new IllegalArgumentException("seenData can't be null.");
		
		try
		{
			final SeenData	current_seen = getSeen(bot, channel, seenData.getNickname());
			
			Query query = null;
			if (null == current_seen)
			{
				query = addSeen;
			}
			else
			{
				query = updateSeen;
			}
			
			if (0 == executeUpdate(query, new DbPreparedStatementHandler() {
					public void setParameters(DbPreparedStatement statement)
					{
						statement
							.setString("botname", bot.getName())
							.setString("channel", channel.getName())
							.setString("servername", channel.getServer().getServerName())
							.setBean(seenData);
						if (current_seen != null)
						{
							statement
								.setString("currentnickname", current_seen.getNickname().toLowerCase());
						}
					}
				}))
			{
				throw new RecordSeenErrorException(bot, channel, seenData);
			}
		}
		catch (DatabaseException e)
		{
			throw new RecordSeenErrorException(bot, channel, seenData, e);
		}
	}
	
	protected SeenData _getSeen(Select getSeen, final Bot bot, final Channel channel, final String nickname)
	throws SeenManagerException
	{
		assert getSeen != null;
		
		if (null == bot)			throw new IllegalArgumentException("bot can't be null.");
		if (null == channel)		throw new IllegalArgumentException("channel can't be null.");
		if (null == nickname)		throw new IllegalArgumentException("nickname can't be null.");
		if (0 == nickname.length())	throw new IllegalArgumentException("nickname can't be empty.");
		
		SeenData result = null;
		try
		{
			result = (SeenData)executeFetchFirstBean(getSeen, SeenData.class, new DbPreparedStatementHandler() {
					public void setParameters(DbPreparedStatement statement)
					{
						statement
							.setString("botname", bot.getName())
							.setString("channel", channel.getName())
							.setString("servername", channel.getServer().getServerName())
							.setString("nickname", nickname.toLowerCase());
					}
				});
		}
		catch (DatabaseException e)
		{
			throw new GetSeenErrorException(bot, channel, nickname);
		}
		
		return result;
	}
	
	protected boolean _remove(DropTable dropTableSeen)
	throws SeenManagerException
	{
		assert dropTableSeen != null;

		try
		{
			executeUpdate(dropTableSeen);
		}
		catch (DatabaseException e)
		{
			throw new RemoveErrorException(e);
		}
		
		return true;
	}
}
