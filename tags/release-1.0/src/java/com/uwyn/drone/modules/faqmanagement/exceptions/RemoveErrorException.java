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

public class RemoveErrorException extends FaqManagerException
{
	public RemoveErrorException()
	{
		this(null);
	}

	public RemoveErrorException(DatabaseException cause)
	{
		super("Can't remove the faq database structure.", cause);
	}
}

