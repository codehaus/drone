/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.protocol.commands;

public class Nick implements IrcCommand
{
	private String	mCommand = null;
	
	public Nick(String nickName)
	{
		if (null == nickName)		throw new IllegalArgumentException("nickName can't be null.");
		if (0 == nickName.length())	throw new IllegalArgumentException("nickName can't be empty.");
		
		StringBuffer command = new StringBuffer("NICK ");
		command.append(nickName);
		
		mCommand = command.toString();
	}
	
	public String getCommand()
	{
		return mCommand;
	}
}
