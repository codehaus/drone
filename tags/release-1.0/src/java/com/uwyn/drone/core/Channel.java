/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import com.uwyn.drone.core.exceptions.ChannelMessageErrorException;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.core.exceptions.JoinErrorException;
import com.uwyn.drone.core.exceptions.LeaveErrorException;
import com.uwyn.drone.protocol.commands.Join;
import com.uwyn.drone.protocol.commands.Part;
import com.uwyn.drone.protocol.commands.Privmsg;

public class Channel
{
	private String	mName = null;
	private Server	mServer = null;
	
	Channel(String name, Server server)
	{
		assert name != null;
		assert name.length() > 0;
		assert server != null;
		
		mName = name.toLowerCase();
		mServer = server;
	}
	
	public boolean join()
	throws CoreException
	{
		// only join if the server is connected
		if (!mServer.isConnected())
		{
			return false;
		}
		
		try
		{
			synchronized (mServer)
			{
				mServer.send(new Join(mName));
			}
		}
		catch (CoreException e)
		{
			throw new JoinErrorException(this, e);
		}
		return true;
	}

	public boolean leave()
	throws CoreException
	{
		// only leave if the server is connected
		if (!mServer.isConnected())
		{
			return false;
		}
		
		try
		{
			synchronized (mServer)
			{
				mServer.send(new Part(mName));
			}
		}
		catch (CoreException e)
		{
			throw new LeaveErrorException(this, e);
		}
		
		return true;
	}
	
	public boolean send(String message)
	throws CoreException
	{
		if (null == message)		throw new IllegalArgumentException("message can't be null.");
		if (0 == message.length())	throw new IllegalArgumentException("message can't be empty.");
		
		try
		{
			synchronized (mServer)
			{
				mServer.send(new Privmsg(mName, message));
			}
		}
		catch (CoreException e)
		{
			throw new ChannelMessageErrorException(this, message, e);
		}
		
		return true;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public Server getServer()
	{
		return mServer;
	}
	
	public boolean equals(Object other)
	{
		if (other instanceof Channel)
		{
			Channel other_channel = (Channel)other;
			if (null != other_channel &&
                other_channel.getName().equals(this.getName()) &&
                other_channel.getServer().equals(this.getServer()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public int hashCode()
	{
		return mName.hashCode()*mServer.hashCode();
	}
	
	public String toString()
	{
		return mName;
	}
}
