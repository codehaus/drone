/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules;

import com.uwyn.drone.core.AbstractModule;
import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.modules.exceptions.LogManagerException;
import com.uwyn.drone.modules.logmanagement.DatabaseLogs;
import com.uwyn.drone.modules.logmanagement.DatabaseLogsFactory;
import com.uwyn.drone.modules.logmanagement.LogResultProcessor;
import com.uwyn.drone.modules.logmanagement.exceptions.InvalidSearchSyntaxException;
import com.uwyn.drone.protocol.AttributeCode;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.drone.protocol.commands.Privmsg;
import com.uwyn.rife.config.Config;
import com.uwyn.rife.tools.ExceptionUtils;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Log extends AbstractModule
{
	private static final int				MAX_RESULTS = Config.getRepInstance().getInt("MAX_SEARCH_RESULTS", 20);
    private static final SimpleDateFormat	DATE_FORMAT = new SimpleDateFormat("EEE dd MMM yyyy HH:mm 'GMT'");
	private static final String[]			MESSAGE_COMMANDS = new String[] {"logsearch"};
	private static final Pattern			SEARCH_PATTERN = Pattern.compile("^\\s*(#\\w+)\\s+(.+)\\s*$");
	private static final String				SEARCH_SYNTAX = "[query];[nick=name];[begin=[[yyyy/]mm/dd] [hh:mm]];[end=[[yyyy/]mm/dd] [hh:mm]];[#=count]";
	private static final HashMap			HELPMAP = new HashMap();
	private static final String				IRC_ACTION = "\u0001ACTION";
	
	static
	{
		HELPMAP.put(null,
			AttributeCode.BOLD+"Log"+AttributeCode.BOLD+" stores all the conversions on a channel."+AttributeCode.ENDLINE+
			"It can be used to search in detail what was said by whom"+AttributeCode.ENDLINE+
			"at which moment."+AttributeCode.ENDLINE+
			"For more information on a specific command, type        "+AttributeCode.ENDLINE+
			AttributeCode.BOLD+"/msg $botnick help $modulename <command>"+AttributeCode.BOLD+"."+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			AttributeCode.BOLD+"Privmsg commands"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			"    LOGSEARCH   searches the log of a channel"+AttributeCode.ENDLINE);
		
		HELPMAP.put("logsearch",
			"Syntax: "+AttributeCode.BOLD+"LOGSEARCH <#channel> <search>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Queries the stored logs of the public conversations in a"+AttributeCode.ENDLINE+
			"channel."+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"The format of the search parameter is as follows: "+AttributeCode.ENDLINE+
			AttributeCode.BOLD+"[query];[nick=name];[begin=date];[end=date];[#=count]"+AttributeCode.BOLD+AttributeCode.ENDLINE+
			"where at least one part should be present, below is"+AttributeCode.ENDLINE+
			"a detailed explanation of each query part."+AttributeCode.ENDLINE+
			"    "+AttributeCode.BOLD+"query"+AttributeCode.BOLD+"   is a string with '%' as wildcard"+AttributeCode.ENDLINE+
			"    "+AttributeCode.BOLD+"name"+AttributeCode.BOLD+"    is the nick name of someone who spoke"+AttributeCode.ENDLINE+
			"    "+AttributeCode.BOLD+"date"+AttributeCode.BOLD+"    with the format '[[yyyy/]mm/dd] [hh:mm]'"+AttributeCode.ENDLINE+
			"    "+AttributeCode.BOLD+"count"+AttributeCode.BOLD+"   limits the results"+AttributeCode.ENDLINE+
			"In any case, "+MAX_RESULTS+" results are returned maximum"+AttributeCode.ENDLINE);
	}
	
	public String getName()
	{
		return "LOG";
	}
	
	public String getDescription()
	{
		return "Logs all the conversations on a channel.";
	}

	public Map getHelpMap()
	{
		return HELPMAP;
	}
	
	public String[] getMessageCommands()
	{
		return MESSAGE_COMMANDS;
	}
	
	public boolean processesChannelMessages()
	{
		return true;
	}
	
	public void messageCommand(Bot bot, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
		DatabaseLogs	database_log = DatabaseLogsFactory.get();
		try
		{
			if (command.equals("logsearch"))
			{
				if (null == arguments ||
				   0 == arguments.length())
				{
					bot.send(new Privmsg(nick, "You need to provide a search argument."));
					return;
				}
				
				Matcher logsearch_matcher = SEARCH_PATTERN.matcher(arguments);
				
				if (!logsearch_matcher.matches() ||
					logsearch_matcher.groupCount() != 2)
				{
					bot.send(new Privmsg(nick, "Invalid syntax '"+command+" "+arguments+"'"));
					return;
				}
				
				// obtain the requested channel
				String channel_name = logsearch_matcher.group(1);
				String search = logsearch_matcher.group(2);
				Channel	channel = bot.getServer().getChannel(channel_name);
				if (null == channel)
				{
					bot.send(new Privmsg(nick, "Unknown channel '"+channel_name+"'"));
					return;
				}
				
				SearchResults	search_results = new SearchResults(bot, nick);
				try
				{
					if (!database_log.searchLog(search_results, bot, channel, search))
					{
						bot.send(new Privmsg(nick, "No results for '"+search+"' could be found in channel '"+channel_name+"'."));
					}
				}
				catch (InvalidSearchSyntaxException e)
				{
					bot.send(new Privmsg(nick, "The search syntax of '"+search+"' is not valid, it should be like this :"));
					bot.send(new Privmsg(nick, SEARCH_SYNTAX));
				}
			}
		}
		catch (LogManagerException e)
		{
			Logger.getLogger("com.uwyn.drone.modules").severe(ExceptionUtils.getExceptionStackTrace(e));
		}
	}

	public void channelMessage(Bot bot, Channel channel, String nick, ServerMessage fullMessage)
	throws CoreException
	{
		// don't log commands
		if (!fullMessage.getTrailing().startsWith("!"))
		{
			DatabaseLogs	database_log = DatabaseLogsFactory.get();
			try
			{
				database_log.addLog(bot, channel, fullMessage);
			}
			catch (LogManagerException e)
			{
				Logger.getLogger("com.uwyn.drone.modules").severe(ExceptionUtils.getExceptionStackTrace(e));
			}
		}
	}
	
	private class SearchResults extends LogResultProcessor
	{
		private Bot		mBot = null;
		private String	mNick = null;
		
		public SearchResults(Bot bot, String nick)
		{
			assert bot != null;
			assert nick != null;
			
			mBot = bot;
			mNick = nick;
		}
		
		public boolean gotMessage(Timestamp moment, ServerMessage serverMessage)
		{
			try
			{
				if (getCount() <= MAX_RESULTS)
				{
					StringBuffer	formatted_result = new StringBuffer();
            		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
					formatted_result.append("[");
					formatted_result.append(DATE_FORMAT.format(moment));
					formatted_result.append("] (");
					formatted_result.append(serverMessage.getPrefix().getNickName());
					formatted_result.append(") ");
				
					// translate the \u0001ACTION command which corresponds to
					// /me so that the user's nickname is used instead
					if (serverMessage.getPrefix() != null &&
						serverMessage.getTrailing().startsWith(IRC_ACTION))
					{
						formatted_result.append(serverMessage.getPrefix().getNickName());
						formatted_result.append(serverMessage.getTrailing().substring(IRC_ACTION.length()));
					}
					else
					{
						formatted_result.append(serverMessage.getTrailing());
					}
					
					mBot.send(new Privmsg(mNick, formatted_result.toString()));
					
					return true;
				}
				else
				{
					mBot.send(new Privmsg(mNick, "!!! more than "+MAX_RESULTS+" matches, refine your search."));
				}
			}
			catch (CoreException e)
			{
				Logger.getLogger("com.uwyn.drone.modules").severe(ExceptionUtils.getExceptionStackTrace(e));
			}
			
			return false;
		}
	}
}
