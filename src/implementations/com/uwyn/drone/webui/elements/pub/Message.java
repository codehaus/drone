/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.webui.elements.pub;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.BotFactory;
import com.uwyn.drone.core.BotsRunner;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.protocol.commands.Privmsg;
import com.uwyn.rife.engine.Element;
import com.uwyn.rife.engine.exceptions.EngineException;
import com.uwyn.rife.site.ConstrainedProperty;
import com.uwyn.rife.site.Validation;
import com.uwyn.rife.template.Template;
import java.util.Collection;
import java.util.Iterator;

public class Message extends Element
{
	private Template	mTemplate = null;
	
	public void initialize()
	{
		mTemplate = getHtmlTemplate("pub.message");
	}
	
	public void processElement()
	{
		Collection	bots = BotsRunner.getRepInstance().getBots();
		Iterator	bots_it = bots.iterator();
		Bot			bot = null;
		while (bots_it.hasNext())
		{
			bot = (Bot)bots_it.next();
			
			mTemplate.setValue("bot", encodeHtml(bot.getName()));
			mTemplate.appendBlock("options", "option");
		}

		print(mTemplate);
	}
	
	public void doSendMessage()
	{
		Submission	submission = (Submission)getSubmissionBean("sendMessage", Submission.class);
		if (!submission.validate())
		{
			generateForm(mTemplate, submission);
			processElement();
			return;
		}
		
		Bot			bot = BotFactory.get(submission.getBot());
		Privmsg		msg = new Privmsg(submission.getTarget(), submission.getMessage());
		
		try
		{
			bot.send(msg);
		}
		catch (CoreException e)
		{
			throw new EngineException(e);
		}
		
		mTemplate.setBlock("page_content", "page_content_executed");
		print(mTemplate);
	}
	
	public static class Submission extends Validation
	{
		private String	mBot = null;
		private String	mTarget = null;
		private String	mMessage = null;
		
		public Submission()
		{
		}
		
		public void activateValidation()
		{
			addConstraint(new ConstrainedProperty("bot").notNull(true).notEmpty(true));
			addConstraint(new ConstrainedProperty("target").notNull(true).notEmpty(true));
			addConstraint(new ConstrainedProperty("message").notNull(true).notEmpty(true));
		}
		
		public void setBot(String bot)
		{
			mBot = bot;
		}
		
		public String getBot()
		{
			return mBot;
		}
		
		public void setTarget(String target)
		{
			mTarget = target;
		}
		
		public String getTarget()
		{
			return mTarget;
		}
		
		public void setMessage(String message)
		{
			mMessage = message;
		}
		
		public String getMessage()
		{
			return mMessage;
		}
	}
}

