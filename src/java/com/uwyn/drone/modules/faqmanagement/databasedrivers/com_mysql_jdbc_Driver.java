/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.faqmanagement.databasedrivers;

import com.uwyn.rife.database.queries.*;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.modules.exceptions.FaqManagerException;
import com.uwyn.drone.modules.faqmanagement.DatabaseFaqs;
import com.uwyn.drone.modules.faqmanagement.FaqData;
import com.uwyn.drone.modules.faqmanagement.exceptions.AddFaqErrorException;
import com.uwyn.drone.modules.faqmanagement.exceptions.GetFaqIdErrorException;
import com.uwyn.drone.modules.faqmanagement.exceptions.InstallErrorException;
import com.uwyn.drone.modules.faqmanagement.exceptions.RemoveErrorException;
import com.uwyn.rife.database.Datasource;
import com.uwyn.rife.database.DbPreparedStatement;
import com.uwyn.rife.database.exceptions.DatabaseException;

public class com_mysql_jdbc_Driver extends DatabaseFaqs
{
	private static CreateTable		sCreateTableFaq = null;
	private static Select			sGetFaqId = null;
	private static Insert			sAddFaq = null;
	private static Update			sEditFaq = null;
	private static Select			sGetFaq = null;
	private static Select			sGetFaqById = null;
	private static Select			sGetRandomFaqIds = null;
	private static Update			sSetRandom = null;
	private static Delete			sRemoveFaq = null;
	private static DropTable		sDropTableFaq = null;
	
	public com_mysql_jdbc_Driver(Datasource datasource)
	{
		super(datasource);
		initializeQueries();
	}

	protected void initializeQueries()
	{
		if (null == sCreateTableFaq)
		{
			sCreateTableFaq = new CreateTable(getDatasource());
			sCreateTableFaq
				.table("Faq")
				.columns(FaqData.class)
				.column("botname", String.class, 30, CreateTable.NOTNULL)
				.customAttribute("id", "AUTO_INCREMENT")
				.primaryKey("FAQ_PK", "id")
				.unique("FAQ_KEY_UQ", new String[] {"botname", "lowcasekey"})
				.unique("FAQ_NAME_UQ", new String[] {"botname", "name"});
		}

		if (null == sGetFaqId)
		{
			sGetFaqId = new Select(getDatasource());
			sGetFaqId.field("LAST_INSERT_ID()");
		}

		if (null == sAddFaq)
		{
			sAddFaq = new Insert(getDatasource());
			sAddFaq
				.into("Faq")
				.fieldsParametersExcluded(FaqData.class, new String[] {"id"})
				.fieldParameter("botname");
		}

		if (null == sEditFaq)
		{
			sEditFaq = new Update(getDatasource());
			sEditFaq
				.table("Faq")
				.fieldsParametersExcluded(FaqData.class, new String[] {"id"})
				.whereParameter("id", "=");
		}

		if (null == sGetFaq)
		{
			sGetFaq = new Select(getDatasource());
			sGetFaq
				.from("Faq")
				.whereParameter("botname", "=")
				.whereParameterAnd("lowcasekey", "=");
		}

		if (null == sGetFaqById)
		{
			sGetFaqById = new Select(getDatasource());
			sGetFaqById
				.from("Faq")
				.whereParameter("id", "=");
		}
		
		if (null == sGetRandomFaqIds)
		{
			sGetRandomFaqIds = new Select(getDatasource());
			sGetRandomFaqIds
				.from("Faq")
				.field("id")
				.where("random", "=", true)
				.whereParameterAnd("botname", "=");
		}
		
		if (null == sSetRandom)
		{
			sSetRandom = new Update(getDatasource());
			sSetRandom
				.table("Faq")
				.fieldParameter("random")
				.whereParameter("botname", "=")
				.whereParameterAnd("lowcasekey", "=");
		}
		
		if (null == sRemoveFaq)
		{
			sRemoveFaq = new Delete(getDatasource());
			sRemoveFaq
				.from("Faq")
				.whereParameter("botname", "=")
				.whereParameterAnd("lowcasekey", "=");
		}
		
		if (null == sDropTableFaq)
		{
			sDropTableFaq = new DropTable(getDatasource());
			sDropTableFaq
				.table("Faq");
		}
	}
	
	public boolean install()
	throws FaqManagerException
	{
		try
		{
			executeUpdate(sCreateTableFaq);
		}
		catch (DatabaseException e)
		{
			throw new InstallErrorException(e);
		}
		
		return true;
	}

	public int addFaq(Bot bot, FaqData faqData)
	throws FaqManagerException
	{
		if (null == bot)		throw new IllegalArgumentException("bot can't be null.");
		if (null == faqData)	throw new IllegalArgumentException("faqData can't be null.");
		if (!faqData.validate())
		{
			throw new AddFaqErrorException(faqData);
		}
		
		int result = -1;

		try
		{
			DbPreparedStatement ps_add_faq = getPreparedStatement(sAddFaq);
			try
			{
				ps_add_faq.setString("botname", bot.getName());
				ps_add_faq.setBean(faqData);
				
				if (0 == ps_add_faq.executeUpdate())
				{
					throw new AddFaqErrorException(faqData);
				}
			}
			finally
			{
				ps_add_faq.close();
			}
		}
		catch (DatabaseException e)
		{
			throw new AddFaqErrorException(faqData, e);
		}
		
		try
		{
			DbPreparedStatement	ps_get_faqid = getPreparedStatement(sGetFaqId);
			try
			{
				ps_get_faqid.executeQuery();
				if (ps_get_faqid.getResultSet().hasResultRows())
				{
					result = ps_get_faqid.getResultSet().getFirstInt();
				}
			}
			finally
			{
				ps_get_faqid.close();
			}
		}
		catch (DatabaseException e)
		{
			throw new GetFaqIdErrorException(e);
		}
		
		assert result >= 0;

		return result;
	}

	public boolean editFaq(FaqData faqData)
	throws FaqManagerException
	{
		return _editFaq(sEditFaq, faqData);
	}
	
	public FaqData getFaq(Bot bot, String name)
	throws FaqManagerException
	{
		return _getFaq(sGetFaq, bot, name);
	}
	
	public FaqData getFaqById(int id)
	throws FaqManagerException
	{
		return _getFaqById(sGetFaqById, id);
	}
	
	public FaqData getRandomFaq(Bot bot)
	throws FaqManagerException
	{
		return _getRandomFaq(sGetRandomFaqIds, bot);
	}
	
	public boolean setRandom(Bot bot, String name, boolean state)
	throws FaqManagerException
	{
		return _setRandom(sSetRandom, bot, name, state);
	}
	
	public boolean removeFaq(Bot bot, String name)
	throws FaqManagerException
	{
		return _removeFaq(sRemoveFaq, bot, name);
	}

	public boolean remove()
	throws FaqManagerException
	{
		try
		{
			executeUpdate(sDropTableFaq);
		}
		catch (DatabaseException e)
		{
			throw new RemoveErrorException(e);
		}
		
		return true;
	}
}
