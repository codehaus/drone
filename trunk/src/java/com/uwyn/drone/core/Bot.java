/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import com.uwyn.drone.core.modulemessages.*;
import java.util.*;

import com.uwyn.drone.core.BotListener;
import com.uwyn.drone.core.Module;
import com.uwyn.drone.core.exceptions.AllNicksInUseException;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.core.exceptions.InvalidModuleNameException;
import com.uwyn.drone.core.exceptions.LogoffErrorException;
import com.uwyn.drone.core.exceptions.LogonErrorException;
import com.uwyn.drone.protocol.ResponseCode;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.drone.protocol.commands.IrcCommand;
import com.uwyn.drone.protocol.commands.Nick;
import com.uwyn.drone.protocol.commands.Pong;
import com.uwyn.drone.protocol.commands.Quit;
import com.uwyn.drone.protocol.commands.User;
import com.uwyn.rife.tools.ExceptionUtils;
import java.util.logging.Logger;

public class Bot implements Runnable, ServerListener, ResponseListener, CommandListener
{
	private String				mName = null;
	private String				mNick = null;
	private String				mAltNick = null;
	private Server				mServer = null;
	private String				mRealName = null;
	private boolean				mLoggedOn = false;
	private boolean				mConnected = false;
	private Thread				mBotThread = null;
	private Throwable			mServerError = null;
	
	private String				mConnectedNick = null;
	
	private HashSet		mJoinedChannels = null;
	private HashSet		mBotListeners = null;
	private Object		mBotListenersMonitor = new Object();
	private boolean		mPaused = false;
	
	private ArrayList		mModules = new ArrayList();
	private HashMap			mNamedModules = new HashMap();
	private HashMap			mMessageCommands = new HashMap();
	private HashMap			mNoticeCommands = new HashMap();
	private HashMap			mChannelCommands = new HashMap();
	private ArrayList		mChannelMessageModules = new ArrayList();
	private HashMap			mRawCommands = new HashMap();
	private HashMap			mResponseCodes = new HashMap();
	
	Bot(String name)
	{
		if (null == name)			throw new IllegalArgumentException("name can't be null.");
		if (0 == name.length())		throw new IllegalArgumentException("name can't be empty.");
		
		mName = name.toLowerCase();
	}
	
	public void initialize(String nick, String altNick, String realName, Server server)
	{
		if (null == nick)			throw new IllegalArgumentException("nick can't be null.");
		if (0 == nick.length())		throw new IllegalArgumentException("nick can't be empty.");
		if (null == altNick)		throw new IllegalArgumentException("altNick can't be null.");
		if (0 == altNick.length())	throw new IllegalArgumentException("altNick can't be empty.");
		if (nick.equals(altNick))	throw new IllegalArgumentException("nick and altNick can't be equal.");
		if (null == realName)		throw new IllegalArgumentException("realName can't be null.");
		if (null == server)			throw new IllegalArgumentException("server can't be null.");
		
		mNick = nick;
		mAltNick = altNick;
		mRealName = realName;
		mServer = server;
		mJoinedChannels = new HashSet();
		mBotListeners = new HashSet();
		
		mServer.addServerListener(this);
		mServer.addResponseListener(this);
		mServer.addCommandListener(this);
	}
	
	public ServerMessage createServerMessage(IrcCommand command)
	{
		StringBuffer raw = new StringBuffer(":");
		synchronized (raw)	// thread lock pre-allocation
		{
			raw.append(getConnectedNick());
			raw.append("!~");
			raw.append(getName());
			raw.append("@localhost ");
			raw.append(command.getCommand());
			return ServerMessage.parse(raw.toString());
		}
	}
		
	public void send(IrcCommand command)
	throws CoreException
	{
		synchronized (mServer)
		{
			mServer.send(command);
		}
	}
	
	public void logon()
	throws CoreException
	{
		if (!mServer.isConnected())
		{
			connect();
		}
		
		synchronized (mServer)
		{
			try
			{
				mServer.send(new User(mName, mRealName));
				changeNick(mNick);
			}
			catch (CoreException e)
			{
				throw new LogonErrorException(this, e);
			}
		}
			
		// call all the connected methods of the registered modules
		Iterator	it = mModules.iterator();
		Module		module = null;
		while (it.hasNext())
		{
			module = (Module)it.next();
			module.loggedon(this);
		}
	}
	
