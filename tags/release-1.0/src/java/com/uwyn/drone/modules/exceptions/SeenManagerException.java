/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.exceptions;

public class SeenManagerException extends Exception
{
	public SeenManagerException(String message)
	{
		super(message);
	}

	public SeenManagerException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public SeenManagerException(Throwable cause)
	{
		super(cause);
	}
}

