/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.exceptions;

import com.uwyn.drone.core.Server;

public class MissingAddressException extends CoreException
{
	private Server	mServer = null;
	
	public MissingAddressException(Server server)
	{
		super("The server '"+server+"' doesn't have any addresses defined.");
		
		mServer = server;
	}
	
	public Server getServer()
	{
		return mServer;
	}
}

