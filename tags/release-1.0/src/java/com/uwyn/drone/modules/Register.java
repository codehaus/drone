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
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.protocol.AttributeCode;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.drone.protocol.commands.Privmsg;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AbstractModule
{
	private static final String[]	MESSAGE_COMMANDS = new String[] {"register"};
	private static final Pattern	REGISTER_PATTERN = Pattern.compile("^\\s*([^\\s]+)\\s+(.+)\\s*$");
	private static final HashMap	HELPMAP = new HashMap();
	
	static
	{
		HELPMAP.put(null,
			AttributeCode.BOLD+"Register"+AttributeCode.BOLD+" makes the bot register itself with a server bot."+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Syntax: "+AttributeCode.BOLD+"REGISTER <botnick> <password>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Registers the bot with its current nick with the provided"+AttributeCode.ENDLINE+
			"bot nick and the provided password."+AttributeCode.ENDLINE);
	}
	
	public String getName()
	{
		return "REGISTER";
	}
	
	public String getDescription()
	{
		return "Registers the bot with a nick bot.";
	}
	
	public Map getHelpMap()
	{
		return HELPMAP;
	}
	
	public String[] getMessageCommands()
	{
		return MESSAGE_COMMANDS;
	}
	
	public void messageCommand(Bot bot, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
		if (command.equals("register"))
		{
			if (null == arguments ||
				arguments.length() < 2)
			{
				bot.send(new Privmsg(nick, "You need to provide 2 arguments to the command."));
				return;
			}
			
			Matcher register_matcher = REGISTER_PATTERN.matcher(arguments);
			
			if (!register_matcher.matches() ||
				register_matcher.groupCount() != 2)
			{
				bot.send(new Privmsg(nick, "Invalid syntax '"+command+" "+arguments+"'"));
				return;
			}
			
			String botnick = register_matcher.group(1).toLowerCase();
			String password = register_matcher.group(2);
			bot.send(new Privmsg(botnick, "register "+password));
		}
	}
}
