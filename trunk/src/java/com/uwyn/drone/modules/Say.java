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
import com.uwyn.drone.protocol.AttributeCode;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.drone.protocol.commands.Privmsg;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Say extends AbstractModule
{
	private static final String[]	MESSAGE_COMMANDS = new String[] {"say"};
	private static final Pattern	SAY_PATTERN = Pattern.compile("^\\s*(#[^\\s]+)\\s+(.+)\\s*$");
	private static final HashMap	HELPMAP = new HashMap();
	
	static
	{
		HELPMAP.put(null,
			AttributeCode.BOLD+"Say"+AttributeCode.BOLD+" makes the bot talk on a channel."+AttributeCode.ENDLINE+
			"It can be used to make the bot appear human, or to say"+AttributeCode.ENDLINE+
			"something in an anonymous fashion."+AttributeCode.ENDLINE+
			"For more information on a specific command, type        "+AttributeCode.ENDLINE+
			AttributeCode.BOLD+"/msg $botnick help $modulename <command>"+AttributeCode.BOLD+"."+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			AttributeCode.BOLD+"Privmsg commands"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			"    SAY   say a message in a channel"+AttributeCode.ENDLINE);
		
		HELPMAP.put("say",
			"Syntax: "+AttributeCode.BOLD+"SAY <#channel> <message>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Says the provided message in the channel. Note that this"+AttributeCode.ENDLINE+
			"normally only works if the bot has joined the channel."+AttributeCode.ENDLINE);
	}
	
	public String getName()
	{
		return "SAY";
	}
	
	public String getDescription()
	{
		return "Makes the bot talk on a channel.";
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
		if (command.equals("say"))
		{
			if (null == arguments ||
				0 == arguments.length())
			{
				bot.send(new Privmsg(nick, "You need to provide an argument to the command."));
				return;
			}
			
			Matcher seen_matcher = SAY_PATTERN.matcher(arguments);
			
			if (!seen_matcher.matches() ||
				seen_matcher.groupCount() != 2)
			{
				bot.send(new Privmsg(nick, "Invalid syntax '"+command+" "+arguments+"'"));
				return;
			}
			
			// obtain the requested channel
			String channel_name = seen_matcher.group(1).toLowerCase();
			String message = seen_matcher.group(2);
			Channel	channel = bot.getServer().getChannel(channel_name);
			if (null == channel)
			{
				return;
			}

			// say the message on the channel
			channel.send(message);
		}
	}
}