	public void redoLogon()
	throws CoreException
	{
		disconnect();
		logon();
	}
	
	public void changeNick(String nick)
	throws CoreException
	{
		synchronized (mServer)
		{
			if (nick.equals(mConnectedNick))
			{
				fireNickChanged();
				return;
			}
			
			mServer.send(new Nick(nick));
		}
	}
	
	private void joinActiveChannels()
	throws CoreException
	{
		synchronized (mJoinedChannels)
		{
			if (0 == mJoinedChannels.size())
			{
				return;
			}
			
			Iterator channels_it = mJoinedChannels.iterator();
			while (channels_it.hasNext())
			{
				((Channel)channels_it.next()).join();
			}
		}
	}
	
	public void logoff()
	throws CoreException
	{
		synchronized (mServer)
		{
			mLoggedOn = false;

			try
			{
				mServer.send(new Quit("shutting down"));
			}
			catch (CoreException e)
			{
				throw new LogoffErrorException(this, e);
			}
			
			fireLoggedOff();
		}
	}
	
	public boolean isLoggedOn()
	{
		return mLoggedOn;
	}
	
	public void connect()
	{
		mBotThread = new Thread(this);
		mBotThread.start();
		waitForServerConnection();
	}
	
	private void waitForServerConnection()
	{
		while (null == mServerError &&
			   !mServer.isConnected())
		{
			try
			{
				synchronized (mServer)
				{
					if (!mServer.isConnected())
					{
						mServer.wait();
					}
				}
			}
			catch (InterruptedException e)
			{
				return;
			}
			Thread.currentThread().yield();
		}
	}
	
	private void ensureServerConnection()
	throws CoreException
	{
		while (null == mServerError &&
			   !mServer.isConnected())
		{
			synchronized (mServer)
			{
				mServer.connect();
			}
			
			Thread.currentThread().yield();
		}
	}
	
    public void run()
    {
		try
		{
			ensureServerConnection();
		}
		catch (CoreException e)
		{
			synchronized (mServer)
			{
				mServerError = e;
				mServer.notifyAll();
			}
			fireConnectionError(e);
			return;
		}
		
		while (mConnected)
		{
			try
			{
				synchronized (mServer)
				{
					mServer.wait();
				}
			}
			catch (InterruptedException e)
			{
				return;
			}
			Thread.currentThread().yield();
		}
	}
	
	public void disconnect()
	throws CoreException
	{
		mConnected = false;
		mLoggedOn = false;
		mServer.disconnect();
	}
	
