/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.webui.elements.pub;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.modules.exceptions.LogManagerException;
import com.uwyn.drone.modules.logmanagement.DatabaseLogsFactory;
import com.uwyn.drone.modules.logmanagement.LogResultProcessor;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.rife.engine.Element;
import com.uwyn.rife.engine.exceptions.EngineException;
import com.uwyn.rife.site.ValidationError;
import com.uwyn.rife.template.Template;
import com.uwyn.rife.tools.StringUtils;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelLog extends Element
{
    public static final SimpleDateFormat	DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	public static final SimpleDateFormat	OUTPUT_TIME_FORMAT = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat	OUTPUT_DATE_FORMAT = new SimpleDateFormat("dd-MMMM-yyyy");

	public static final String				IRC_ACTION = "\u0001ACTION";

	private static final String[] 			NICK_COLORS = new String[] {"eeeeee", "eeeecc", "cceeee", "eeccee", "ccccee", "eecccc", "cceecc"};
	private static final Pattern			URL_HIGHLIGHT = Pattern.compile("((?:http|ftp)s?://(?:%[\\p{Digit}A-Fa-f][\\p{Digit}A-Fa-f]|[\\-_\\.!~*';\\|/?:@#&=\\+$,\\p{Alnum}])+)");
	private static final Pattern			EMAIL_HIGHLIGHT = Pattern.compile("([a-zA-Z0-9][_\\-\\.\\w]*@[\\w\\.\\-]+\\.[a-zA-Z]{2,4})");

	static
	{
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
		OUTPUT_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
		OUTPUT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	private Template	mTemplate = null;
	
	static Channel validateChannel(ArrayList errors, Bot bot, String channelName)
	{
		if (null == channelName)
		{
			errors.add(new ValidationError.MANDATORY("channelname"));
		}
		else
		{
			Channel channel = bot.getJoinedChannel(channelName);
			if (null == channel)
			{
				errors.add(new ValidationError.INVALID("channelname"));
			}
			else
			{
				return channel;
			}
		}
		
		return null;
	}
	
	public final void processElement()
	{
		ArrayList errors = new ArrayList();
		
		Bot			bot = null;
		Channel		channel = null;
		Calendar	day = null;
		
		String botname = null;
		String channelname = null;
		
		botname = getInput("botname");
		channelname = getInput("channelname");

		String[] parts = StringUtils.splitToArray(getPathInfo(), "/");
		if (null == botname &&
			parts.length > 1)
		{
			botname = parts[1];
		}
		if (null == channelname &&
			parts.length > 2)
		{
			channelname = parts[2];
		}
		
		if (null == botname ||
			null == channelname)
		{
			exit("back_to_list");
		}
		
		if (!channelname.startsWith("#"))
		{
			channelname = "#"+channelname;
		}
		
		bot = BotList.validateBotName(errors, botname);
		if (bot != null)
		{
			channel = validateChannel(errors, bot, channelname);
		}
		day = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		if (hasInputValue("day"))
		{
			try
			{
				day.setTime(DATE_FORMAT.parse(getInput("day")));
			}
			catch (ParseException e)
			{
				errors.add(new ValidationError.INVALID("DAY"));
			}
		}
		
		processChannelLog(errors, bot, channel, day);
	}

	protected void processChannelLog(ArrayList errors, Bot bot, Channel channel, Calendar day)
	{
		mTemplate = getHtmlTemplate("pub.channel_log");
		
		if (errors.size() > 0)
		{
			mTemplate
				.getBeanHandler()
				.getFormBuilder()
				.getValidationBuilder()
				.generateValidationErrors(mTemplate, errors, null);
		}
		else
		{
			setOutput("botname", bot.getName());
			setOutput("channelname", channel.getName());
			Calendar previous = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			Calendar next = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			previous.set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH));
			previous.add(Calendar.DAY_OF_MONTH, -1);
			next.set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH));
			next.add(Calendar.DAY_OF_MONTH, 1);
			setOutput("day", ChannelLog.DATE_FORMAT.format(previous.getTime()));
			setExitQuery(mTemplate, "show_previous");
			setOutput("day", ChannelLog.DATE_FORMAT.format(next.getTime()));
			setExitQuery(mTemplate, "show_next");

			mTemplate.setValue("botname", encodeHtml(bot.getName()));
			mTemplate.setValue("channelname", encodeHtml(channel.getName()));
			mTemplate.setValue("servername", encodeHtml(channel.getServer().getServerName()));
			mTemplate.setValue("day", encodeHtml(OUTPUT_DATE_FORMAT.format(day.getTime())));
			GetLogMessages log_messages = new GetLogMessages();
			try
			{
				if (DatabaseLogsFactory.get().getLogMessages(log_messages, bot, channel, day))
				{
					setExitQuery(mTemplate, "download", new String[] {"day", ChannelLog.DATE_FORMAT.format(day.getTime())});
				}
				else
				{
					mTemplate.setValue("download", "");
				}
			}
			catch (LogManagerException e)
			{
				throw new EngineException(e);
			}
		}
		
		print(mTemplate);
	}
	
	private class GetLogMessages extends LogResultProcessor
	{
		private HashMap	mNickColors = new HashMap();
		
		private int mColorCounter = 0;
		
		public boolean gotMessage(Timestamp moment, ServerMessage serverMessage)
		{
			String nickname = serverMessage.getPrefix().getNickName();
			
			String nickcolor = (String)mNickColors.get(nickname);
			if (null == nickcolor)
			{
				nickcolor = NICK_COLORS[mColorCounter++%NICK_COLORS.length];
				mNickColors.put(nickname, nickcolor);
			}
			mTemplate.setValue("bgcolor", nickcolor);
			
			mTemplate.setValue("time", OUTPUT_TIME_FORMAT.format(moment));
			mTemplate.setValue("nickname", encodeHtml(nickname));
		
			// translate the \u0001ACTION command which corresponds to
			// /me so that the user's nickname is used instead
			StringBuffer message = new StringBuffer();
			if (serverMessage.getPrefix() != null &&
				serverMessage.getTrailing().startsWith(IRC_ACTION))
			{
				message.append(serverMessage.getPrefix().getNickName());
				message.append(serverMessage.getTrailing().substring(IRC_ACTION.length()));
			}
			else
			{
				message.append(serverMessage.getTrailing());
			}
			
			String encoded_message = encodeHtml(message.toString());
			
			encoded_message = convertUrl(encoded_message, URL_HIGHLIGHT);
			
			Matcher email_matcher = EMAIL_HIGHLIGHT.matcher(encoded_message);
			encoded_message = email_matcher.replaceAll("<a href=\"mailto:$1\">$1</a>");
			
			mTemplate.setValue("message", encoded_message);
			
			mTemplate.appendBlock("messages", "message");
			
			return true;
		}
	}
	
	private static String convertUrl(String source, Pattern pattern)
	{
		String result = source;
		
		Matcher	url_matcher = pattern.matcher(source);
		boolean found = url_matcher.find();
        if (found)
		{
			String visual_url = null;
			String actual_url = null;
			int last = 0;
            StringBuffer sb = new StringBuffer();
			synchronized (sb)
			{
				do
				{
					actual_url = url_matcher.group(1);
					if (url_matcher.groupCount() > 1)
					{
						visual_url = url_matcher.group(2);
					}
					else
					{
						visual_url = actual_url;
					}
					
					sb.append(source.substring(last, url_matcher.start(0)));
					sb.append("<a href=\"");
					if (actual_url.startsWith("www."))
					{
						actual_url = "http://"+actual_url;
					}
					sb.append(actual_url);
					sb.append("\"");
					sb.append(">");
					if (visual_url.length() <= 80)
					{
						sb.append(visual_url);
					}
					else
					{
						String ellipsis = "...";
						int last_slash = visual_url.lastIndexOf("/");
						int trailing_length = visual_url.length() - last_slash + ellipsis.length();
						int start_slash = visual_url.indexOf("/", visual_url.indexOf("://")+3);
						int previous_start_slash = start_slash;
						while (start_slash+trailing_length < 80)
						{
							previous_start_slash = start_slash;
							start_slash = visual_url.indexOf("/", previous_start_slash+1);
						}
						
						sb.append(visual_url.substring(0, previous_start_slash+1));
						sb.append(ellipsis);
						sb.append(visual_url.substring(last_slash));
					}
					sb.append("</a>");
					
					last = url_matcher.end(0);
					
					found = url_matcher.find();
				}
				while (found);
				
				sb.append(source.substring(last));
				result = sb.toString();
			}
        }
		
		return result;
	}
}

