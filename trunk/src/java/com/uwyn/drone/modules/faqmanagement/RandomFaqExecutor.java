/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.faqmanagement;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.BotFactory;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.modules.exceptions.FaqManagerException;
import com.uwyn.rife.scheduler.Executor;
import com.uwyn.rife.scheduler.Task;
import com.uwyn.rife.scheduler.exceptions.SchedulerException;
import com.uwyn.rife.tools.ExceptionUtils;
import java.util.Iterator;
import java.util.logging.Logger;

public class RandomFaqExecutor extends Executor
{
	protected boolean executeTask(Task task)
	{
		String	bot_name = null;
		try
		{
			bot_name = task.getTaskoptionValue("bot");
			
			Bot bot = BotFactory.get(bot_name);
			if (bot != null)
			{
				DatabaseFaqs	database_faq = DatabaseFaqsFactory.get();
				FaqData			faq_data = database_faq.getRandomFaq(bot);
				if (faq_data != null)
				{
					Iterator	channels_it = bot.getJoinedChannels().iterator();
					Channel		channel = null;
					while (channels_it.hasNext())
					{
						channel = (Channel)channels_it.next();
						channel.send(faq_data.getName()+": "+faq_data.getAnswer());
					}
				}
			}
		}
		catch (CoreException e)
		{
			Logger.getLogger("com.uwyn.drone.modules").severe(ExceptionUtils.getExceptionStackTrace(e));
		}
		catch (FaqManagerException e)
		{
			Logger.getLogger("com.uwyn.drone.modules").severe(ExceptionUtils.getExceptionStackTrace(e));
		}
		catch (SchedulerException e)
		{
			Logger.getLogger("com.uwyn.drone.modules").severe(ExceptionUtils.getExceptionStackTrace(e));
		}
		
		return true;
	}
	
	public String getHandledTasktype()
	{
		return "RandomFaq";
	}
}
