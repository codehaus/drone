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
import com.uwyn.rife.engine.exceptions.EngineException;
import com.uwyn.rife.tools.ExceptionUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DownloadChannelLog extends ChannelLog
{
	protected void processChannelLog(ArrayList errors, Bot bot, Channel channel, Calendar day)
	{
		if (errors.size() > 0)
		{
			setContentType("text/html");
			print("invalid query");
			return;
		}
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String tsv_filename = "irclog-"+bot.getName()+"-"+channel.getName()+"-"+channel.getServer().getServerName()+"-"+format.format(day.getTime())+".txt";

		setContentType("application/download");
		addHeader("Content-Disposition", "attachment; filename="+tsv_filename+".zip");
		
		OutputStream binary_out = getOutputStream();
		ZipOutputStream zip_stream = new ZipOutputStream(getOutputStream());
		zip_stream.setLevel(9);
		zip_stream.setMethod(ZipOutputStream.DEFLATED);
		ZipEntry tsv_zip = new ZipEntry(tsv_filename);
		try
		{
			zip_stream.putNextEntry(tsv_zip);

			DownloadLogMessages log_messages = new DownloadLogMessages(zip_stream);
			try
			{
				DatabaseLogsFactory.get().getLogMessages(log_messages, bot, channel, day);
			}
			catch (LogManagerException e)
			{
				throw new EngineException(e);
			}
			
			zip_stream.closeEntry();
			zip_stream.close();
			// disabled since tomcat closes it anyhow and complains otherwise
			// binary_out.close();
		}
		catch (IOException e)
		{
			Logger.getLogger("com.uwyn.drone.webui").warning(ExceptionUtils.getExceptionStackTrace(e));
		}
	}
	
	private class DownloadLogMessages extends LogResultProcessor
	{
		private OutputStream	mOut = null;
		
		DownloadLogMessages(OutputStream out)
		{
			mOut = out;
		}
		
		public boolean gotMessage(Timestamp moment, ServerMessage serverMessage)
		{
			try
			{
				mOut.write(OUTPUT_TIME_FORMAT.format(moment).getBytes());
				mOut.write("\t".getBytes());
				mOut.write(serverMessage.getPrefix().getNickName().getBytes());
				mOut.write("\t".getBytes());
				
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
				mOut.write(message.toString().getBytes());
				mOut.write("\r\n".getBytes());
			}
			catch (IOException e)
			{
				Logger.getLogger("com.uwyn.drone.webui").warning(ExceptionUtils.getExceptionStackTrace(e));
				return false;
			}
			
			return true;
		}
	}
}

