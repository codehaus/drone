/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.modulemessages;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Module;
import com.uwyn.drone.core.ModuleMessage;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.rife.tools.ExceptionUtils;
import java.util.logging.Logger;

public class RawCommand implements ModuleMessage
{
	private ServerMessage	mServerMessage = null;
	
	public RawCommand(ServerMessage serverMessage)
	{
		if (null == serverMessage)	throw new IllegalArgumentException("serverMessage can't be null.");
		
		mServerMessage = serverMessage;
	}
	
	public void execute(Bot bot, Module module)
	{
		try
		{
			module.rawCommand(bot, mServerMessage.getPrefix().getNickName(), mServerMessage.getCommand(), mServerMessage);
		}
		catch (Throwable e)
		{
			Logger.getLogger("com.uwyn.drone.core").severe("Error during module execution of the raw command '"+mServerMessage.getCommand()+"' : "+ExceptionUtils.getExceptionStackTrace(e));
		}
	}
}
