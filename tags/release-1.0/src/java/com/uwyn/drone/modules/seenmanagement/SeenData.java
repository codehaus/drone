/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.seenmanagement;

import com.uwyn.rife.site.ConstrainedProperty;
import com.uwyn.rife.site.Validation;
import java.sql.Timestamp;

public class SeenData extends Validation
{
	private static final String	IRC_ACTION = "\u0001ACTION";

    private String		mNickname = null;
	private Timestamp	mMoment = null;
    private String		mMessage = null;
    private String		mRaw = null;

    public SeenData()
    {
		super();
		
		init();
	}
	
    public SeenData(String nickname, Timestamp moment, String message, String raw)
    {
		super();
		
		setNickname(nickname);
		setMoment(moment);
		setMessage(message);
		setRaw(raw);
		
		init();
	}
	
	private void init()
	{
		addConstraint(new ConstrainedProperty("nickname").notNull(true).notEmpty(true).maxLength(30));
		addConstraint(new ConstrainedProperty("moment").notNull(true));
		addConstraint(new ConstrainedProperty("message").notNull(true));
		addConstraint(new ConstrainedProperty("raw").notNull(true));
    }
	
	public String toString()
	{
		return ""+mNickname+","+mMoment+","+mMessage;
	}
	
    public String getNickname()
    {
        return mNickname;
    }

    public void setNickname(String nickname)
    {
        mNickname = nickname;
    }
	
	public void setMoment(Timestamp moment)
	{
		mMoment = moment;
	}
	
	public Timestamp getMoment()
	{
		return mMoment;
	}
	
    public String getMessage()
    {
        return mMessage;
    }
	
    public String getDisplayMessage()
    {
		if (null == mMessage)
		{
			return null;
		}
		
		// translate the \u0001ACTION command which corresponds to
		// /me so that the user's nickname is used instead
		if (mMessage.startsWith(IRC_ACTION))
		{
			return mNickname+mMessage.substring(IRC_ACTION.length());
		}
		else
		{
			return mMessage;
		}
    }
	
    public void setMessage(String moment)
    {
        mMessage = moment;
    }
	
	public void setRaw(String raw)
	{
		mRaw = raw;
	}
	
	public String getRaw()
	{
		return mRaw;
	}
}
