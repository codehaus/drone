/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.faqmanagement.exceptions;

import com.uwyn.drone.modules.exceptions.FaqManagerException;
import com.uwyn.drone.modules.faqmanagement.FaqData;
import com.uwyn.rife.database.exceptions.DatabaseException;

public class AddFaqErrorException extends FaqManagerException
{
	private FaqData	mFaqData = null;
	
	public AddFaqErrorException(FaqData faqData)
	{
		this(faqData, null);
	}

	public AddFaqErrorException(FaqData faqData, DatabaseException cause)
	{
		super("Error while adding faq with name '"+faqData.getName()+"' and answer '"+faqData.getAnswer()+"'.", cause);
		
		mFaqData = faqData;
	}
	
	public FaqData getFaqData()
	{
		return mFaqData;
	}
}

