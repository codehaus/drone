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
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.drone.protocol.commands.Notice;

public class Ping extends AbstractModule
{
	private static final String[]	COMMANDS = new String[] {"\u0001PING"};
	
	public String[] getMessageCommands()
	{
		return COMMANDS;
	}
	
	public void messageCommand(Bot bot, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
		bot.send(new Notice(nick, "\u0001PING "+arguments));
	}
}
