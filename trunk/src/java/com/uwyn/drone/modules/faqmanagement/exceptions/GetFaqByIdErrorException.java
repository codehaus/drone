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

public class GetFaqByIdErrorException extends FaqManagerException
{
	private int	mId = -1;

	public GetFaqByIdErrorException(int id)
	{
		this(id, null);
	}

	public GetFaqByIdErrorException(int id, DatabaseException cause)
	{
		super("Unable to get the faq with id '"+id+"'.", cause);
		mId = id;
	}

	public int getId()
	{
		return mId;
	}
}

