/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules;

import java.util.*;

import com.uwyn.drone.core.AbstractModule;
import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.modules.exceptions.SeenManagerException;
import com.uwyn.drone.modules.seenmanagement.DatabaseSeen;
import com.uwyn.drone.modules.seenmanagement.DatabaseSeenFactory;
import com.uwyn.drone.modules.seenmanagement.SeenData;
import com.uwyn.drone.protocol.AttributeCode;
import com.uwyn.drone.protocol.ResponseCode;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.drone.protocol.commands.Names;
import com.uwyn.drone.protocol.commands.Privmsg;
import com.uwyn.rife.tools.ExceptionUtils;
import com.uwyn.rife.tools.StringUtils;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Seen extends AbstractModule
{
    private static final SimpleDateFormat	DATE_FORMAT = new SimpleDateFormat("EEE dd MMM yyyy HH:mm 'GMT'");
	private static final String[]			CHANNEL_COMMANDS = new String[] {"seen"};
	private static final String[]			MESSAGE_COMMANDS = new String[] {"seen"};
	private static final ResponseCode[]		RESPONSE_CODES = new ResponseCode[] {ResponseCode.RPL_NAMREPLY, ResponseCode.RPL_ENDOFNAMES};
	private static final Pattern			SEEN_PATTERN = Pattern.compile("^\\s*(#\\w+)\\s+(.+)\\s*$");
	private static final HashMap			HELPMAP = new HashMap();
	
	static
	{
		HELPMAP.put(null,
			AttributeCode.BOLD+"Seen"+AttributeCode.BOLD+" records when someone last talked on a channel."+AttributeCode.ENDLINE+
			"It can be used to easily know when someone was active"+AttributeCode.ENDLINE+
			"on a channel for the last time."+AttributeCode.ENDLINE+
			"For more information on a specific command, type        "+AttributeCode.ENDLINE+
			AttributeCode.BOLD+"/msg $botnick help $modulename <command>"+AttributeCode.BOLD+"."+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			AttributeCode.BOLD+"Channel commands"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			"    !SEEN  checks when a nick was last seen"+AttributeCode.ENDLINE+
			AttributeCode.BOLD+"Privmsg commands"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			"    SEEN   checks when a nick was last seen in a channel"+AttributeCode.ENDLINE);
		
		HELPMAP.put("!seen",
			"Syntax: "+AttributeCode.BOLD+"!SEEN <nick>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Checks when someone last spoke on a channel. The time is"+AttributeCode.ENDLINE+
			"output together with what the last message was."+AttributeCode.ENDLINE+
			"If the person is currently online, this will be noticed"+AttributeCode.ENDLINE+
			"and reported accordingly."+AttributeCode.ENDLINE);
		
		HELPMAP.put("seen",
			"Syntax: "+AttributeCode.BOLD+"SEEN <#channel> <nick>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Checks when someone last spoke on a channel. The time is"+AttributeCode.ENDLINE+
			"output together with what the last message was."+AttributeCode.ENDLINE+
			"If the person is currently online, this will be noticed"+AttributeCode.ENDLINE+
			"and reported accordingly."+AttributeCode.ENDLINE);
	}
	
	private	SeenQueue	mChannelSeensWaiting = new SeenQueue();
	private	SeenQueue	mMessageSeensWaiting = new SeenQueue();
	
	private	SeenQueue	mFinishedChannelNames = new SeenQueue();
	private	SeenQueue	mConstructedChannelNames = new SeenQueue();
	
	public String getName()
	{
		return "SEEN";
	}
	
	public String getDescription()
	{
		return "Stores when someone was last seen on a channel.";
	}
	
	public Map getHelpMap()
	{
		return HELPMAP;
	}
	
	public String[] getChannelCommands()
	{
		return CHANNEL_COMMANDS;
	}
	
	public String[] getMessageCommands()
	{
		return MESSAGE_COMMANDS;
	}
	
	public boolean processesChannelMessages()
	{
		return true;
	}
	
	public ResponseCode[] getResponseCodes()
	{
		return RESPONSE_CODES;
	}
	
	public void channelCommand(Bot bot, Channel channel, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
		if (command.equals("seen"))
		{
			String seen = arguments.trim().toLowerCase();
			
			// First drone requests the list of current channel names to
			// see of the requested nick isn't currently online.
			// After the response of the names-list, drone will determine
			// what the correct answer is
			// This is done in the response() method.
			synchronized (mChannelSeensWaiting)
			{
				// register the nick as waiting for a repsonse
				ArrayList seens_waiting = mChannelSeensWaiting.getSeens(channel);
				if (null == seens_waiting)
				{
					seens_waiting = new ArrayList();
					mChannelSeensWaiting.put(channel, seens_waiting);
				}
				if (!seens_waiting.contains(seen))
				{
					seens_waiting.add(seen);
					
					// request the current list of names in the channel
					bot.send(new Names(channel.getName()));
				}
			}
		}
	}
	
	public void messageCommand(Bot bot, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
		if (command.equals("seen"))
		{
			if (null == arguments ||
				0 == arguments.length())
			{
				bot.send(new Privmsg(nick, "You need to provide an argument to the command."));
				return;
			}
			
			Matcher seen_matcher = SEEN_PATTERN.matcher(arguments.toLowerCase());
			
			if (!seen_matcher.matches() ||
				seen_matcher.groupCount() != 2)
			{
				bot.send(new Privmsg(nick, "Invalid syntax '"+command+" "+arguments+"'"));
				return;
			}
			
			// obtain the requested channel
			String channel_name = seen_matcher.group(1);
			String seen = seen_matcher.group(2);
			Channel	channel = bot.getServer().getChannel(channel_name);
			if (null == channel)
			{
				return;
			}

			// First drone requests the list of current channel names to
			// see of the requested nick isn't currently online.
			// After the response of the names-list, drone will determine
			// what the correct answer is.
			// This is done in the response() method.
			synchronized (mMessageSeensWaiting)
			{
				// register the nick as waiting for a repsonse
				ArrayList seens_waiting = mMessageSeensWaiting.getSeens(channel);
				if (null == seens_waiting)
				{
					seens_waiting = new ArrayList();
					mMessageSeensWaiting.put(channel, seens_waiting);
				}
				
				int			message_seen_index = seens_waiting.indexOf(seen);
				MessageSeen	message_seen = null;
				if (-1 == message_seen_index)
				{
					message_seen = new MessageSeen(seen);
					seens_waiting.add(message_seen);
				}
				else
				{
					message_seen = (MessageSeen)seens_waiting.get(message_seen_index);
				}
				
				if (!message_seen.getNicks().contains(nick))
				{
					message_seen.getNicks().add(nick);
					
					// request the current list of names in the channel
					bot.send(new Names(channel.getName()));
				}
			}
		}
	}
	
	public void channelMessage(Bot bot, Channel channel, String nick, ServerMessage fullMessage)
	throws CoreException
	{
		// don't log bot commands
		if (!fullMessage.getTrailing().startsWith("!"))
		{
			SeenData		seen_data = new SeenData(nick, new Timestamp(Calendar.getInstance().getTimeInMillis()), fullMessage.getTrailing(), fullMessage.getRaw());
			DatabaseSeen	database_seen = DatabaseSeenFactory.get();
			try
			{
				database_seen.recordSeen(bot, channel, seen_data);
			}
			catch (SeenManagerException e)
			{
				Logger.getLogger("com.uwyn.drone.modules").severe(ExceptionUtils.getExceptionStackTrace(e));
			}
		}
	}

	public void response(Bot bot, String nick, ResponseCode responseCode, ServerMessage fullMessage)
	throws CoreException
	{
		// obtain the channel the response came from
		String	channel_name = null;
		if (ResponseCode.RPL_NAMREPLY == responseCode)
		{
			channel_name = (String)fullMessage.getParameters().get(2);
		}
		if (ResponseCode.RPL_ENDOFNAMES == responseCode)
		{
			channel_name = (String)fullMessage.getParameters().get(1);
		}
		if (null == channel_name ||
			0 == channel_name.length())
		{
			return;
		}
		Channel	channel = bot.getServer().getChannel(channel_name);
		if (null == channel)
		{
			return;
		}
		
		synchronized (mConstructedChannelNames)
		{
			// received additional name entries
			if (ResponseCode.RPL_NAMREPLY == responseCode)
			{
				ArrayList nicknames = mConstructedChannelNames.getSeens(channel);
				if (null == nicknames)
				{
					nicknames = new ArrayList();
					mConstructedChannelNames.put(channel, nicknames);
				}
				ArrayList	received_names = StringUtils.split(fullMessage.getTrailing(), " ");
				Iterator	received_names_it = received_names.iterator();
				String		received_name = null;
				while (received_names_it.hasNext())
				{
					received_name = (String)received_names_it.next();
					// remove operator and voice indicators
					if (received_name.startsWith("@") ||
						received_name.startsWith("+"))
					{
						nicknames.add(received_name.substring(1));
					}
					else
					{
						nicknames.add(received_name);
					}
				}
			}
			// received all name entries, so process all the waiting seen messages
			else if (ResponseCode.RPL_ENDOFNAMES == responseCode)
			{
				ArrayList channel_nicknames = mConstructedChannelNames.removeSeens(channel);
				if (null == channel_nicknames)
				{
					return;
				}
				
				mFinishedChannelNames.put(channel, channel_nicknames);
				
				// check of there are seen nicks names that are waiting for a
				// channel namelist
				if (!mChannelSeensWaiting.hasChannel(channel) &&
					!mMessageSeensWaiting.hasChannel(channel))
				{
					return;
				}

				synchronized (mChannelSeensWaiting)
				{
					ArrayList	channel_seens = mChannelSeensWaiting.getSeens(channel);
					String 		channel_seen = null;
					
					// go over all the waiting channel seen requests and process
					// them, removing them from the waiting list
					while (channel_seens != null &&
						   channel_seens.size() > 0)
					{
						channel_seen = (String)channel_seens.remove(0);
						String reply = processSeenWaiting(bot, channel, channel_seen);
						if (reply != null)
						{
							channel.send(reply);
						}
					}
				}

				synchronized (mMessageSeensWaiting)
				{
					ArrayList	message_seens = mMessageSeensWaiting.getSeens(channel);
					MessageSeen message_seen = null;
					
					// go over all the waiting message seen requests and process
					// them, removing them from the waiting list
					while (message_seens != null &&
						   message_seens.size() > 0)
					{
						message_seen = (MessageSeen)message_seens.remove(0);
						String reply = processSeenWaiting(bot, channel, message_seen.getSeen());
						if (reply != null)
						{
							Iterator	nicks_it = message_seen.getNicks().iterator();
							while (nicks_it.hasNext())
							{
								bot.send(new Privmsg((String)nicks_it.next(), reply));
							}
						}
					}
				}
			}
		}
	}

	public String processSeenWaiting(Bot bot, Channel channel, String seen)
	throws CoreException
	{
		String reply = null;
	
		try
		{
			// query the database for information about the nickname
			DatabaseSeen	database_seen = DatabaseSeenFactory.get();
			SeenData		search_result = database_seen.getSeen(bot, channel, seen);

			DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));

			// check if the nick is part of the current channel names
			Iterator	channel_nicknames_it = mFinishedChannelNames.getSeens(channel).iterator();
			String		channel_nickname = null;
			while (channel_nicknames_it.hasNext())
			{
				channel_nickname = (String)channel_nicknames_it.next();
				if (seen.equals(channel_nickname.toLowerCase()))
				{
					StringBuffer	formatted_result = new StringBuffer();
					formatted_result.append(channel_nickname);
					formatted_result.append(" is currently online in ");
					formatted_result.append(channel.getName());
					if (null == search_result)
					{
						formatted_result.append(", but never said anything that I saw.");
					}
					else
					{
						formatted_result.append(" and last spoke on ");
						formatted_result.append(DATE_FORMAT.format(search_result.getMoment()));
						formatted_result.append(", saying '");
						formatted_result.append(search_result.getDisplayMessage());
						formatted_result.append("'.");
					}
					return formatted_result.toString();
				}
			}
			
			if (null == search_result)
			{
				StringBuffer	formatted_result = new StringBuffer();
				formatted_result.append("I've never seen ");
				formatted_result.append(seen);
				formatted_result.append(" talk in ");
				formatted_result.append(channel.getName());
				formatted_result.append(".");
				reply =  formatted_result.toString();
				return reply;
			}
			else
			{
				ServerMessage message = ServerMessage.parse(search_result.getRaw());
				
				StringBuffer	formatted_result = new StringBuffer();
				formatted_result.append(search_result.getNickname());
				formatted_result.append(" (");
				formatted_result.append(message.getPrefix().getRaw().substring(1));
				formatted_result.append(") was last seen in ");
				formatted_result.append(channel.getName());
				formatted_result.append(" on ");
				formatted_result.append(DATE_FORMAT.format(search_result.getMoment()));
				formatted_result.append(", saying '");
				formatted_result.append(search_result.getDisplayMessage());
				formatted_result.append("'.");
				reply =  formatted_result.toString();
				return reply;
			}
		}
		catch (SeenManagerException e)
		{
			Logger.getLogger("com.uwyn.drone.modules").severe(ExceptionUtils.getExceptionStackTrace(e));
		}
		
		return reply;
	}
	
	private class MessageSeen
	{
		private String		mSeen = null;
		private ArrayList	mNicks = new ArrayList();
		
		private MessageSeen()
		{
		}
		
		public MessageSeen(String seen)
		{
			setSeen(seen);
		}
		
		public void setSeen(String seen)
		{
			mSeen = seen;
		}
		
		public String getSeen()
		{
			return mSeen;
		}
		
		public void setNicks(ArrayList nicks)
		{
			mNicks = nicks;
		}
		
		public ArrayList getNicks()
		{
			return mNicks;
		}
		
		public boolean equals(Object other)
		{
			if (other instanceof MessageSeen)
			{
				MessageSeen other_instance = (MessageSeen)other;
				if (null != other_instance &&
					other_instance.getSeen().equals(this.getSeen()))
				{
					return true;
				}
			}
			if (other instanceof String)
			{
				String other_instance = (String)other;
				if (null != other_instance &&
					other_instance.equals(this.getSeen()))
				{
					return true;
				}
			}
			
			return false;
		}
		
		public int hashCode()
		{
			return mSeen.hashCode();
		}
	}
	
	private class SeenQueue
	{
		private	HashMap	mChannelSeens = new HashMap();
		
		public void put(Channel channel, ArrayList seens)
		{
			mChannelSeens.put(channel, seens);
		}
		
		public boolean hasChannel(Channel channel)
		{
			return mChannelSeens.containsKey(channel);
		}
		
		public ArrayList getSeens(Channel channel)
		{
			return (ArrayList)mChannelSeens.get(channel);
		}
		
		public ArrayList removeSeens(Channel channel)
		{
			return (ArrayList)mChannelSeens.remove(channel);
		}
	}
}
