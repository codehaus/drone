/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.webui.elements.admin.modules.log;

import com.uwyn.drone.modules.exceptions.LogManagerException;
import com.uwyn.drone.modules.logmanagement.DatabaseLogs;
import com.uwyn.drone.modules.logmanagement.DatabaseLogsFactory;
import com.uwyn.rife.engine.Element;
import com.uwyn.rife.site.ValidationBuilderXhtml;
import com.uwyn.rife.template.Template;
import com.uwyn.rife.tools.ExceptionUtils;
import java.util.logging.Logger;

public class Remove extends Element
{
	private Template mTemplate = null;
	
	public void initialize()
	{
		mTemplate = getHtmlTemplate("admin.modules.log.remove");
	}
	
	public void processElement()
	{
		print(mTemplate);
	}
	
	public void doConfirm()
	{
		DatabaseLogs	database_log = DatabaseLogsFactory.get();
		try
		{
			database_log.remove();
			mTemplate.setBlock("authenticated_content", "removed_content");
		}
		catch (LogManagerException e)
		{
			ValidationBuilderXhtml builder = new ValidationBuilderXhtml();
			builder.setFallbackErrorArea(mTemplate, e.getCause().getMessage());
			Logger.getLogger("com.uwyn.drone.webui").severe(ExceptionUtils.getExceptionStackTrace(e));
		}

		print(mTemplate);
	}
}

