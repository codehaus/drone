/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules;

import com.uwyn.drone.core.AbstractModule;
import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.modules.exceptions.FaqManagerException;
import com.uwyn.drone.modules.faqmanagement.DatabaseFaqs;
import com.uwyn.drone.modules.faqmanagement.DatabaseFaqsFactory;
import com.uwyn.drone.modules.faqmanagement.FaqData;
import com.uwyn.drone.protocol.AttributeCode;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.drone.protocol.commands.Privmsg;
import com.uwyn.rife.tools.ExceptionUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Faq extends AbstractModule
{
	private static final String[]	MESSAGE_COMMANDS = new String[] {"addfaq", "delfaq", "getfaq", "editfaq", "randfaq", "randfaqon", "randfaqoff"};
	private static final String[]	CHANNEL_COMMANDS = new String[] {"?"};
	private static final HashMap	HELPMAP = new HashMap();
	
	static
	{
		HELPMAP.put(null,
			AttributeCode.BOLD+"Frequently Asked Questions (FAQ)"+AttributeCode.BOLD+" are a collection of"+AttributeCode.ENDLINE+
			"common questions with their answers. Each question is"+AttributeCode.ENDLINE+
			"identified by a unique key, which is used to retrieve"+AttributeCode.ENDLINE+
			"the answer afterwards or to manipulate an existing faq."+AttributeCode.ENDLINE+
			"For more information on a specific command, type        "+AttributeCode.ENDLINE+
			AttributeCode.BOLD+"/msg $botnick help $modulename <command>"+AttributeCode.BOLD+"."+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			AttributeCode.BOLD+"Channel commands"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			"    !?           retrieves a faq's answer"+AttributeCode.ENDLINE+
			AttributeCode.BOLD+"Privmsg commands"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			"    ADDFAQ       adds a new faq entry"+AttributeCode.ENDLINE+
			"    DELFAQ       removes an existing faq"+AttributeCode.ENDLINE+
			"    EDITFAQ      edits an existing faq entry"+AttributeCode.ENDLINE+
			"    GETFAQ       retrieves a faq's answer"+AttributeCode.ENDLINE+
			"    RANDFAQON    turns on random retrieval"+AttributeCode.ENDLINE+
			"    RANDFAQOFF   turns off random retrieval"+AttributeCode.ENDLINE+
			"    RANDFAQ      retrieves a random faq"+AttributeCode.ENDLINE);
		
		HELPMAP.put("!?",
			"Syntax: "+AttributeCode.BOLD+"!? <name>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Retrieves the answer of a faq. The name is used to"+AttributeCode.ENDLINE+
			"lookup the answer. A warning message will be output"+AttributeCode.ENDLINE+
			"if the faq can't be found."+AttributeCode.ENDLINE);
		
		HELPMAP.put("addfaq",
			"Syntax: "+AttributeCode.BOLD+"ADDFAQ <name>:<answer>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			"        "+AttributeCode.BOLD+"ADDFAQ <name>:0:<answer>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			"        "+AttributeCode.BOLD+"ADDFAQ <name>:1:<answer>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Adds a new faq entry into the database. The question"+AttributeCode.ENDLINE+
			"will be identified by the provided name and can consist"+AttributeCode.ENDLINE+
			"out of any character besides the colon (:). This is used"+AttributeCode.ENDLINE+
			"to indicate the start of the answer which will continue"+AttributeCode.ENDLINE+
			"until the end of the message."+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"If a faq with the same name already exists, you'll be"+AttributeCode.ENDLINE+
			"notified of this and you'll have to use either DELFAQ to"+AttributeCode.ENDLINE+
			"delete the faq entry first or EDITFAQ to modify the"+AttributeCode.ENDLINE+
			"faq's definition."+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"The faq module can optionally be installed with a"+AttributeCode.ENDLINE+
			"scheduler that randomly retrieves faq entries and pastes"+AttributeCode.ENDLINE+
			"them to a channel at certain intervals. Faq entries can"+AttributeCode.ENDLINE+
			"either be enabled or disabled for random retrieval."+AttributeCode.ENDLINE+
			"Adding a 0 or a 1 with a colon (:) after the faq's name,"+AttributeCode.ENDLINE+
			"explicitely disables or enabled random faq retrieval"+AttributeCode.ENDLINE+
			"for that entry"+AttributeCode.ENDLINE);

		HELPMAP.put("getfaq",
			"Syntax: "+AttributeCode.BOLD+"GETFAQ <name>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Retrieves the answer of a faq. The name is used to"+AttributeCode.ENDLINE+
			"lookup the answer. A warning message will be output"+AttributeCode.ENDLINE+
			"if the faq can't be found."+AttributeCode.ENDLINE);

		HELPMAP.put("delfaq",
			"Syntax: "+AttributeCode.BOLD+"DELFAQ <name>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Removes a faq from the database. The name is used to"+AttributeCode.ENDLINE+
			"lookup the faq entry. A warning message will be output"+AttributeCode.ENDLINE+
			"if the faq can't be found."+AttributeCode.ENDLINE);

		HELPMAP.put("editfaq",
			"Syntax: "+AttributeCode.BOLD+"EDITFAQ <name>:<answer>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Edits the answer of an existing faq. The name is used to"+AttributeCode.ENDLINE+
			"lookup the faq entry. A warning message will be output"+AttributeCode.ENDLINE+
			"if the faq can't be found."+AttributeCode.ENDLINE+
			"The start of the answer is marked by the first colon (:)"+AttributeCode.ENDLINE+
			"and it will continue until the end of the message."+AttributeCode.ENDLINE);

		HELPMAP.put("randfaqon",
			"Syntax: "+AttributeCode.BOLD+"RANDFAQON <name>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Turns on random retrieval for a faq entry. The name is"+AttributeCode.ENDLINE+
			"used to lookup the faq entry. A warning message will be"+AttributeCode.ENDLINE+
			"output if the faq can't be found."+AttributeCode.ENDLINE);

		HELPMAP.put("randfaqoff",
			"Syntax: "+AttributeCode.BOLD+"RANDFAQOFF <name>"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Turns off random retrieval for a faq entry. The name is"+AttributeCode.ENDLINE+
			"used to lookup the faq entry. A warning message will be"+AttributeCode.ENDLINE+
			"output if the faq can't be found."+AttributeCode.ENDLINE);

		HELPMAP.put("randfaq",
			"Syntax: "+AttributeCode.BOLD+"RANDFAQ"+AttributeCode.BOLD+""+AttributeCode.ENDLINE+
			" "+AttributeCode.ENDLINE+
			"Simulates random faq retrieval."+AttributeCode.ENDLINE);
	}

	public String getName()
	{
		return "FAQ";
	}
	
	public String getDescription()
	{
		return "Collection of frequently asked questions.";
	}
	
	public Map getHelpMap()
	{
		return HELPMAP;
	}

	public String[] getMessageCommands()
	{
		return MESSAGE_COMMANDS;
	}
	
	public String[] getChannelCommands()
	{
		return CHANNEL_COMMANDS;
	}
	
	public void messageCommand(Bot bot, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
		DatabaseFaqs	database_faq = DatabaseFaqsFactory.get();
		
		try
		{
			if (command.equals("getfaq"))
			{
				String	name = arguments.trim();
				FaqData	faq_data = database_faq.getFaq(bot, name);
				
				if (faq_data != null)
				{
					bot.send(new Privmsg(nick, faq_data.getName()+": "+(faq_data.isRandom() ? "random" : "not random")+": "+faq_data.getAnswer()));
				}
				else
				{
					bot.send(new Privmsg(nick, "Faq '"+name+"' couldn't be found."));
				}
			}
			else if (command.equals("randfaq"))
			{
				FaqData	faq_data = database_faq.getRandomFaq(bot);
				
				if (faq_data != null)
				{
					bot.send(new Privmsg(nick, faq_data.getName()+": "+faq_data.getAnswer()));
				}
				else
				{
					bot.send(new Privmsg(nick, "No random faq could be found."));
				}
			}
			else if (command.equals("randfaqon"))
			{
				String	name = arguments.trim();
				if (database_faq.setRandom(bot, name, true))
				{
					bot.send(new Privmsg(nick, "Random retrieval has been enabled for faq '"+name+"'."));
				}
				else
				{
					bot.send(new Privmsg(nick, "Faq '"+name+"' couldn't be found."));
				}
			}
			else if (command.equals("randfaqoff"))
			{
				String	name = arguments.trim();
				if (database_faq.setRandom(bot, name, false))
				{
					bot.send(new Privmsg(nick, "Random retrieval has been disabled for faq '"+name+"'."));
				}
				else
				{
					bot.send(new Privmsg(nick, "Faq '"+name+"' couldn't be found."));
				}
			}
			else if (command.equals("addfaq"))
			{
				int	first_colon_index = arguments.indexOf(":");
				if (-1 == first_colon_index)
				{
					bot.send(new Privmsg(nick, "Invalid syntax '!"+command+" "+arguments+"'"));
					return;
				}
				else
				{
					int	second_colon_index = arguments.indexOf(":", first_colon_index+1);
					
					String	name = null;
					String	answer = null;
					boolean	random = false;
					
					if (second_colon_index != -1)
					{
						String random_string = arguments.substring(first_colon_index+1, second_colon_index).trim();
						if (1 == random_string.length())
						{
							if ('1' == random_string.charAt(0))
							{
								random = true;
								answer = arguments.substring(second_colon_index+1).trim();
							}
							else if ('0' == random_string.charAt(0))
							{
								random = false;
								answer = arguments.substring(second_colon_index+1).trim();
							}
							else
							{
								bot.send(new Privmsg(nick, "Invalid syntax '!"+command+" "+arguments+"'"));
								return;
							}
						}
					}
					
					name = arguments.substring(0, first_colon_index).trim();
					if (null == answer)
					{
						answer = arguments.substring(first_colon_index+1).trim();
					}
					
					try
					{
						FaqData	faq_data = new FaqData(name, answer);
						faq_data.setRandom(random);
						database_faq.addFaq(bot, faq_data);
						bot.send(new Privmsg(nick, "Added faq '"+name+"'"));
					}
					catch (FaqManagerException e)
					{
						try
						{
							if (database_faq.getFaq(bot, name) != null)
							{
								bot.send(new Privmsg(nick, "Faq '"+name+"' already exists, delete it first to update it."));
							}
						}
						catch (FaqManagerException e2)
						{
							e.fillInStackTrace();
							throw e;
						}
					}
				}
			}
			else if (command.equals("delfaq"))
			{
				String	name = arguments.trim();

				if (database_faq.removeFaq(bot, arguments))
				{
					bot.send(new Privmsg(nick, "Removed faq '"+name+"'"));
				}
				else
				{
					bot.send(new Privmsg(nick, "Faq '"+name+"' couldn't be found."));
				}
			}
			else if (command.equals("editfaq"))
			{
				int	first_colon_index = arguments.indexOf(":");
				if (-1 == first_colon_index)
				{
					bot.send(new Privmsg(nick, "Invalid syntax '!"+command+" "+arguments+"'"));
					return;
				}
				else
				{
					String	name = null;
					String	answer = null;
					
					name = arguments.substring(0, first_colon_index).trim();
					answer = arguments.substring(first_colon_index+1).trim();
					
					FaqData	faq_data = database_faq.getFaq(bot, name);
					if (null == faq_data)
					{
						bot.send(new Privmsg(nick, "Faq '"+name+"' doesn't exist."));
					}
					else
					{
						faq_data.setName(name);
						faq_data.setAnswer(answer);
						if (database_faq.editFaq(faq_data))
						{
							bot.send(new Privmsg(nick, "Edited faq '"+name+"'"));
						}
						else
						{
							bot.send(new Privmsg(nick, "Error while editing faq '"+name+"'"));
						}
					}
				}
			}
		}
		catch (FaqManagerException e)
		{
			Logger.getLogger("com.uwyn.drone.modules").severe(ExceptionUtils.getExceptionStackTrace(e));
		}
	}
	
	public void channelCommand(Bot bot, Channel channel, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
		DatabaseFaqs	database_faq = DatabaseFaqsFactory.get();
		try
		{
			if (command.equals("?"))
			{
				String	name = arguments.trim();
				FaqData	faq_data = database_faq.getFaq(bot, name);
				
				if (faq_data != null)
				{
					channel.send(faq_data.getName()+": "+faq_data.getAnswer());
				}
				else
				{
					channel.send("Faq '"+name+"' couldn't be found.");
				}
			}
		}
		catch (FaqManagerException e)
		{
			Logger.getLogger("com.uwyn.drone.modules").severe(ExceptionUtils.getExceptionStackTrace(e));
		}
	}
}
