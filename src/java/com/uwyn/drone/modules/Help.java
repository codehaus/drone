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
import com.uwyn.drone.core.Module;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.protocol.AttributeCode;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.drone.protocol.commands.Notice;
import com.uwyn.drone.protocol.commands.Privmsg;
import com.uwyn.rife.tools.StringUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Help extends AbstractModule
{
	private static final String[]	COMMANDS = new String[] {"help"};
	private static final Pattern	HELP_PATTERN = Pattern.compile("^\\s*(\\w+)\\s*(.*)\\s*$");
	private static final String		VAR_BOTNICK = "$botnick";
	private static final String		VAR_MODULENAME = "$modulename";

	private static final String		MODULES_HELP = 	
		AttributeCode.BOLD+"Drone"+AttributeCode.BOLD+" consists out of a collection of pluggable modules"+AttributeCode.ENDLINE+
		"that provide the actual functionalities. Below is a list"+AttributeCode.ENDLINE+
		"of the modules that are available in this bot."+AttributeCode.ENDLINE+
		"For more information on a specific module, type        "+AttributeCode.ENDLINE+
		AttributeCode.BOLD+"/msg $botnick help <module>"+AttributeCode.BOLD+"."+AttributeCode.ENDLINE+
		" "+AttributeCode.ENDLINE;

	
	public String[] getMessageCommands()
	{
		return COMMANDS;
	}
	
	public void messageCommand(Bot bot, String nick, String command, String arguments, ServerMessage fullMessage)
	throws CoreException
	{
		if (command.equals("help"))
		{
			Map		modules = bot.getNamedModules();
			Module	module = null;
			
			// display module-specific help
			if (arguments != null &&
				arguments.trim().length() > 0)
			{
				Matcher logsearch_matcher = HELP_PATTERN.matcher(arguments.toLowerCase());
				
				// check if the syntax is correct
				if (!logsearch_matcher.matches() ||
					(logsearch_matcher.groupCount() < 1 &&
					 logsearch_matcher.groupCount() > 2))
				{
					bot.send(new Privmsg(nick, "Invalid syntax '"+command+" "+arguments+"'."));
					return;
				}
				
				// get the different help arguments
				String module_name = null;
				String help_key = null;
				module_name = logsearch_matcher.group(1);
				if (logsearch_matcher.groupCount() > 1)
				{
					help_key = logsearch_matcher.group(2);
					if (0 == help_key.length())
					{
						help_key = null;
					}
				}
				
				// check if the module is known
				module = (Module)modules.get(module_name);
				if (null == module)
				{
					bot.send(new Privmsg(nick, "Unknown module '"+module_name+"'."));
					return;
				}
				
				// obtain the requested help text
				Map helpmap = module.getHelpMap();
				if (null == helpmap ||
					!helpmap.containsKey(help_key))
				{
					bot.send(new Privmsg(nick, "Couldn't find the requested help information in module '"+module_name+"'."));
					return;
				}
				
				// output the help text
				String help = (String)helpmap.get(help_key);
				help = StringUtils.replace(help, VAR_MODULENAME, module.getName());
				outputHelp(bot, nick, help);
			}
			// display help about all the installed modules
			else
			{
				if (modules.size() > 0)
				{
					StringBuffer	module_help = new StringBuffer(MODULES_HELP);
					String			description = null;
					
					Iterator	modules_it = modules.values().iterator();
					while (modules_it.hasNext())
					{
						module = (Module)modules_it.next();
						
						module_help.append(AttributeCode.BOLD+module.getName()+AttributeCode.BOLD+AttributeCode.ENDLINE);
						description = module.getDescription();
						if (description != null)
						{
							module_help.append(module.getDescription()+AttributeCode.ENDLINE);
						}
					}
				
					// output the help text
					outputHelp(bot, nick, module_help.toString());
				}
			}
		}
	}
	
	private void outputHelp(Bot bot, String nick, String help)
	throws CoreException
	{
		ArrayList	help_lines = StringUtils.split(help, AttributeCode.ENDLINE.toString());
		Iterator	help_lines_it = help_lines.iterator();
		String		help_line = null;
		while (help_lines_it.hasNext())
		{
			help_line = (String)help_lines_it.next();
			
			// output the line if it's not empty
			if (help_line.length() > 0)
			{
				// replace help variables
				help_line = StringUtils.replace(help_line, VAR_BOTNICK, bot.getConnectedNick());
				
				bot.send(new Notice(nick, help_line+AttributeCode.ENDLINE));
			}
		}
	}
}
