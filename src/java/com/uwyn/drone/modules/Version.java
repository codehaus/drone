/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules;

import com.uwyn.drone.Droned;
import com.uwyn.drone.core.AbstractModule;
import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.drone.protocol.commands.Notice;

public class Version  extends AbstractModule
{
	private static final String[]	COMMANDS = new String[] {"\u0001VERSION"};
	
	public String[] getMessageCommands()
	{
		return COMMANDS;
	}
	
	public void messageCommand(Bot bot, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
		bot.send(new Notice(nick, "\u0001VERSION UWYN Drone IRC Bot "+Droned.getVersion()+", "+System.getProperty("os.name")+" "+System.getProperty("os.arch")+" "+System.getProperty("os.version")+", Java "+System.getProperty("java.vendor")+" "+System.getProperty("java.version")));
	}
}
