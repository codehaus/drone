/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.webui.elements.admin.modules.seen;

import com.uwyn.drone.modules.exceptions.SeenManagerException;
import com.uwyn.drone.modules.seenmanagement.DatabaseSeen;
import com.uwyn.drone.modules.seenmanagement.DatabaseSeenFactory;
import com.uwyn.rife.engine.Element;
import com.uwyn.rife.site.ValidationBuilderXhtml;
import com.uwyn.rife.template.Template;
import com.uwyn.rife.tools.ExceptionUtils;
import java.util.logging.Logger;

public class Install extends Element
{
	private Template mTemplate = null;
	
	public void initialize()
	{
		mTemplate = getHtmlTemplate("admin.modules.seen.install");
	}
	
	public void processElement()
	{
		print(mTemplate);
	}
	
	public void doConfirm()
	{
		DatabaseSeen	database_seen = DatabaseSeenFactory.get();
		try
		{
			database_seen.install();
			mTemplate.setBlock("authenticated_content", "installed_content");
		}
		catch (SeenManagerException e)
		{
			ValidationBuilderXhtml builder = new ValidationBuilderXhtml();
			builder.setFallbackErrorArea(mTemplate, e.getCause().getMessage());
			Logger.getLogger("com.uwyn.drone.webui").severe(ExceptionUtils.getExceptionStackTrace(e));
		}

		print(mTemplate);
	}
}

