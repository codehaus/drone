/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.protocol.ResponseCode;
import com.uwyn.drone.protocol.ServerMessage;
import java.util.Map;

public abstract class AbstractModule implements Module
{
	private ModuleRunner	mRunner = null;

	public void setRunner(ModuleRunner runner)
	{
		if (null == runner)	throw new IllegalArgumentException("runner can't be null.");
		
		mRunner = runner;
	}
	
	public ModuleRunner getRunner()
	{
		return mRunner;
	}

	public String getName()
	{
		return null;
	}

	public String getDescription()
	{
		return null;
	}

	public Map getHelpMap()
	{
		return null;
	}

	public String[] getMessageCommands()
	{
		return null;
	}
	
	public String[] getNoticeCommands()
	{
		return null;
	}
	
	public String[] getChannelCommands()
	{
		return null;
	}
	
	public boolean processesChannelMessages()
	{
		return false;
	}
	
	public String[] getRawCommands()
	{
		return null;
	}

	public ResponseCode[] getResponseCodes()
	{
		return null;
	}
	
	public void loggedon(Bot bot)
	throws CoreException
	{
	}
	
	public void messageCommand(Bot bot, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
	}
	
	public void noticeCommand(Bot bot, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
	}
	
	public void channelCommand(Bot bot, Channel channel, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
	}
	
	public void channelMessage(Bot bot, Channel channel, String nick, ServerMessage fullMessage)
	throws CoreException
	{
	}
	
	public void rawCommand(Bot bot, String nick, String command, ServerMessage fullMessage)
	throws CoreException
	{
	}

	public void response(Bot bot, String nick, ResponseCode responseCode, ServerMessage fullMessage)
	throws CoreException
	{
	}
}
