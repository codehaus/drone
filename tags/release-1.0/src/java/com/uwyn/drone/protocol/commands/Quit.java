/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.protocol.commands;

public class Quit implements IrcCommand
{
	private String	mCommand = null;
	
	public Quit(String message)
	{
		if (null == message)		throw new IllegalArgumentException("message can't be null.");
		if (0 == message.length())	throw new IllegalArgumentException("message can't be empty.");
		
		StringBuffer command = new StringBuffer("QUIT :");
		command.append(message);
		
		mCommand = command.toString();
	}
	
	public String getCommand()
	{
		return mCommand;
	}
}
