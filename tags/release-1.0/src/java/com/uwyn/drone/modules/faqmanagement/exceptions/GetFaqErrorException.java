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

public class GetFaqErrorException extends FaqManagerException
{
	private String	mName = null;

	public GetFaqErrorException(String name)
	{
		this(name, null);
	}

	public GetFaqErrorException(String name, DatabaseException cause)
	{
		super("Unable to get the faq with name '"+name+"'.", cause);
		mName = name;
	}

	public String getName()
	{
		return mName;
	}
}

