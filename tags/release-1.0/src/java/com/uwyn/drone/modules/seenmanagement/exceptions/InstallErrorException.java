/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.seenmanagement.exceptions;

import com.uwyn.drone.modules.exceptions.SeenManagerException;
import com.uwyn.rife.database.exceptions.DatabaseException;

public class InstallErrorException extends SeenManagerException
{
	public InstallErrorException()
	{
		this(null);
	}

	public InstallErrorException(DatabaseException cause)
	{
		super("Can't install the seen database structure.", cause);
	}
}

