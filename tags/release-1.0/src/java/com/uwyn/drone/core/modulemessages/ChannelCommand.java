/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.modulemessages;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.core.Module;
import com.uwyn.drone.core.ModuleMessage;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.rife.tools.ExceptionUtils;
import java.util.logging.Logger;

public class ChannelCommand implements ModuleMessage
{
	private ServerMessage	mServerMessage = null;
	private Channel			mChannel = null;
	private String			mCommand = null;
	private String			mArguments = null;
	
	public ChannelCommand(ServerMessage serverMessage, Channel channel, String command, String arguments)
	{
		if (null == serverMessage)	throw new IllegalArgumentException("serverMessage can't be null.");
		if (null == channel)		throw new IllegalArgumentException("channel can't be null.");
		if (null == command)		throw new IllegalArgumentException("command can't be null.");
		
		mServerMessage = serverMessage;
		mChannel = channel;
		mCommand = command;
		mArguments = arguments;
	}
	
	public void execute(Bot bot, Module module)
	{
		try
		{
			// remove the '!' from the beginning of the command
			module.channelCommand(bot, mChannel, mServerMessage.getPrefix().getNickName(), mCommand.substring(1), mArguments, mServerMessage);
		}
		catch (Throwable e)
		{
			Logger.getLogger("com.uwyn.drone.core").severe("Error during module execution of channel command '"+mCommand+"' : "+ExceptionUtils.getExceptionStackTrace(e));
		}
	}
}
