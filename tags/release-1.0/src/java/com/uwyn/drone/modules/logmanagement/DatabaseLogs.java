/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.logmanagement;

import com.uwyn.drone.modules.logmanagement.exceptions.*;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.modules.exceptions.LogManagerException;
import com.uwyn.drone.modules.logmanagement.LogManager;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.rife.database.Datasource;
import com.uwyn.rife.database.DbPreparedStatement;
import com.uwyn.rife.database.DbPreparedStatementHandler;
import com.uwyn.rife.database.DbQueryManager;
import com.uwyn.rife.database.exceptions.DatabaseException;
import com.uwyn.rife.database.queries.CreateTable;
import com.uwyn.rife.database.queries.DropTable;
import com.uwyn.rife.database.queries.Insert;
import com.uwyn.rife.database.queries.Select;
import com.uwyn.rife.tools.StringUtils;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

public abstract class DatabaseLogs extends DbQueryManager implements LogManager
{
	private static SimpleDateFormat	FULL_FORMAT = null;
	private static SimpleDateFormat	YEARDATE_FORMAT = null;
	private static SimpleDateFormat	DATETIME_FORMAT = null;
	private static SimpleDateFormat	DATE_FORMAT = null;
	private static SimpleDateFormat	TIME_FORMAT = null;
	
