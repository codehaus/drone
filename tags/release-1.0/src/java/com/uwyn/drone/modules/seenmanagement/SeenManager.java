/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.seenmanagement;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.modules.exceptions.SeenManagerException;
import com.uwyn.drone.protocol.ServerMessage;

public interface SeenManager
{
	public void recordSeen(Bot bot, Channel channel, SeenData seenData) throws SeenManagerException;
	public SeenData getSeen(Bot bot, Channel channel, String nickname) throws SeenManagerException;
}
