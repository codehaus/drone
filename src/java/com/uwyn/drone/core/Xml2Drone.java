/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.drone.core.exceptions.InvalidModuleNameException;
import com.uwyn.rife.tools.BeanUtils;
import com.uwyn.rife.tools.exceptions.BeanUtilsException;
import com.uwyn.rife.xml.Xml2Data;
import com.uwyn.rife.xml.exceptions.XmlErrorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Xml2Drone extends Xml2Data
{
	private ArrayList	mBots = null;
	private HashMap		mServerInfos = null;
	
	private StringBuffer	mCharacterDataStack = null;

	private Bot				mCurrentBot = null;
	private ServerInfo		mCurrentServerInfo = null;
	private Module			mCurrentModule = null;
	private String			mCurrentProperty = null;
	
	public Collection getBots()
	{
		return mBots;
	}
	
	protected void clear()
	{
		mCurrentBot = null;
		mCurrentServerInfo = null;
	}
	
	public void startDocument()
	{
		clear();
		mBots = new ArrayList();
		mServerInfos = new HashMap();
	}
	
	public void endDocument()
	{
		clear();
	}

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
	{
		if (qName.equals("drone"))
		{
			// do nothing
		}
		else if (qName.equals("server"))
		{
			String	name = atts.getValue("name");
			
			mCurrentServerInfo = new ServerInfo();
			mServerInfos.put(name, mCurrentServerInfo);
		}
		else if (qName.equals("output"))
		{
			int	max = Integer.parseInt(atts.getValue("max"));
			int	amount = Integer.parseInt(atts.getValue("amount"));
			int	interval = Integer.parseInt(atts.getValue("interval"));
			
			mCurrentServerInfo.setOutput(max, amount, interval);
		}
		else if (qName.equals("address"))
		{
			String	name = atts.getValue("name");
			int		port = Integer.parseInt(atts.getValue("port"));
			
			try
			{
				mCurrentServerInfo.addAddress(name, port);
			}
			catch (CoreException e)
			{
				throw new XmlErrorException(e);
			}
		}
		else if (qName.equals("bot"))
		{
			String	name 		= atts.getValue("name");
			String	nick 		= atts.getValue("nick");
			String	alt_nick	= atts.getValue("altnick");
			String	real_name	= atts.getValue("realname");
			String	server_name	= atts.getValue("servername");
			
			if (BotFactory.contains(name))
			{
				throw new XmlErrorException("A bot with name '"+name+"' has already been set up.");
			}
			
			mCurrentBot = BotFactory.get(name);
			mBots.add(mCurrentBot);
			mCurrentBot.initialize(nick, alt_nick, real_name, new Server(server_name, (ServerInfo)mServerInfos.get(server_name)));
		}
		else if (qName.equals("channel"))
		{
			String	name = atts.getValue("name");
			
			try
			{
				mCurrentBot.join(name);
			}
			catch (CoreException e)
			{
				throw new XmlErrorException(e);
			}
		}
		else if (qName.equals("module"))
		{
			String	classname = atts.getValue("classname");

			Class	module_class = null;
			Module	module_instance = null;
			try
			{
				module_class = Class.forName(classname);
				module_instance = (Module)module_class.newInstance();
			}
			catch (ClassNotFoundException e)
			{
				throw new XmlErrorException(e);
			}
			catch (InstantiationException e)
			{
				throw new XmlErrorException(e);
			}
			catch (IllegalAccessException e)
			{
				throw new XmlErrorException(e);
			}
			
			mCurrentModule = module_instance;
		}
		else if (qName.equals("property"))
		{
			String	name = atts.getValue("name");

			if (null == mCurrentModule)
			{
				throw new XmlErrorException("No module specified to set the property '"+name+"' for.");
			}
			
			mCharacterDataStack = new StringBuffer();
			
			mCurrentProperty = name;
		}
		else
		{
			throw new XmlErrorException("Unsupport element name '"+qName+"'.");
		}
	}
	
	public void endElement(String namespaceURI, String localName, String qName)
	throws SAXException
	{
		if (qName.equals("module"))
		{
			try
			{
				mCurrentBot.addModule(mCurrentModule);
			}
			catch (InvalidModuleNameException e)
			{
				throw new XmlErrorException("The name of the module '"+mCurrentModule.getClass()+"' is invalid.");
			}
		}
		else if (qName.equals("property"))
		{
			String value = mCharacterDataStack.toString();
			mCharacterDataStack = null;
			
			try
			{
				BeanUtils.setPropertyValue(mCurrentModule, mCurrentProperty, value);
			}
			catch (BeanUtilsException e)
			{
				throw new XmlErrorException(e);
			}
		}
	}
	
	public void characters(char[] ch, int start, int length)
	{
		if (mCharacterDataStack != null &&
			length > 0)
		{
			mCharacterDataStack.append(String.copyValueOf(ch, start, length));
		}
	}
}


