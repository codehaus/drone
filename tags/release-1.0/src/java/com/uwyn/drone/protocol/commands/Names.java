/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.protocol.commands;

public class Names implements IrcCommand
{
	private String	mCommand = null;
	
	public Names(String channelName)
	{
		if (null == channelName)		throw new IllegalArgumentException("channelName can't be null.");
		if (0 == channelName.length())	throw new IllegalArgumentException("channelName can't be empty.");
		
		StringBuffer command = new StringBuffer("NAMES ");
		command.append(channelName);
		
		mCommand = command.toString();
	}
	
	public String getCommand()
	{
		return mCommand;
	}
}
