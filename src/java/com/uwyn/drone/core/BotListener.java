/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

public interface BotListener
{
	public void loggedOn(Bot bot);
	public void loggedOff(Bot bot);
	public void nickChanged(Bot bot);
	public void nickInUse(Bot bot, String nick);
	public void connectionError(Bot bot, Throwable e);
}
