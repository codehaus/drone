/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.exceptions;

public class LogManagerException extends Exception
{
	public LogManagerException(String message)
	{
		super(message);
	}

	public LogManagerException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public LogManagerException(Throwable cause)
	{
		super(cause);
	}
}

