/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.faqmanagement;

import com.uwyn.rife.config.Config;
import com.uwyn.rife.database.Datasource;
import com.uwyn.rife.database.Datasources;
import com.uwyn.rife.database.DbQueryManagerCache;
import com.uwyn.rife.database.DbQueryManagerFactory;

public abstract class DatabaseFaqsFactory extends DbQueryManagerFactory
{
	public static final String	MANAGER_PACKAGE_NAME = DatabaseFaqsFactory.class.getPackage().getName() + ".databasedrivers.";
	
	private static DbQueryManagerCache	mCache = new DbQueryManagerCache();
	
	public static DatabaseFaqs get()
	{
        Datasource datasource = Datasources.getRepInstance().getDatasource(Config.getRepInstance().getString("DATASOURCE"));
		return (DatabaseFaqs)getInstance(MANAGER_PACKAGE_NAME, mCache, datasource);
	}
}

