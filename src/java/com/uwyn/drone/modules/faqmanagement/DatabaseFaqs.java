/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.faqmanagement;

import com.uwyn.drone.modules.faqmanagement.exceptions.*;
import com.uwyn.rife.database.*;
import com.uwyn.rife.database.queries.*;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.modules.exceptions.FaqManagerException;
import com.uwyn.drone.modules.faqmanagement.FaqManager;
import com.uwyn.rife.database.exceptions.DatabaseException;
import com.uwyn.rife.database.exceptions.InnerClassException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

public abstract class DatabaseFaqs extends DbQueryManager implements FaqManager
{
	private static Random	msRandomFaq = new Random();
	
    protected DatabaseFaqs(Datasource datasource)
    {
        super(datasource);
    }

	public abstract boolean install()
	throws FaqManagerException;
	
	public abstract boolean remove()
	throws FaqManagerException;

	protected boolean _install(final CreateSequence createSequenceFaq, final CreateTable createTableFaq)
	throws FaqManagerException
	{
		assert createSequenceFaq != null;
		assert createTableFaq != null;
		
		try
		{
			inTransaction(new DbTransactionUserWithoutResult() {
					public void useTransactionWithoutResult()
					throws InnerClassException
					{
						executeUpdate(createSequenceFaq);
						executeUpdate(createTableFaq);
					}
				});
		}
		catch (DatabaseException e) 
		{
			throw new InstallErrorException(e);
		}
		
		return true;
	}
	
	protected int _addFaq(Select getFaqId, Insert addFaq, final Bot bot, final FaqData faqData)
	throws FaqManagerException
	{
		assert getFaqId != null;
		assert addFaq != null;
		
		if (null == bot)		throw new IllegalArgumentException("bot can't be null.");
		if (null == faqData)	throw new IllegalArgumentException("faqData can't be null.");
		
		faqData.validate();
		faqData.makeSubjectValid("id");
		if (faqData.countValidationErrors() > 0)
		{
			throw new AddFaqErrorException(faqData);
		}
		
		int	faq_id = -1;
		int result = -1;

		try
		{
			faq_id = executeGetFirstInt(getFaqId);
			if (-1 == faq_id)
			{
				throw new GetFaqIdErrorException();
			}
		}
		catch (DatabaseException e)
		{
			throw new GetFaqIdErrorException(e);
		}
		
		if (faq_id >= 0)
		{
			faqData.setId(faq_id);
			
			try
			{
				if (0 == executeUpdate(addFaq, new DbPreparedStatementHandler() {
						public void setParameters(DbPreparedStatement statement)
						{
							statement
								.setString("botname", bot.getName())
								.setBean(faqData);
						}
					}))
				{
					throw new AddFaqErrorException(faqData);
				}

				result = faq_id;
			}
			catch (DatabaseException e)
			{
				throw new AddFaqErrorException(faqData, e);
			}
		}
		
		assert result >= 0;

		return result;
	}

	protected boolean _editFaq(Update editFaq, final FaqData faqData)
	throws FaqManagerException
	{
		assert editFaq != null;
		
		if (null == faqData)		throw new IllegalArgumentException("name can't be null");
		
		boolean	result = false;
		
		try
		{
			if (1 == executeUpdate(editFaq, new DbPreparedStatementHandler() {
					public void setParameters(DbPreparedStatement statement)
					{
						statement
							.setBean(faqData);
					}
				}))
			{
				result = true;
			}
		}
		catch (DatabaseException e)
		{
			throw new EditFaqErrorException(faqData, e);
		}

		return result;
	}
	
	protected FaqData _getFaq(Select getFaq, final Bot bot, final String name)
	throws FaqManagerException
	{
		assert getFaq != null;
		
		if (null == bot)		throw new IllegalArgumentException("bot can't be null.");
		if (null == name)		throw new IllegalArgumentException("name can't be null");
		if (0 == name.length())	throw new IllegalArgumentException("name can't be empty");
		
		FaqData result = null;

		try
		{
			result = (FaqData)executeFetchFirstBean(getFaq, FaqData.class, new DbPreparedStatementHandler() {
				public void setParameters(DbPreparedStatement statement)
				{
					statement
						.setString("botname", bot.getName())
						.setString("lowcasekey", name.toLowerCase());
				}});
		}
		catch (DatabaseException e)
		{
			throw new GetFaqErrorException(name, e);
		}

		return result;
	}
	
