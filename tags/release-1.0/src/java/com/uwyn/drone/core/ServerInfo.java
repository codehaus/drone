/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.core.exceptions.UnknownServerAddressException;
import com.uwyn.drone.core.exceptions.UnknownServerHostnameException;
import com.uwyn.rife.tools.StringUtils;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

class ServerInfo
{
	private static final int			DEFAULT_MAX = 1024;
	private static final int			DEFAULT_AMOUNT = 25;
	private static final int			DEFAULT_INTERVAL = 1000;
	
	private ArrayList	mAddresses = null;
	private int			mMax = 0;
	private int			mAmount = 0;
	private int			mInterval = 0;

	ServerInfo()
	{
		mAddresses = new ArrayList();
		mMax = DEFAULT_MAX;
		mAmount = DEFAULT_AMOUNT;
		mInterval = DEFAULT_INTERVAL;
	}
	
	Collection getAddresses()
	{
		return mAddresses;
	}
	
	int getMax()
	{
		return mMax;
	}
	
	int getAmount()
	{
		return mAmount;
	}
	
	int getInterval()
	{
		return mInterval;
	}
	
	void setOutput(int max, int amount, int interval)
	{
		if (max <= 0)		throw new IllegalArgumentException("max has to be bigger than 0.");
		if (max <= amount)	throw new IllegalArgumentException("max has to be bigger than amount.");
		if (amount <= 0)	throw new IllegalArgumentException("amount has to be bigger than 0.");
		if (interval <= 0)	throw new IllegalArgumentException("interval has to be bigger than 0.");
			
		mMax = max;
		mAmount = amount;
		mInterval = interval;
	}

	void addAddress(String host, int port)
	throws CoreException
	{
		if (null == host)		throw new IllegalArgumentException("host can't be null.");
		if (0 == host.length())	throw new IllegalArgumentException("host can't be empty.");
		
		// detect ip addresses
		byte[] parts = StringUtils.splitToByteArray(host, ".");
		if (4 == parts.length)
		{
			addAddress(parts, port);
			return;
		}

		// no ip address, thus a regular hostname
		InetAddress address = null;
		
		try
		{
			address = InetAddress.getByName(host);
		}
		catch (UnknownHostException e)
		{
			throw new UnknownServerHostnameException(host, e);
		}

		addAddress(address, port);
	}

	void addAddress(byte[] ip, int port)
	throws CoreException
	{
		if (null == ip)	throw new IllegalArgumentException("ip can't be null.");

		InetAddress address = null;
		
		try
		{
			address = InetAddress.getByAddress(ip);
		}
		catch (UnknownHostException e)
		{
			throw new UnknownServerAddressException(ip, e);
		}
		
		addAddress(address, port);
	}
	
	synchronized void addAddress(InetAddress address, int port)
	{
		assert address != null;
		assert port >= 0;
		
		mAddresses.add(new InetSocketAddress(address, port));
	}
}
