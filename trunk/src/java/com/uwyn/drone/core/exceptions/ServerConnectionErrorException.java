/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.exceptions;

import com.uwyn.drone.core.Server;

public class ServerConnectionErrorException extends CoreException
{
	private Server	mServer = null;
	
	public ServerConnectionErrorException(Server server, Throwable cause)
	{
		super("Unable to connect to the server '"+server+"'.", cause);
		
		mServer = server;
	}
	
	public Server getServer()
	{
		return mServer;
	}
}

