/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.webui.elements.admin;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.BotListener;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.rife.engine.Element;
import com.uwyn.rife.site.FormBuilder;
import com.uwyn.rife.site.ValidationBuilder;
import com.uwyn.rife.site.ValidationError;
import com.uwyn.rife.template.Template;
import java.util.ArrayList;

public class ChangeNick extends Element implements BotListener
{
	public void processElement()
	{
		Template			t = getHtmlTemplate("admin.change_nick");
		FormBuilder			form_builder = t.getBeanHandler().getFormBuilder();
		ValidationBuilder	validation_builder = form_builder.getValidationBuilder();
		
		form_builder.generateField(t, "new_nick", new String[] {getParameter("new_nick")}, null);
		
		// validate inputs
		ArrayList	errors = new ArrayList();
		String		botname = null;
		if (hasSubmission("perform_change"))
		{
			botname = getParameter("bot_to_change");
			if (isParameterEmpty("new_nick"))
			{
				errors.add(new ValidationError.MANDATORY("nick"));
			}
		}
		else
		{
			botname = getInput("botname");
		}
		Bot bot = Home.validateBotName(errors, botname);
		
		// act according to the actions that were performed
		if (errors.size() > 0)
		{
			if (botname != null)
			{
				t.setValue("botname", encodeHtml(botname));
			}
			validation_builder.generateValidationErrors(t, errors, null);
			validation_builder.generateErrorMarkings(t, errors, null);
		}
		else
		{
			if (hasSubmission("perform_change"))
			{
				String new_nick = getParameter("new_nick");
				
				t.setValue("botname", encodeHtml(bot.getName()));
				t.setValue("nick", encodeHtml(new_nick));

				try
				{
					bot.addBotListener(this);
					bot.changeNick(new_nick);
					synchronized (this)
					{
						if (!bot.getConnectedNick().equals(new_nick))
						{
							try
							{
								this.wait();
							}
							catch (InterruptedException e)
							{
								// do nothing
							}
						}
					}
					bot.removeBotListener(this);
					
					if (bot.getConnectedNick().equals(new_nick))
					{
						exit("nick_changed");
					}
					validation_builder.setFallbackErrorArea(t, t.getBlock("ERROR_CHANGING_NICK"));
				}
				catch (CoreException e)
				{
					validation_builder.setFallbackErrorArea(t, t.getBlock("ERROR_CHANGING_NICK"));
				}
			}
		}
			
		setSubmissionForm(t, "perform_change", new String[] {"bot_to_change", botname});
		
		print(t);
	}
	
	public void loggedOff(Bot bot)
	{
	}
	
	public void loggedOn(Bot bot)
	{
	}
	
	public void nickChanged(Bot bot)
	{
		synchronized (this)
		{
			this.notifyAll();
		}
	}
	
	public void nickInUse(Bot bot, String nick)
	{
		synchronized (this)
		{
			this.notifyAll();
		}
	}
	
	public void connectionError(Bot bot, Throwable e)
	{
	}
}

