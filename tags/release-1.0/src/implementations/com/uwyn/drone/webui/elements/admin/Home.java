/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.webui.elements.admin;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.BotFactory;
import com.uwyn.drone.core.BotListener;
import com.uwyn.drone.core.BotsRunner;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.rife.engine.Element;
import com.uwyn.rife.rep.Rep;
import com.uwyn.rife.site.ValidationBuilderXhtml;
import com.uwyn.rife.site.ValidationError;
import com.uwyn.rife.template.Template;
import com.uwyn.rife.tools.ExceptionUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Home extends Element implements BotListener
{
	private Template	mTemplate = null;
	
	static Bot validateBotName(ArrayList errors, String botname)
	{
		if (null == botname)
		{
			errors.add(new ValidationError.MANDATORY("botname"));
		}
		else if (!BotFactory.contains(botname))
		{
			errors.add(new ValidationError.INVALID("botname"));
		}
		else
		{
			return BotFactory.get(botname);
		}
		
		return null;
	}
	
	public void initialize()
	{
		mTemplate = getHtmlTemplate("admin.home");
	}
	
	public void processElement()
	{
		// output the bot list and the possible actions
		generateBotsList();
		
		print(mTemplate);
	}
	
	public void doPause()
	{
		ValidationBuilderXhtml builder = new ValidationBuilderXhtml();
		
		ArrayList	errors = new ArrayList();
		String		botname = getParameter("bot_to_pause");
		Bot			bot = Home.validateBotName(errors, botname);
		if (null == bot)
		{
			if (botname != null)
			{
				mTemplate.setValue("botname", encodeHtml(botname));
			}
			builder.generateValidationErrors(mTemplate, errors, null);
			processElement();
			return;
		}
		
		try
		{
			bot.pause();
		}
		catch (CoreException e)
		{
			builder.setFallbackErrorArea(mTemplate, "Error while pausing the bot '"+encodeHtml(bot.getName())+"' : "+ExceptionUtils.getExceptionStackTrace(e));
		}
		
		processElement();
	}
	
	public void doResume()
	{
		ValidationBuilderXhtml builder = new ValidationBuilderXhtml();
		
		ArrayList	errors = new ArrayList();
		String		botname = getParameter("bot_to_resume");
		Bot			bot = Home.validateBotName(errors, botname);
		if (null == bot)
		{
			if (botname != null)
			{
				mTemplate.setValue("botname", encodeHtml(botname));
			}
			builder.generateValidationErrors(mTemplate, errors, null);
			processElement();
			return;
		}

		try
		{
			bot.resume();
		}
		catch (CoreException e)
		{
			builder.setFallbackErrorArea(mTemplate, "Error while resuming the bot '"+encodeHtml(bot.getName())+"' : "+ExceptionUtils.getExceptionStackTrace(e));
		}
		
		processElement();
	}
	
	public void doReconnect()
	{
		ValidationBuilderXhtml builder = new ValidationBuilderXhtml();
		
		ArrayList	errors = new ArrayList();
		String		botname = getParameter("bot_to_reconnect");
		Bot			bot = Home.validateBotName(errors, botname);
		if (null == bot)
		{
			if (botname != null)
			{
				mTemplate.setValue("botname", encodeHtml(botname));
			}
			builder.generateValidationErrors(mTemplate, errors, null);
			processElement();
			return;
		}

		try
		{
			bot.addBotListener(this);
			synchronized (this)
			{
				bot.redoLogon();
				
				try
				{
					this.wait();
				}
				catch (InterruptedException e)
				{
					// do nothing
				}
			}
			bot.removeBotListener(this);
		}
		catch (CoreException e)
		{
			builder.setFallbackErrorArea(mTemplate, "Error while reconnecting the bot '"+encodeHtml(bot.getName())+"' : "+ExceptionUtils.getExceptionStackTrace(e));
		}
		processElement();
	}
	
	private void generateBotsList()
	{
		Collection	bots = ((BotsRunner)Rep.getParticipant("com.uwyn.drone.core.DroneParticipant").getObject()).getBots();
		Iterator	bots_it = bots.iterator();
		Bot			bot = null;
		while (bots_it.hasNext())
		{
			bot = (Bot)bots_it.next();
			mTemplate.setValue("botname", encodeHtml(bot.getName()));
			mTemplate.setValue("connected_nick", encodeHtml(bot.getConnectedNick()));
			mTemplate.setValue("nicks", encodeHtml(bot.getNick()+", "+bot.getAltNick()));
			if (bot.isPaused())
			{
				setSubmissionForm(mTemplate, "resume", new String[] {"bot_to_resume", bot.getName()});
				mTemplate.setBlock("actions", "action_resume");
			}
			else
			{
				setSubmissionForm(mTemplate, "pause", new String[] {"bot_to_pause", bot.getName()});
				mTemplate.setBlock("actions", "action_pause");
			}
			setExitForm(mTemplate, "change_nick", new String[] {"botname", bot.getName()});
			setSubmissionForm(mTemplate, "reconnect", new String[] {"bot_to_reconnect", bot.getName()});
			mTemplate.appendBlock("actions", "action_reconnect");
			mTemplate.appendBlock("actions", "action_changenick");
			
			mTemplate.appendBlock("bots", "bot");
		}
	}
	
	public void loggedOn(Bot bot)
	{
		synchronized (this)
		{
			this.notifyAll();
		}
	}
	
	public void loggedOff(Bot bot)
	{
	}
	
	public void nickChanged(Bot bot)
	{
	}
	
	public void nickInUse(Bot bot, String nick)
	{
	}
	
	public void connectionError(Bot bot, Throwable e)
	{
	}
}

