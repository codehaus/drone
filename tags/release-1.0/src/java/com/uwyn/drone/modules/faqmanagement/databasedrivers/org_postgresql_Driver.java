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
import com.uwyn.rife.database.Datasource;

public class org_postgresql_Driver extends DatabaseFaqs
{
	private static CreateSequence	sCreateSequenceFaq = null;
	private static CreateTable		sCreateTableFaq = null;
	private static Select			sGetFaqId = null;
	private static Insert			sAddFaq = null;
	private static Update			sEditFaq = null;
	private static Select			sGetFaq = null;
	private static Select			sGetFaqById = null;
	private static Select			sGetRandomFaqIds = null;
	private static Update			sSetRandom = null;
	private static Delete			sRemoveFaq = null;
	private static DropSequence		sDropSequenceFaq = null;
	private static DropTable		sDropTableFaq = null;
	
	public org_postgresql_Driver(Datasource datasource)
	{
		super(datasource);
		initializeQueries();
	}

	protected void initializeQueries()
	{
		if (null == sCreateSequenceFaq)
		{
			sCreateSequenceFaq = new CreateSequence(getDatasource());
			sCreateSequenceFaq
				.name("SEQ_FAQ");
		}
		
		if (null == sCreateTableFaq)
		{
			sCreateTableFaq = new CreateTable(getDatasource());
			sCreateTableFaq
				.table("Faq")
				.columns(FaqData.class)
				.column("botname", String.class, 30, CreateTable.NOTNULL)
				.primaryKey("FAQ_PK", "id")
				.unique("FAQ_KEY_UQ", new String[] {"botname", "lowcasekey"})
				.unique("FAQ_NAME_UQ", new String[] {"botname", "name"});
		}

		if (null == sGetFaqId)
		{
			sGetFaqId = new Select(getDatasource());
			sGetFaqId.field("nextval('SEQ_FAQ')");
		}

		if (null == sAddFaq)
		{
			sAddFaq = new Insert(getDatasource());
			sAddFaq
				.into("Faq")
				.fieldsParameters(FaqData.class)
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
				.field("*")
				.whereParameter("botname", "=")
				.whereParameterAnd("lowcasekey", "=");
		}

		if (null == sGetFaqById)
		{
			sGetFaqById = new Select(getDatasource());
			sGetFaqById
				.from("Faq")
				.field("*")
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
		
		if (null == sDropSequenceFaq)
		{
			sDropSequenceFaq = new DropSequence(getDatasource());
			sDropSequenceFaq
				.name("SEQ_FAQ");
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
		return _install(sCreateSequenceFaq, sCreateTableFaq);
	}

	public int addFaq(Bot bot, FaqData faqData)
	throws FaqManagerException
	{
		return _addFaq(sGetFaqId, sAddFaq, bot, faqData);
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
		return _remove(sDropSequenceFaq, sDropTableFaq);
	}
}
