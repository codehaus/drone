/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.exceptions;

public class FaqManagerException extends Exception
{
	public FaqManagerException(String message)
	{
		super(message);
	}

	public FaqManagerException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public FaqManagerException(Throwable cause)
	{
		super(cause);
	}
}

