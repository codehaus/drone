/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.exceptions;

import com.uwyn.drone.core.Server;

public class NoValidAddressException extends CoreException
{
	private Server	mServer = null;
	
	public NoValidAddressException(Server server)
	{
		super("The server '"+server+"' couldn't connect to any of the addresses it has.");
		
		mServer = server;
	}
	
	public Server getServer()
	{
		return mServer;
	}
}

