/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.logmanagement;

import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.rife.database.DbRowProcessor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public abstract class LogResultProcessor extends DbRowProcessor
{
	private int mCount = 0;

    public LogResultProcessor()
    {
		super();
	}
	
	public int getCount()
	{
		return mCount;
	}
	
	public abstract boolean gotMessage(Timestamp moment, ServerMessage serverMessage);
	
	public boolean processRow(ResultSet resultSet) throws SQLException
	{
		Timestamp	moment = resultSet.getTimestamp("moment");
		String		raw = resultSet.getString("raw");
		
		if (moment != null &&
			raw != null)
		{
			mCount++;
			return gotMessage(moment, ServerMessage.parse(raw));
		}
	
		return false;
	}
}
