/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.exceptions;

import com.uwyn.drone.core.Server;
import com.uwyn.drone.protocol.commands.IrcCommand;

public class SendErrorException extends CoreException
{
	private Server	mServer = null;
	private IrcCommand	mCommand = null;
	
	public SendErrorException(Server server, IrcCommand command, Throwable cause)
	{
		super("Unexpected error while sending the message '"+command.getCommand()+"' to the server '"+server+"'.", cause);
		
		mServer = server;
		mCommand = command;
	}
	
	public Server getServer()
	{
		return mServer;
	}
	
	public IrcCommand getCommand()
	{
		return mCommand;
	}
}

