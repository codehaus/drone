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

public class ServerMessage
{
	private IrcPrefix		mPrefix = null;
	private ResponseCode	mResponseCode = null;
	private String			mCommand = null;
	private ArrayList		mParameters = null;
	private String			mTrailing = null;
	private	String			mRaw = null;
	private String			mString = null;
	
	protected ServerMessage(IrcPrefix prefix, ResponseCode responseCode, ArrayList parameters, String trailing)
	{
		mPrefix = prefix;
		mResponseCode = responseCode;
		mParameters = parameters;
		mTrailing = trailing;
	}
	
	protected ServerMessage(IrcPrefix prefix, String command, ArrayList parameters, String trailing)
	{
		mPrefix = prefix;
		mCommand = command;
		mParameters = parameters;
		mTrailing = trailing;
	}
	
	public static ServerMessage parse(String raw)
	{
		if (null == raw)
		{
			return null;
		}
		
		String	raw_trimmed = raw.trim();
		int		space_index = raw_trimmed.indexOf(" ", 1); // skip to space first to jump over ipv6 inet addresses
		int		trailing_index = raw_trimmed.indexOf(":", space_index);
		String	trailing = null;
		if (trailing_index > 0)
		{
			trailing = raw_trimmed.substring(trailing_index+1);
			raw_trimmed = raw_trimmed.substring(0, trailing_index);
		}
		
		ServerMessage	irc_message = null;
		ArrayList		message_parts = StringUtils.split(raw_trimmed, " ");
		if (message_parts.size() > 0)
		{
			IrcPrefix		prefix = null;
			ResponseCode	response_code = null;
			String			command = null;
			ArrayList		parameters = null;
			
			if (((String)message_parts.get(0)).startsWith(":"))
			{
				prefix = IrcPrefix.parse((String)message_parts.remove(0));
			}
			command = (String)message_parts.remove(0);
			response_code = ResponseCode.get(command);
			parameters = message_parts;
			
			if (null == response_code)
			{
				irc_message = new ServerMessage(prefix, command, parameters, trailing);
			}
			else
			{
				irc_message = new ServerMessage(prefix, response_code, parameters, trailing);
			}
			
			if (irc_message != null)
			{
				irc_message.setRaw(raw);
			}
		}
		
		return irc_message;
	}
	
	private void setRaw(String raw)
	{
		assert raw != null;
		assert raw.length() > 0;
		
		mRaw = raw;
	}
	
	public boolean isCommand()
	{
		return mCommand != null;
	}
	
	public boolean isResponse()
	{
		return mResponseCode != null;
	}
	
	public IrcPrefix getPrefix()
	{
		return mPrefix;
	}
	
	public String getCommand()
	{
		return mCommand;
	}
	
	public ResponseCode getResponseCode()
	{
		return mResponseCode;
	}
	
	public ArrayList getParameters()
	{
		return mParameters;
	}
	
	public String getTrailing()
	{
		return mTrailing;
	}
	
	public String getRaw()
	{
		return mRaw;
	}
	
	public String toString()
	{
		if (null == mString)
		{
			StringBuffer raw = new StringBuffer();
			if (mPrefix != null)
			{
				raw.append(mPrefix.toString());
				raw.append(" ");
			}
			if (null == mResponseCode)
			{
				raw.append(mCommand);
			}
			else
			{
				raw.append(mResponseCode.toString());
			}
			if (mParameters != null)
			{
				raw.append(" ");
				raw.append(StringUtils.join(mParameters, " "));
			}
			mString = raw.toString();
		}
		
		return mString;
	}
}
