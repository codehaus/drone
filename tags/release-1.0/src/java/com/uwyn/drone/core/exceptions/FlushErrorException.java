/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.exceptions;

import com.uwyn.drone.core.Server;

public class FlushErrorException extends CoreException
{
	private Server	mServer = null;
	
	public FlushErrorException(Server server, Throwable cause)
	{
		super("Unexpected error while flushing the output to the server '"+server+"'.", cause);
		
		mServer = server;
	}
	
	public Server getServer()
	{
		return mServer;
	}
}

