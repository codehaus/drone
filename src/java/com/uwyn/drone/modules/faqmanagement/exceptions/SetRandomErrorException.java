/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.faqmanagement.exceptions;

import com.uwyn.drone.modules.exceptions.FaqManagerException;
import com.uwyn.rife.database.exceptions.DatabaseException;

public class SetRandomErrorException extends FaqManagerException
{
	private String	mName = null;
	private boolean	mState = false;
	
	public SetRandomErrorException(String name, boolean state)
	{
		this(name, state, null);
	}

	public SetRandomErrorException(String name, boolean state, DatabaseException cause)
	{
		super("Error while setting the random state of faq '"+name+"' to '"+cause+"'.", cause);
		
		mName = name;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public boolean getState()
	{
		return mState;
	}
}

