/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone;

import com.uwyn.drone.core.BotsRunner;
import com.uwyn.drone.core.BotsRunnerListener;
import com.uwyn.rife.rep.Rep;
import com.uwyn.rife.resources.ResourceFinder;
import com.uwyn.rife.resources.ResourceFinderClasspath;
import com.uwyn.rife.tools.FileUtils;
import com.uwyn.rife.tools.exceptions.FileUtilsErrorException;
import java.net.URL;

public class Droned implements BotsRunnerListener
{
	private static String	sVersion = null;
	private static Integer	msVersionMonitor = new Integer(0);
	
	private BotsRunner	mBotsRunner = null;
	
	private Droned(BotsRunner botsRunner)
	{
		assert botsRunner != null;
		mBotsRunner = botsRunner;
		mBotsRunner.addBotsRunnerListener(this);
	}
	
	private void waitForFinish()
	{
		synchronized (mBotsRunner)
		{
			while (mBotsRunner.isAlive())
			{
				try
				{
					mBotsRunner.wait();
				}
				catch (InterruptedException e)
				{
					// do nothing
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		ResourceFinder	resource_finder = ResourceFinderClasspath.getInstance();

		Rep.initialize("rep/participants.xml", resource_finder);
		
		BotsRunner	bots_runner = (BotsRunner)Rep.getParticipant("com.uwyn.drone.core.DroneParticipant").getObject();
		Droned		drone = new Droned(bots_runner);
		drone.waitForFinish();
	}
	
	public void finished(BotsRunner botsRunner)
	{
		synchronized (mBotsRunner)
		{
			mBotsRunner.notifyAll();
		}
	}
	
	public static String getVersion()
	{
		if (null == sVersion)
		{
			synchronized (msVersionMonitor)
			{
				// check a second time since another thread could have modified
				// the version in the meantime
				if (null == sVersion)
				{
					ResourceFinderClasspath resource_finder = ResourceFinderClasspath.getInstance();
					URL						version_resource = resource_finder.getResource("DRONE_VERSION");
					if (version_resource != null)
					{
						try
						{
							sVersion = FileUtils.readString(version_resource);
						}
						catch (FileUtilsErrorException e)
						{
							sVersion = null;
						}
						
						if (sVersion != null)
						{
							sVersion = sVersion.trim();
						}
					}
					if (null == sVersion)
					{
						sVersion = "unknown version";
					}
				}
			}
		}
		return sVersion;
	}
}

