/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.exceptions;

import com.uwyn.drone.core.Module;

public class InvalidModuleNameException extends CoreException
{
	private Module	mModule = null;
	
	public InvalidModuleNameException(Module module)
	{
		super("The name of module '"+module.getClass().getName()+"' is not valid.");
		
		mModule = module;
	}
	
	public Module getModule()
	{
		return mModule;
	}
}

