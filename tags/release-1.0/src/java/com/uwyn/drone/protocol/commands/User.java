/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.protocol.commands;

public class User implements IrcCommand
{
	private String	mCommand = null;
	
	public User(String name, String realName)
	{
		this(name, 0, realName);
	}
	
	public User(String name, int mode, String realName)
	{
		if (null == name)		throw new IllegalArgumentException("name can't be null.");
		if (0 == name.length())	throw new IllegalArgumentException("name can't be empty.");
		if (null == realName)	throw new IllegalArgumentException("realName can't be null.");
		if (mode < 0)			throw new IllegalArgumentException("mode can't be negative.");
		
		StringBuffer command = new StringBuffer("USER ");
		command.append(name);
		command.append(" ");
		command.append(mode);
		command.append(" * :");
		command.append(realName);
		
		mCommand = command.toString();
	}
	
	public String getCommand()
	{
		return mCommand;
	}
}
