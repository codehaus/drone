/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.protocol.commands;

public class Privmsg implements IrcCommand
{
	private String	mCommand = null;
	
	public Privmsg(String target, String message)
	{
		if (null == target)		throw new IllegalArgumentException("target can't be null.");
		if (0 == target.length())	throw new IllegalArgumentException("target can't be empty.");
		if (null == message)	throw new IllegalArgumentException("message can't be null.");
		
		StringBuffer command = new StringBuffer("PRIVMSG ");
		command.append(target);
		command.append(" :");
		command.append(message);
		
		mCommand = command.toString();
	}
	
	public String getCommand()
	{
		return mCommand;
	}
}