	protected FaqData _getFaqById(Select getFaqById, final int id)
	throws FaqManagerException
	{
		assert getFaqById != null;
		
		if (id < 0)	throw new IllegalArgumentException("id can't be negative");
		
		FaqData result = null;

		try
		{
			result = (FaqData)executeFetchFirstBean(getFaqById, FaqData.class, new DbPreparedStatementHandler() {
				public void setParameters(DbPreparedStatement statement)
				{
					statement
						.setInt("id", id);
				}});
		}
		catch (DatabaseException e)
		{
			throw new GetFaqByIdErrorException(id, e);
		}

		return result;
	}
	
	protected FaqData _getRandomFaq(Select getRandomFaqIds, final Bot bot)
	throws FaqManagerException
	{
		assert getRandomFaqIds != null;
		
		if (null == bot)	throw new IllegalArgumentException("bot can't be null.");

		FaqData result = null;

		try
		{
			CollectFaqIds row_processor = new CollectFaqIds();
			executeFetchAll(getRandomFaqIds, row_processor, new DbPreparedStatementHandler() {
				public void setParameters(DbPreparedStatement statement)
				{
					statement
						.setString("botname", bot.getName());
				}});
			
			ArrayList	faq_ids = row_processor.getFaqIds();
			if (faq_ids.size() > 0)
			{
				int	random_id = msRandomFaq.nextInt(faq_ids.size());
				result = getFaqById(((Integer)faq_ids.get(random_id)).intValue());
			}
		}
		catch (DatabaseException e)
		{
			throw new GetRandomFaqErrorException(e);
		}

		return result;
	}

	protected boolean _setRandom(Update setRandom, final Bot bot, final String name, final boolean state)
	throws FaqManagerException
	{
		assert setRandom != null;
		
		if (null == bot)		throw new IllegalArgumentException("bot can't be null.");
		if (null == name)		throw new IllegalArgumentException("name can't be null");
		if (0 == name.length())	throw new IllegalArgumentException("name can't be empty");
		
		boolean	result = false;
		
		try
		{
			if (1 == executeUpdate(setRandom, new DbPreparedStatementHandler() {
					public void setParameters(DbPreparedStatement statement)
					{
						statement
							.setBoolean("random", state)
							.setString("botname", bot.getName())
							.setString("lowcasekey", name.toLowerCase());
					}
				}))
			{
				result = true;
			}
		}
		catch (DatabaseException e)
		{
			throw new SetRandomErrorException(name, state, e);
		}

		return result;
	}

	protected boolean _removeFaq(Delete removeFaq, final Bot bot, final String name)
	throws FaqManagerException
	{
		assert removeFaq != null;
		
		if (null == bot)		throw new IllegalArgumentException("bot can't be null.");
		if (null == name)		throw new IllegalArgumentException("name can't be null");
		if (0 == name.length())	throw new IllegalArgumentException("name can't be empty");
		
		boolean	result = false;
		
		try
		{
			if (1 == executeUpdate(removeFaq, new DbPreparedStatementHandler() {
					public void setParameters(DbPreparedStatement statement)
					{
						statement
							.setString("botname", bot.getName())
							.setString("lowcasekey", name.toLowerCase());
					}
				}))
			{
				result = true;
			}
		}
		catch (DatabaseException e)
		{
			throw new RemoveFaqErrorException(name, e);
		}

		return result;
	}
	
	protected boolean _remove(final DropSequence dropSequenceFaq, final DropTable dropTableFaq)
	throws FaqManagerException
	{
		assert dropSequenceFaq != null;
		assert dropTableFaq != null;

		try
		{
			inTransaction(new DbTransactionUserWithoutResult() {
					public void useTransactionWithoutResult()
					throws InnerClassException
					{
						executeUpdate(dropTableFaq);
						executeUpdate(dropSequenceFaq);
					}
				});
		}
		catch (DatabaseException e)
		{
			throw new RemoveErrorException(e);
		}
		
		return true;
	}
	
	class CollectFaqIds extends DbRowProcessor
	{
		private ArrayList	mFaqIds = new ArrayList();
		
		CollectFaqIds()
		{
		}

		public boolean processRow(ResultSet resultSet)
		throws SQLException
		{
			int id = resultSet.getInt("id");
			
			mFaqIds.add(new Integer(id));
			
			return true;
		}
		
		ArrayList getFaqIds()
		{
			return mFaqIds;
		}
	}
}



