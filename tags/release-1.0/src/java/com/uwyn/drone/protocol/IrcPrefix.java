/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.protocol;

import com.uwyn.rife.tools.StringUtils;
import java.util.ArrayList;
import java.util.Collection;

public class IrcPrefix
{
	private	String	mServerName = null;
	private	String	mNickName = null;
	private	String	mUser = null;
	private	String	mHost = null;
	private	String	mRaw = null;
	private String	mString = null;
	
	public IrcPrefix(String serverName)
	{
		mServerName = serverName;
	}
	
	public IrcPrefix(String nickName, String user, String host)
	{
		mNickName = nickName;
		mUser = user;
		mHost = host;
	}
	
	public static IrcPrefix parse(String raw)
	{
		IrcPrefix irc_prefix = null;
		
		if (raw.startsWith(":"))
		{
			int user_index = raw.indexOf("!");
			
			if (user_index != -1)
			{
				String nick_name = raw.substring(1, user_index);
				String user = null;
				String host = null;
				
				int host_index = raw.indexOf("@", user_index);
				if (host_index != -1)
				{
					user = raw.substring(user_index+1, host_index);
					host = raw.substring(host_index+1);
				}
				else
				{
					user = raw.substring(user_index+1);
				}
				
				irc_prefix = new IrcPrefix(nick_name, user, host);
				irc_prefix.setRaw(raw);
			}
			else
			{
				irc_prefix = new IrcPrefix(raw.substring(1));
				irc_prefix.setRaw(raw);
			}
		}
		
		return irc_prefix;
	}
	
	private void setRaw(String raw)
	{
		assert raw != null;
		assert raw.length() > 0;
		
		mRaw = raw;
	}
	
	public String getServerName()
	{
		return mServerName;
	}
	
	public String getNickName()
	{
		return mNickName;
	}
		
	public String getUser()
	{
		return mUser;
	}
	
	public String getHost()
	{
		return mHost;
	}
	
	public String getRaw()
	{
		return mRaw;
	}
	
	public String toString()
	{
		if (null == mString)
		{
			StringBuffer result = new StringBuffer(":");
			if (mNickName != null)
			{
				result.append(mNickName);
				if (mUser != null)
				{
					result.append("!");
					result.append(mUser);
					if (mHost != null)
					{
						result.append("@");
						result.append(mHost);
					}
				}
				mRaw = result.toString();
			}
			else
			{
				result.append(mServerName);
			}
			mString = result.toString();
		}
		return mString;
	}
}
