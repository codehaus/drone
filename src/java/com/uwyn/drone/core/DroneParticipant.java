/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import com.uwyn.drone.core.BotsRunner;
import com.uwyn.rife.rep.BlockingParticipant;
import java.util.ArrayList;
import java.util.List;

public class DroneParticipant extends BlockingParticipant
{
	private BotsRunner	mBotsRunner = null;
	
	public DroneParticipant()
	{
		setInitializationMessage("Starting the IRC bot ...");
	}
	
	protected void initialize()
	{
		Xml2Drone xml_drone = new Xml2Drone();
		xml_drone.processXml(getParameter(), getResourceFinder());
		
		mBotsRunner = new BotsRunner(xml_drone.getBots());
		mBotsRunner.start();
	}

	protected Object _getObject(Object key)
	{
		return mBotsRunner;
	}

	protected List _getObjects(Object key)
	{
		ArrayList objects_list = new ArrayList();

		objects_list.add(mBotsRunner);

		return objects_list;
	}
}


