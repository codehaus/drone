/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules;

import com.uwyn.drone.core.AbstractModule;
import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.protocol.commands.Privmsg;

public class Identify extends AbstractModule
{
	private static final String[]	MESSAGE_COMMANDS = new String[] {"identify"};
	
	private String mNick = null;
	private String mPassword = null;
	
	public void setNick(String nick)
	{
		mNick = nick;
	}
	
	public String getNick()
	{
		return mNick;
	}
	
	public void setPassword(String password)
	{
		mPassword = password;
	}
	
	public String getPassword()
	{
		return mPassword;
	}
	
	public String[] getMessageCommands()
	{
		return MESSAGE_COMMANDS;
	}
	
	public void loggedon(Bot bot)
	throws CoreException
	{
		bot.send(new Privmsg(mNick, "identify "+mPassword));
	}
}
