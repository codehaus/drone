/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import com.uwyn.drone.core.exceptions.*;
import java.io.*;

import com.uwyn.drone.core.ServerListener;
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.drone.protocol.commands.IrcCommand;
import com.uwyn.rife.tools.ExceptionUtils;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

public class Server implements Runnable, TimedOutputStreamListener
{
	private static final int			DEFAULT_MAX = 1024;
	private static final int			DEFAULT_AMOUNT = 25;
	private static final int			DEFAULT_INTERVAL = 1000;
	
	private String			mServerName = null;
	private ServerInfo		mServerInfo = null;
	private Thread			mServerThread = null;
	
	private	Socket				mServerSocket = null;
	private BufferedReader		mInput = null;
	private BufferedWriter		mOutput = null;
	private TimedOutputStream	mTimedOutput = null;
	private HashMap				mChannels = null;
	private boolean				mConnected = false;

	private HashSet		mServerListeners = null;
	private HashSet		mCommandListeners = null;
	private HashSet		mResponseListeners = null;
	private Integer		mServerListenersMonitor = new Integer(0);
	private Integer		mCommandListenersMonitor = new Integer(0);
	private Integer		mResponseListenersMonitor = new Integer(0);
	
	Server(String serverName, ServerInfo serverInfo)
	{
		assert serverName != null;
		assert serverInfo != null;
		
		mServerName = serverName.toLowerCase();
		mServerInfo = serverInfo;
		
		mChannels = new HashMap();
		mServerListeners = new HashSet();
		mCommandListeners = new HashSet();
		mResponseListeners = new HashSet();

		cleanUp();
	}
	
	private void cleanUp()
	{
		mServerSocket = null;
		mInput = null;
		mOutput = null;
		mConnected = false;
	}
	
	public String getServerName()
	{
		return mServerName;
	}
	
	public ServerInfo getServerInfo()
	{
		return mServerInfo;
	}
	
	public Socket getServerSocket()
	{
		return mServerSocket;
	}
	
	public Channel getChannel(String name)
	{
		if (null == name)		throw new IllegalArgumentException("name can't be null.");
		if (0 == name.length())	throw new IllegalArgumentException("name can't be empty.");
		
		Channel channel = null;
		
		name = name.toLowerCase();
		
		synchronized (mChannels)
		{
			channel = (Channel)mChannels.get(name);
			
			if (null == channel)
			{
				channel = new Channel(name, this);
				mChannels.put(name, channel);
			}
		}
		
		assert channel != null;
		assert channel.getName().equals(name);
		assert channel.getServer() == this;
			
		return channel;
	}
	
	public synchronized void connect()
	throws CoreException
	{
		if (0 == mServerInfo.getAddresses().size())
		{
			throw new MissingAddressException(this);
		}
		
		InputStream			input_stream = null;
		Iterator			addresses_it = null;
		InetSocketAddress	address = null;
		
		boolean is_connected = false;
		
		while (!is_connected)
		{
			try
			{
				// try all addresses, one by one until a valid connection
				// could
				addresses_it = mServerInfo.getAddresses().iterator();
				while (addresses_it.hasNext())
				{
					address = (InetSocketAddress)addresses_it.next();
					try
					{
						mServerSocket = new Socket(address.getAddress(), address.getPort());
						break;
					}
					catch (IOException e)
					{
						mServerSocket = null;
					}
				}
				
				// no valid address was found, throw an error
				if (null == mServerSocket)
				{
					throw new NoValidAddressException(this);
				}
				
				// continue with the first valid address
				mServerSocket.setSoTimeout(0);
				
				input_stream = mServerSocket.getInputStream();
				if (mTimedOutput != null)
				{
					mTimedOutput.close();
				}
				mTimedOutput = new TimedOutputStream(mServerSocket.getOutputStream(), mServerInfo.getMax(), mServerInfo.getAmount(), mServerInfo.getInterval());
				mTimedOutput.addTimedOutputStreamListener(this);
				is_connected = true;
			}
			catch (IOException e)
			{
				Logger.getLogger("com.uwyn.drone.core").severe("Error while connecting from the server '"+this+"', retrying : "+ExceptionUtils.getExceptionStackTrace(e));
			}
		}
		
		mInput = new BufferedReader(new InputStreamReader(input_stream));
		mOutput = new BufferedWriter(new OutputStreamWriter(mTimedOutput));

		mConnected = true;
		mServerThread = new Thread(this);
		mServerThread.start();
		fireConnected();
		
		assert mServerSocket.isConnected();
		assert mInput != null;
		assert mOutput != null;
	}
	
