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
import com.uwyn.drone.protocol.ServerMessage;

public class Throughput  extends AbstractModule
{
	private static final String[]	COMMANDS = new String[] {"throughput"};
	
	public String[] getChannelCommands()
	{
		return COMMANDS;
	}
	
	public void channelCommand(Bot bot, Channel channel, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
		for (int i = 0; i < 100; i++)
		{
			channel.send(i+":01234567890123456789012345678901");
		}
	}
}
