/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.exceptions;

public class CoreException extends Exception
{
	public CoreException(String message)
	{
		super(message);
	}

	public CoreException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public CoreException(Throwable cause)
	{
		super(cause);
	}
}