	public synchronized void disconnect()
	throws CoreException
	{
		mConnected = false;
		
		try
		{
			mInput = null;
			
			if (mOutput != null)
			{
				mOutput.close();
			}
			if (mServerSocket != null)
			{
				mServerSocket.close();
			}
		}
		catch (IOException e)
		{
			Logger.getLogger("com.uwyn.drone.core").severe("Error while disconnecting from the server '"+this+"', reconnecting : "+ExceptionUtils.getExceptionStackTrace(e));
			cleanUp();
			try
			{
				connect();
			}
			catch (CoreException e2)
			{
				throw new ServerDisconnectionErrorException(this, e2);
			}
			
			return;
		}
		
		this.notifyAll();
		
		cleanUp();
		fireDisconnected();
	}
	
	private void handleTerminatedStream(Throwable e)
	{
		if (mConnected)
		{
			mConnected = false;
			while (!mConnected)
			{
				if (e != null)
				{
					Logger.getLogger("com.uwyn.drone.core").severe("Error during the server connection, reconnecting : "+ExceptionUtils.getExceptionStackTrace(e));
				}
				else
				{
					Logger.getLogger("com.uwyn.drone.core").severe("Stream to IRC server has terminated, reconnecting.");
				}
				
				try
				{
					reconnect();
				}
				catch (CoreException e2)
				{
					Logger.getLogger("com.uwyn.drone.core").severe("Unable to restore connection after a terminated server stream : "+ExceptionUtils.getExceptionStackTrace(e2));
					try
					{
						// sleep 30 seconds before trying again
						Thread.sleep(30000);
					}
					catch (InterruptedException e3)
					{
						return;
					}
				}
			}
		}
		
		return;
	}
	
    public void run()
    {
		while (mConnected)
		{
			try
			{
				if (mInput != null)
				{
					String message_string = mInput.readLine();
					
					// check if a message has been received, if this is not the case,
					// the connection has been terminated and the stream also
					// try to reconnect until this succeeds
					if (null == message_string)
					{
						handleTerminatedStream(null);
						return;
					}
					// handle the received message
					else
					{
						System.out.println(message_string);
						
						ServerMessage	message = ServerMessage.parse(message_string);
						if (message.isResponse())
						{
							fireReceivedResponse(message);
						}
						if (message.isCommand())
						{
							fireReceivedCommand(message);
						}
					}
				}
			}
			catch (IOException e)
			{
				handleTerminatedStream(e);
				return;
			}
			
			mServerThread.yield();
		}
		
		mServerThread = null;
	}
	
	public boolean isConnected()
	{
		return mConnected;
	}
	
	synchronized boolean send(IrcCommand command)
	throws CoreException
	{
		if (null == mServerSocket ||
			!mServerSocket.isConnected() ||
			mServerSocket.isOutputShutdown() ||
		    !mServerSocket.isBound())
		{
			return false;
		}
		
		try
		{
			mOutput.write(command.getCommand());
			mOutput.newLine();
		}
		catch(IOException e)
		{
			throw new SendErrorException(this, command, e);
		}
		
		flush();
	
		return true;
	}
	
	public void reconnect()
	throws CoreException
	{
		synchronized (this)
		{
			disconnect();
			connect();
		}
	}
	
	public void exceptionThrow(IOException e)
	{
		try
		{
			reconnect();
		}
		catch (CoreException e2)
		{
			Logger.getLogger("com.uwyn.drone.core").severe("Unable to restore connection after socket error : "+ExceptionUtils.getExceptionStackTrace(e));
		}
	}
	
	synchronized private boolean flush()
	throws CoreException
	{
		if (null == mServerSocket ||
			!mServerSocket.isConnected() ||
			mServerSocket.isOutputShutdown())
		{
			return false;
		}
		
		try
		{
			mOutput.flush();
		}
		catch(IOException e)
		{
			throw new FlushErrorException(this, e);
		}
	
		return true;
	}

