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

public class RemoveFaqErrorException extends FaqManagerException
{
	private String	mName = null;
	
	public RemoveFaqErrorException(String name)
	{
		this(name, null);
	}

	public RemoveFaqErrorException(String name, DatabaseException cause)
	{
		super("Error while removing faq with name '"+name+"'.", cause);
		
		mName = name;
	}
	
	public String getName()
	{
		return mName;
	}
}

