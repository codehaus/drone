/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.webui.elements.pub;

import java.util.*;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.BotFactory;
import com.uwyn.drone.core.BotsRunner;
import com.uwyn.drone.core.Channel;
import com.uwyn.rife.engine.Element;
import com.uwyn.rife.rep.Rep;
import com.uwyn.rife.site.ValidationError;
import com.uwyn.rife.template.Template;
import com.uwyn.rife.tools.SortListComparables;

public class BotList extends Element
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
	
	public void processElement()
	{
		mTemplate = getHtmlTemplate("pub.bot_list");

		// output the bot list and the possible actions
		generateBotsList();
		
		print(mTemplate);
	}
	
	private void generateBotsList()
	{
		setOutput("day", ChannelLog.DATE_FORMAT.format(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime()));
		
		Collection	bots = ((BotsRunner)Rep.getParticipant("com.uwyn.drone.core.DroneParticipant").getObject()).getBots();
		Iterator	bots_it = bots.iterator();
		Bot			bot = null;
		while (bots_it.hasNext())
		{
			bot = (Bot)bots_it.next();
			
			setOutput("botname", bot.getName());
			
			mTemplate.setValue("botname", encodeHtml(bot.getName()));
			mTemplate.setValue("connected_nick", encodeHtml(bot.getConnectedNick()));
			mTemplate.setValue("nicks", encodeHtml(bot.getNick()+", "+bot.getAltNick()));
			
			HashMap		server_channels = new HashMap();
			String		server_name = null;
			ArrayList	channel_names = null;
			
			Iterator	joined_channels_it = bot.getJoinedChannels().iterator();
			Channel		channel = null;
			while (joined_channels_it.hasNext())
			{
				channel = (Channel)joined_channels_it.next();
				
				server_name = channel.getServer().getServerName();
				channel_names = (ArrayList)server_channels.get(server_name);
				if (null == channel_names)
				{
					channel_names = new ArrayList();
					server_channels.put(server_name, channel_names);
				}
				channel_names.add(channel.getName());
			}
			
			SortListComparables sort = new SortListComparables();
			mTemplate.removeValue("servers");
			
			Iterator	server_channels_it = server_channels.keySet().iterator();
			String		server = null;
			while (server_channels_it.hasNext())
			{
				server = (String)server_channels_it.next();
				
				mTemplate.setValue("server", encodeHtml(server));
				
				channel_names = (ArrayList)server_channels.get(server);
				
				sort.sort(channel_names);
				mTemplate.removeValue("channels");
				
				Iterator	channel_names_it = channel_names.iterator();
				String		channel_name = null;
				while (channel_names_it.hasNext())
				{
					channel_name = (String)channel_names_it.next();
					setOutput("channelname", channel_name);
					setExitQuery(mTemplate, "show_channel_log");
					
					mTemplate.setValue("channel", encodeHtml(channel_name));
					mTemplate.appendBlock("channels", "channel");
				}
				
				mTemplate.appendBlock("servers", "server");
			}
			
			mTemplate.appendBlock("bots", "bot");
		}
	}
}