	private void fireConnected()
	throws CoreException
	{
		synchronized (mServerListenersMonitor)
		{
			Iterator	listeners = mServerListeners.iterator();
			
			while (listeners.hasNext())
			{
				((ServerListener)listeners.next()).connected(this);
			}
		}
	}

	private void fireDisconnected()
	throws CoreException
	{
		synchronized (mServerListenersMonitor)
		{
			Iterator	listeners = mServerListeners.iterator();
			
			while (listeners.hasNext())
			{
				((ServerListener)listeners.next()).disconnected(this);
			}
		}
	}

	private void fireReceivedCommand(ServerMessage command)
	{
		synchronized (mCommandListenersMonitor)
		{
			Iterator	listeners = mCommandListeners.iterator();
			
			while (listeners.hasNext())
			{
				try
				{
					((CommandListener)listeners.next()).receivedCommand(command);
				}
				catch (CoreException e)
				{
					Logger.getLogger("com.uwyn.drone.core").severe(ExceptionUtils.getExceptionStackTrace(e));
				}
			}
		}
	}

	private void fireReceivedResponse(ServerMessage Response)
	{
		synchronized (mResponseListenersMonitor)
		{
			Iterator	listeners = mResponseListeners.iterator();
			
			while (listeners.hasNext())
			{
				try
				{
					((ResponseListener)listeners.next()).receivedResponse(Response);
				}
				catch (CoreException e)
				{
					Logger.getLogger("com.uwyn.drone.core").severe(ExceptionUtils.getExceptionStackTrace(e));
				}
			}
		}
	}

	public boolean addServerListener(ServerListener listener)
	{
		if (null == listener)	throw new IllegalArgumentException("listener can't be null.");

		boolean result = false;
		
		synchronized (mServerListenersMonitor)
		{
			if (!mServerListeners.contains(listener))
			{
				result = mServerListeners.add(listener);
			}
			else
			{
				result = true;
			}
		}
		
		assert true == mServerListeners.contains(listener);
		
		return result;
	}

	public boolean removeServerListener(ServerListener listener)
	{
		if (null == listener)	throw new IllegalArgumentException("listener can't be null.");

        boolean result = false;
		
		synchronized (mServerListenersMonitor)
		{
			result = mServerListeners.remove(listener);
		}
		
		assert false == mServerListeners.contains(listener);
		
		return result;
	}

	public boolean addCommandListener(CommandListener listener)
	{
		if (null == listener)	throw new IllegalArgumentException("listener can't be null.");

		boolean result = false;
		
		synchronized (mCommandListenersMonitor)
		{
			if (!mCommandListeners.contains(listener))
			{
				result = mCommandListeners.add(listener);
			}
			else
			{
				result = true;
			}
		}
		
		assert true == mCommandListeners.contains(listener);
		
		return result;
	}

	public boolean removeCommandListener(CommandListener listener)
	{
		if (null == listener)	throw new IllegalArgumentException("listener can't be null.");

        boolean result = false;
		
		synchronized (mCommandListenersMonitor)
		{
			result = mCommandListeners.remove(listener);
		}
		
		assert false == mCommandListeners.contains(listener);
		
		return result;
	}

	public boolean addResponseListener(ResponseListener listener)
	{
		if (null == listener)	throw new IllegalArgumentException("listener can't be null.");

		boolean result = false;
		
		synchronized (mResponseListenersMonitor)
		{
			if (!mResponseListeners.contains(listener))
			{
				result = mResponseListeners.add(listener);
			}
			else
			{
				result = true;
			}
		}
		
		assert true == mResponseListeners.contains(listener);
		
		return result;
	}

	public boolean removeResponseListener(ResponseListener listener)
	{
		if (null == listener)	throw new IllegalArgumentException("listener can't be null.");

        boolean result = false;
		
		synchronized (mResponseListenersMonitor)
		{
			result = mResponseListeners.remove(listener);
		}
		
		assert false == mResponseListeners.contains(listener);
		
		return result;
	}
}
