/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.webui.elements;

import com.uwyn.drone.Droned;
import com.uwyn.rife.engine.Element;

public class DroneVersion extends Element
{
	public void processElement()
	{
		print(Droned.getVersion());
	}
}

