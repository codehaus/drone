/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import com.uwyn.drone.core.exceptions.CoreException;

public interface ServerListener
{
	public void connected(Server server) throws CoreException;
	public void disconnected(Server server) throws CoreException;
}