	static
	{
		FULL_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		YEARDATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
		DATETIME_FORMAT = new SimpleDateFormat("MM/dd HH:mm");
		DATE_FORMAT = new SimpleDateFormat("MM/dd");
		TIME_FORMAT = new SimpleDateFormat("HH:mm");
		
		FULL_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
		YEARDATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
		DATETIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
		TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
    protected DatabaseLogs(Datasource datasource)
    {
        super(datasource);
    }

	public abstract boolean install()
	throws LogManagerException;
	
	public abstract boolean remove()
	throws LogManagerException;

	protected boolean _install(CreateTable createTableLog, String createGetLogMessagesIndex)
	throws LogManagerException
	{
		assert createTableLog != null;
		
		try
		{
			executeUpdate(createTableLog);
			executeUpdate(createGetLogMessagesIndex);
		}
		catch (DatabaseException e)
		{
			throw new InstallErrorException(e);
		}
		
		return true;
	}
	
	protected void _addLog(Insert addLog, final Bot bot, final Channel channel, final ServerMessage serverMessage)
	throws LogManagerException
	{
		assert addLog != null;
		
		if (null == bot)			throw new IllegalArgumentException("bot can't be null.");
		if (null == channel)		throw new IllegalArgumentException("channel can't be null.");
		if (null == serverMessage)	throw new IllegalArgumentException("serverMessage can't be null.");
		
		try
		{
			if (0 == executeUpdate(addLog, new DbPreparedStatementHandler() {
					public void setParameters(DbPreparedStatement statement)
					{
						statement
							.setString("botname", bot.getName())
							.setString("channel", channel.getName())
							.setString("servername", channel.getServer().getServerName())
							.setString("nickname", serverMessage.getPrefix().getNickName())
							.setString("username", serverMessage.getPrefix().getUser())
							.setString("hostname", serverMessage.getPrefix().getHost())
							.setString("message", serverMessage.getTrailing())
							.setString("raw", serverMessage.getRaw());
					}
				}))
			{
				throw new AddLogErrorException(bot, channel, serverMessage);
			}
		}
		catch (DatabaseException e)
		{
			throw new AddLogErrorException(bot, channel, serverMessage, e);
		}
	}
	
	private Timestamp parseInterval(String interval)
	{
		Timestamp result = null;
		
		Calendar result_cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Calendar now_cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		try
		{
			Date search_begin_date = FULL_FORMAT.parse(interval);
			result_cal.setTime(search_begin_date);
			result = new Timestamp(result_cal.getTimeInMillis());
		}
		catch (ParseException e)
		{
			try
			{
				Date search_begin_date = YEARDATE_FORMAT.parse(interval);
				result_cal.setTime(search_begin_date);
				result_cal.set(Calendar.HOUR, now_cal.get(Calendar.HOUR));
				result_cal.set(Calendar.MINUTE, now_cal.get(Calendar.MINUTE));
				result = new Timestamp(result_cal.getTimeInMillis());
			}
			catch (ParseException e2)
			{
				try
				{
					Date search_begin_date = DATETIME_FORMAT.parse(interval);
					result_cal.setTime(search_begin_date);
					result_cal.set(Calendar.YEAR, now_cal.get(Calendar.YEAR));
					result = new Timestamp(result_cal.getTimeInMillis());
				}
				catch (ParseException e3)
				{
					try
					{
						Date search_begin_date = DATE_FORMAT.parse(interval);
						result_cal.setTime(search_begin_date);
						result_cal.set(Calendar.YEAR, now_cal.get(Calendar.YEAR));
						result_cal.set(Calendar.HOUR, now_cal.get(Calendar.HOUR));
						result_cal.set(Calendar.MINUTE, now_cal.get(Calendar.MINUTE));
						result = new Timestamp(result_cal.getTimeInMillis());
					}
					catch (ParseException e4)
					{
						try
						{
							Date search_begin_date = TIME_FORMAT.parse(interval);
							result_cal.setTime(search_begin_date);
							result_cal.set(Calendar.YEAR, now_cal.get(Calendar.YEAR));
							result_cal.set(Calendar.MONTH, now_cal.get(Calendar.MONTH));
							result_cal.set(Calendar.DATE, now_cal.get(Calendar.DATE));
							result = new Timestamp(result_cal.getTimeInMillis());
						}
						catch (ParseException e5)
						{
							return null;
						}
					}
				}
			}
		}
		
		return result;
	}
	
	protected boolean _searchLog(Select searchLog, LogResultProcessor processor, final Bot bot, final Channel channel, final String search)
	throws LogManagerException
	{
		assert searchLog != null;
		
		if (null == bot)			throw new IllegalArgumentException("bot can't be null.");
		if (null == channel)		throw new IllegalArgumentException("channel can't be null.");
		if (null == search)			throw new IllegalArgumentException("search can't be null");
		if (0 == search.length())	throw new IllegalArgumentException("search can't be empty");
		
		boolean result = false;
		try
		{
			String		search_message = null;
			String		search_nickname = null;
			Timestamp	search_begin = null;
			Timestamp	search_end = null;
			int			search_count = -1;
			
			Select		searchLogAdapted = (Select)searchLog.clone();
			ArrayList	search_parts = StringUtils.split(search, ";");
			Iterator	search_parts_it = search_parts.iterator();
			String		search_part = null;
			while (search_parts_it.hasNext())
			{
				search_part = (String)search_parts_it.next();
				
				if (search_part.startsWith("nick="))
				{
					int equality_index = search_part.indexOf("=");
					if (search_nickname != null ||
						-1 == equality_index)
					{
						throw new InvalidSearchSyntaxException(bot, channel, search);
					}
					
					search_nickname = search_part.substring(equality_index+1).toLowerCase();
					searchLogAdapted.whereParameterAnd("lower(nickname)", "nickname" , "=");
				}
				
				else if (search_part.startsWith("begin="))
				{
					int equality_index = search_part.indexOf("=");
					if (search_begin != null ||
						-1 == equality_index)
					{
						throw new InvalidSearchSyntaxException(bot, channel, search);
					}
					
					if (null == (search_begin = parseInterval(search_part.substring(equality_index+1))))
					{
						throw new InvalidSearchSyntaxException(bot, channel, search);
					}
					
					searchLogAdapted.whereParameterAnd("moment", "begin", ">=");
				}
				
				else if (search_part.startsWith("end="))
				{
					int equality_index = search_part.indexOf("=");
					if (search_end != null ||
						-1 == equality_index)
					{
						throw new InvalidSearchSyntaxException(bot, channel, search);
					}
					
					if (null == (search_end = parseInterval(search_part.substring(equality_index+1))))
					{
						throw new InvalidSearchSyntaxException(bot, channel, search);
					}
					
					searchLogAdapted.whereParameterAnd("moment", "end", "<");
				}
				
				else if (search_part.startsWith("#="))
				{
					int equality_index = search_part.indexOf("=");
					if (search_count != -1 ||
						-1 == equality_index)
					{
						throw new InvalidSearchSyntaxException(bot, channel, search);
					}
					
					try
					{
						search_count = Integer.parseInt(search_part.substring(equality_index+1));
						searchLogAdapted.limit(search_count);
					}
					catch (NumberFormatException e)
					{
						throw new InvalidSearchSyntaxException(bot, channel, search);
					}
				}
				
				else
				{
					if (search_message != null)
					{
						throw new InvalidSearchSyntaxException(bot, channel, search);
					}
					
					search_message = search_part.toLowerCase();
					searchLogAdapted.whereParameterAnd("lower(message)", "message", "LIKE");
				}
			}

			if (null == search_message &&
				null == search_nickname &&
				null == search_begin &&
				null == search_end &&
				-1 == search_count)
			{
				throw new InvalidSearchSyntaxException(bot, channel, search);
			}

			final String	param_search_message = search_message;
			final String	param_search_nickname = search_nickname;
			final Timestamp	param_search_begin = search_begin;
			final Timestamp	param_search_end = search_end;
			result = executeFetchAll(searchLogAdapted, processor, new DbPreparedStatementHandler() {
				public void setParameters(DbPreparedStatement statement)
				{
					statement
						.setString("botname", bot.getName())
						.setString("channel", channel.getName())
						.setString("servername", channel.getServer().getServerName());
					if (param_search_message != null)
					{
						statement.setString("message", "%"+param_search_message+"%");
					}
					if (param_search_nickname != null)
					{
						statement.setString("nickname", param_search_nickname);
					}
					if (param_search_begin != null)
					{
						statement.setTimestamp("begin", param_search_begin);
					}
					if (param_search_end != null)
					{
						statement.setTimestamp("end", param_search_end);
					}
				}});
		}
		catch (DatabaseException e)
		{
			throw new SearchLogErrorException(bot, channel, search, e);
		}
		
		return result;
	}
	
	protected boolean _getLogMessages(Select getLogMessages, LogResultProcessor processor, final Bot bot, final Channel channel, Calendar day)
	throws LogManagerException
	{
		assert getLogMessages != null;
		
		if (null == bot)		throw new IllegalArgumentException("bot can't be null.");
		if (null == channel)	throw new IllegalArgumentException("channel can't be null.");
		if (null == day)		throw new IllegalArgumentException("day can't be null.");
		
		Calendar begin = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Calendar end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		begin.set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		end.set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		end.add(Calendar.DAY_OF_MONTH, 1);

		boolean result = false;
		try
		{
			final Timestamp timestamp_begin = new Timestamp(begin.getTimeInMillis());
			final Timestamp timestamp_end = new Timestamp(end.getTimeInMillis());
			result = executeFetchAll(getLogMessages, processor, new DbPreparedStatementHandler() {
				public void setParameters(DbPreparedStatement statement)
				{
					statement
						.setString("botname", bot.getName())
						.setString("channel", channel.getName())
						.setString("servername", channel.getServer().getServerName())
						.setTimestamp("begin", timestamp_begin)
						.setTimestamp("end", timestamp_end)
						.executeQuery();
				}});
		}
		catch (DatabaseException e)
		{
			throw new GetLogMessagesErrorException(bot, channel, day, e);
		}
		
		return result;
	}
	
	protected boolean _remove(DropTable dropTableLog, String dropGetLogMessagesIndex)
	throws LogManagerException
	{
		assert dropTableLog != null;

		try
		{
			executeUpdate(dropGetLogMessagesIndex);
			executeUpdate(dropTableLog);
		}
		catch (DatabaseException e)
		{
			throw new RemoveErrorException(e);
		}
		
		return true;
	}
}