	public boolean isConnected()
	{
		return mConnected;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public String getNick()
	{
		return mNick;
	}
	
	public String getAltNick()
	{
		return mAltNick;
	}
	
	public Server getServer()
	{
		return mServer;
	}
	
	public String getConnectedNick()
	{
		return mConnectedNick;
	}
	
	public HashSet getJoinedChannels()
	{
		return mJoinedChannels;
	}
	
	public Channel getJoinedChannel(String name)
	{
		Channel channel = new Channel(name, mServer);
		if (!mJoinedChannels.contains(channel))
		{
			return null;
		}
		
		return channel;
	}
	
	public Collection getModules()
	{
		return mModules;
	}
	
	public Map getNamedModules()
	{
		return mNamedModules;
	}
	
	public boolean join(String channelName)
	throws CoreException
	{
		if (null == channelName)		throw new IllegalArgumentException("channelName can't be null.");
		if (0 == channelName.length())	throw new IllegalArgumentException("channelName can't be empty.");
		
		Channel channel = mServer.getChannel(channelName);
		
		synchronized (mJoinedChannels)
		{
			// only join if the bot is connected, otherwise
			// defer it until the logon
			if (mServer.isConnected())
			{
				channel.join();
			}
			
			mJoinedChannels.add(channel);
		}
		
		return true;
	}
	
	public boolean leave(String channelName)
	throws CoreException
	{
		if (null == channelName)		throw new IllegalArgumentException("channelName can't be null.");
		if (0 == channelName.length())	throw new IllegalArgumentException("channelName can't be empty.");
		
		Channel channel = mServer.getChannel(channelName);

		synchronized (mJoinedChannels)
		{
			// only sent a connection message to the server
			// if the connection is really alive
			if (mServer.isConnected())
			{
				channel.leave();
			}
			
			mJoinedChannels.remove(channel);
		}
		
		return true;
	}
	
	public void pause()
	throws CoreException
	{
		synchronized (mJoinedChannels)
		{
			if (mPaused)
			{
				return;
			}
			
			mPaused = true;
			
			if (0 == mJoinedChannels.size())
			{
				return;
			}
			
			Iterator channels_it = mJoinedChannels.iterator();
			while (channels_it.hasNext())
			{
				((Channel)channels_it.next()).leave();
			}
		}
	}
	
	public void resume()
	throws CoreException
	{
		synchronized (mJoinedChannels)
		{
			if (!mPaused)
			{
				return;
			}
			
			mPaused = false;
			
			if (0 == mJoinedChannels.size())
			{
				return;
			}
			
			Iterator channels_it = mJoinedChannels.iterator();
			while (channels_it.hasNext())
			{
				((Channel)channels_it.next()).join();
			}
		}
	}
	
	public boolean isPaused()
	{
		return mPaused;
	}
	
	public void disconnected(Server server)
	throws CoreException
	{
		synchronized (mServer)
		{
			mConnected = false;
			mConnectedNick = null;
			mServer.notifyAll();
		}
	}
	
	public void connected(Server server)
	throws CoreException
	{
		synchronized (mServer)
		{
			// bot was logged on when the server got disconnected
			// restore the connection
			if (mLoggedOn)
			{
				try
				{
					logon();
				}
				catch (CoreException e)
				{
					Logger.getLogger("com.uwyn.drone.core").severe("Unable to log back into a previously disconnected server : "+ExceptionUtils.getExceptionStackTrace(e));
				}
			}
			
			mConnected = true;
			mServer.notifyAll();
		}
	}
	
	private void dispatchModuleMessage(Collection modules, ModuleMessage message)
	{
		if (modules != null)
		{
			Iterator	modules_it = modules.iterator();
			Module		module = null;
			while (modules_it.hasNext())
			{
				module = (Module)modules_it.next();
				module.getRunner().addMessage(message);
			}
		}
	}
	
	public void receivedResponse(ServerMessage message)
	throws CoreException
	{
//		Logger.getLogger("com.uwyn.drone.core").info("RESPONSE :"+message);
		
		// process response codes that need to be sent to modules
		if (mResponseCodes.size() > 0)
		{
			dispatchModuleMessage((Collection)mResponseCodes.get(message.getResponseCode()), new Response(message));
		}

		// successful connection
		if (ResponseCode.RPL_WELCOME == message.getResponseCode())
		{
			synchronized (mServer)
			{
				mConnectedNick = (String)message.getParameters().get(0);
				mLoggedOn = true;
				
				fireLoggedOn();
				
				joinActiveChannels();
			}
		}
		// nick name already exists
		else if (ResponseCode.ERR_NICKNAMEINUSE == message.getResponseCode() ||
				 ResponseCode.ERR_ALREADYREGISTRED == message.getResponseCode())
		{
			String nick = (String)message.getParameters().get(1);
			
			// try to get a free nick at the first connection
			if (null == mConnectedNick)
			{
				if (nick.equals(mNick))
				{
					mServer.send(new Nick(mAltNick));
				}
				else if (nick.equals(mAltNick))
				{
					throw new AllNicksInUseException(this);
				}
				else
				{
					mServer.send(new Nick(mNick));
				}
			}
			// otherwise, notify the listeners
			else
			{
				fireNickInUse(nick);
			}
		}
	}
	
	public void receivedCommand(ServerMessage message)
	throws CoreException
	{
//		Logger.getLogger("com.uwyn.drone.core").info("COMMAND  :"+message+", TRAILING :"+message.getTrailing());
		
		// process raw commands that need to be sent to modules
		if (mRawCommands.size() > 0)
		{
			dispatchModuleMessage((Collection)mRawCommands.get(message.getCommand().toLowerCase()), new RawCommand(message));
		}

		// auto re-join
		if (message.getCommand().equals("KICK"))
		{
			join((String)message.getParameters().get(0));
		}
		
		// sync nick changes
		else if (message.getCommand().equals("NICK") &&
				 message.getPrefix().getNickName().equals(mConnectedNick))
		{
			synchronized (mServer)
			{
				mConnectedNick = message.getTrailing();
				fireNickChanged();
			}
		}
		
		// auto re-join at excess flood
		else if (message.getCommand().equals("ERROR") &&
		    message.getTrailing().indexOf("Excess Flood") != -1)
		{
			getServer().reconnect();
		}
		
		// respond to ping commands
		else if (message.getCommand().equals("PING"))
		{
			Pong pong = null;
			if (message.getParameters().size() > 0 &&
				message.getParameters().get(0) != null &&
				((String)message.getParameters().get(0)).length() > 0)
			{
				pong = new Pong((String)message.getParameters().get(0));
			}
			else if (message.getTrailing() != null &&
					 message.getTrailing().length() > 0)
			{
				pong = new Pong(message.getTrailing());
			}
			
			if (pong != null)
			{
				getServer().send(pong);
			}
		}

		// process commands that can be subscribed to by modules and for which
		// the commands have to be seperated from the arguments
		else if (message.getCommand().equals("NOTICE") ||
				 message.getCommand().equals("PRIVMSG"))
		{
			// seperate the command from the arguments
			String	trailing = message.getTrailing();
			int		command_seperation_index = trailing.indexOf(" ");
			String	command = null;
			String	arguments = null;
			if (command_seperation_index >= 0)
			{
				command = trailing.substring(0, command_seperation_index).toLowerCase();
				arguments = trailing.substring(command_seperation_index+1);
			}
			else
			{
				command = trailing.toLowerCase();
				arguments = null;
			}

			// respond to notice commands
			if (message.getCommand().equals("NOTICE"))
			{
				dispatchModuleMessage((Collection)mNoticeCommands.get(command), new NoticeCommand(message, command, arguments));
			}
	
			// repond to privmsg commands
			else if (message.getCommand().equals("PRIVMSG"))
			{
				// check if this was a channel message
				if (message.getParameters().size() > 0 &&
					((String)message.getParameters().get(0)).length() > 0 &&
					'#' == ((String)message.getParameters().get(0)).charAt(0))
				{
					Channel channel = mServer.getChannel((String)message.getParameters().get(0));
	
					dispatchModuleMessage(mChannelMessageModules, new ChannelMessage(message, channel));

					// send the commands to all the modules that handle them
					if (message.getTrailing().startsWith("!"))
					{
						dispatchModuleMessage((Collection)mChannelCommands.get(command), new ChannelCommand(message, channel, command, arguments));
					}
				}
				// check if this was a private message
				else if (mConnectedNick != null &&
						 ((String)message.getParameters().get(0)).toLowerCase().equals(mConnectedNick.toLowerCase()))
				{
					dispatchModuleMessage((Collection)mMessageCommands.get(command), new MessageCommand(message, command, arguments));
				}
			}
		}
	}

	private void fireLoggedOn()
	{
		Iterator	listeners = mBotListeners.iterator();
		
		while (listeners.hasNext())
		{
			((BotListener)listeners.next()).loggedOn(this);
		}
	}

	private void fireLoggedOff()
	{
		Iterator	listeners = mBotListeners.iterator();
		
		while (listeners.hasNext())
		{
			((BotListener)listeners.next()).loggedOff(this);
		}
	}

	private void fireNickChanged()
	{
		Iterator	listeners = mBotListeners.iterator();
		
		while (listeners.hasNext())
		{
			((BotListener)listeners.next()).nickChanged(this);
		}
	}

	private void fireNickInUse(String nick)
	{
		Iterator	listeners = mBotListeners.iterator();
		
		while (listeners.hasNext())
		{
			((BotListener)listeners.next()).nickInUse(this, nick);
		}
	}

	private void fireConnectionError(Throwable e)
	{
		Iterator	listeners = mBotListeners.iterator();
		
		while (listeners.hasNext())
		{
			((BotListener)listeners.next()).connectionError(this, e);
		}
	}

	public boolean addBotListener(BotListener listener)
	{
		if (null == listener)	throw new IllegalArgumentException("listener can't be null.");

		boolean result = false;
		
		synchronized (mBotListenersMonitor)
		{
			if (!mBotListeners.contains(listener))
			{
				HashSet clone = (HashSet)mBotListeners.clone();
				result = clone.add(listener);
				mBotListeners = clone;
			}
			else
			{
				result = true;
			}
		}
		
		assert true == mBotListeners.contains(listener);
		
		return result;
	}

	public boolean removeBotListener(BotListener listener)
	{
		if (null == listener)	throw new IllegalArgumentException("listener can't be null.");

        boolean result = false;
		
		synchronized (mBotListenersMonitor)
		{
			HashSet clone = (HashSet)mBotListeners.clone();
			result = clone.remove(listener);
			mBotListeners = clone;
		}
		
		assert false == mBotListeners.contains(listener);
		
		return result;
	}

	public boolean addModule(Module module)
	throws InvalidModuleNameException
	{
		if (null == module)	throw new IllegalArgumentException("module can't be null.");

		boolean result = false;
		
		if (module.getName() != null)
		{
			if (0 == module.getName().length() ||
				module.getName().indexOf(" ") != -1)
			{
				throw new InvalidModuleNameException(module);
			}
		}
		
		synchronized (mModules)
		{
			if (!mModules.contains(module))
			{
				// store the module as being present
				result = mModules.add(module);
				// add a mapping from the name to the module for fast retrieval
				if (module.getName() != null)
				{
					mNamedModules.put(module.getName().toLowerCase(), module);
				}
				// create the runner and start it
				ModuleRunner runner  = new ModuleRunner(this, module);
				module.setRunner(runner);
				runner.start();
				
				// register all module hooks
				ResponseCode	response_code = null;
				String			command = null;
				ArrayList		modules = null;
				
				// register response codes
				ResponseCode[]	response_codes = module.getResponseCodes();
				if (response_codes != null)
				{
					for (int i = 0; i < response_codes.length; i++)
					{
						response_code = response_codes[i];
						modules = (ArrayList)mResponseCodes.get(response_code);
						if (null == modules)
						{
							modules = new ArrayList();
							mResponseCodes.put(response_code, modules);
						}
						
						if (!modules.contains(module))
						{
							modules.add(module);
						}
					}
				}
				
				// register message commands
				String[]	message_commands = module.getMessageCommands();
				if (message_commands != null)
				{
					for (int i = 0; i < message_commands.length; i++)
					{
						command = message_commands[i].toLowerCase();
						modules = (ArrayList)mMessageCommands.get(command);
						if (null == modules)
						{
							modules = new ArrayList();
							mMessageCommands.put(command, modules);
						}
						
						if (!modules.contains(module))
						{
							modules.add(module);
						}
					}
				}
				
				// register notice commands
				String[]	notice_commands = module.getNoticeCommands();
				if (notice_commands != null)
				{
					for (int i = 0; i < notice_commands.length; i++)
					{
						command = notice_commands[i].toLowerCase();
						modules = (ArrayList)mNoticeCommands.get(command);
						if (null == modules)
						{
							modules = new ArrayList();
							mNoticeCommands.put(command, modules);
						}
						
						if (!modules.contains(module))
						{
							modules.add(module);
						}
					}
				}
				
				// register channel commands
				String[]	channel_commands = module.getChannelCommands();
				if (channel_commands != null)
				{
					for (int i = 0; i < channel_commands.length; i++)
					{
						command = "!"+channel_commands[i].toLowerCase();
						modules = (ArrayList)mChannelCommands.get(command);
						if (null == modules)
						{
							modules = new ArrayList();
							mChannelCommands.put(command, modules);
						}
						
						if (!modules.contains(module))
						{
							modules.add(module);
						}
					}
				}
				
				// register channel messages
				if (!mChannelMessageModules.contains(module))
				{
					mChannelMessageModules.add(module);
				}
				
				// register raw commands
				String[]	raw_commands = module.getRawCommands();
				if (raw_commands != null)
				{
					for (int i = 0; i < raw_commands.length; i++)
					{
						command = raw_commands[i].toLowerCase();
						modules = (ArrayList)mRawCommands.get(command);
						if (null == modules)
						{
							modules = new ArrayList();
							mRawCommands.put(command, modules);
						}
						
						if (!modules.contains(module))
						{
							modules.add(module);
						}
					}
				}
			}
			else
			{
				result = true;
			}
		}
		
		assert true == mModules.contains(module);
		
		return result;
	}
}
