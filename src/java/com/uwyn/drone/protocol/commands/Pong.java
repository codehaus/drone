/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.protocol.commands;

public class Pong implements IrcCommand
{
	private String	mCommand = null;
	
	public Pong(String identifier)
	{
		if (null == identifier)			throw new IllegalArgumentException("identifier can't be null.");
		if (0 == identifier.length())	throw new IllegalArgumentException("identifier can't be empty.");
		
		StringBuffer command = new StringBuffer("PONG :");
		command.append(identifier);
		
		mCommand = command.toString();
	}
	
	public String getCommand()
	{
		return mCommand;
	}
}
