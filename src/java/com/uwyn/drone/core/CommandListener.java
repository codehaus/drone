/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.protocol.ServerMessage;

public interface CommandListener
{
	public void receivedCommand(ServerMessage message) throws CoreException;
}
