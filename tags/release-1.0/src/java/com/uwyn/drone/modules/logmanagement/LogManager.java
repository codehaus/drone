/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.logmanagement;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.modules.exceptions.LogManagerException;
import com.uwyn.drone.protocol.ServerMessage;
import java.util.Calendar;

public interface LogManager
{
	public void addLog(Bot bot, Channel channel, ServerMessage serverMessage) throws LogManagerException;
	public boolean searchLog(LogResultProcessor processor, Bot bot, Channel channel, String search) throws LogManagerException;
	public boolean getLogMessages(LogResultProcessor processor, Bot bot, Channel channel, Calendar day) throws LogManagerException;
}
