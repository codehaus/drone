/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.exceptions;

import com.uwyn.rife.tools.StringUtils;

public class UnknownServerAddressException extends CoreException
{
	private byte[] mAddress = null;
	
	public UnknownServerAddressException(byte[] address, Throwable cause)
	{
		super("The server address '"+StringUtils.join(address, ".")+"' couldn't be found.", cause);
		
		mAddress = address;
	}
	
	public byte[] getAddress()
	{
		return mAddress;
	}
}

