/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.faqmanagement;

import com.uwyn.rife.site.ConstrainedProperty;
import com.uwyn.rife.site.Validation;

public class FaqData extends Validation
{
	private int		mId = -1;
    private String  mName = null;
    private String  mLowcasekey = null;
    private String  mAnswer = null;
	private boolean	mRandom = false;

    public FaqData()
    {
		super();
		
		init();
	}
	
    public FaqData(String name, String answer)
    {
		super();
		
		setName(name);
		setAnswer(answer);
		
		init();
	}
	
	private void init()
	{
		addConstraint(new ConstrainedProperty("id").notEqual(-1).notNull(true));
		addConstraint(new ConstrainedProperty("name").notNull(true).notEmpty(true).maxLength(255));
		addConstraint(new ConstrainedProperty("lowcasekey").notNull(true).notEmpty(true).maxLength(255));
		addConstraint(new ConstrainedProperty("answer").notNull(true).notEmpty(true));
		addConstraint(new ConstrainedProperty("random").notNull(true).defaultValue(false));
    }
	
    public int getId()
    {
        return mId;
    }

    public void setId(int id)
    {
        mId = id;
    }
	
    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        mName = name;
		
		if (null == mName)
		{
			mLowcasekey = null;
		}
		else
		{
			mLowcasekey = mName.toLowerCase();
		}
    }
	
	public void setLowcasekey(String lowcasekey)
	{
	}
	
	public String getLowcasekey()
	{
		return mLowcasekey;
	}
	
    public String getAnswer()
    {
        return mAnswer;
    }

    public void setAnswer(String answer)
    {
        mAnswer = answer;
    }
	
	public void setRandom(boolean state)
	{
		mRandom = state;
	}
	
	public boolean isRandom()
	{
		return mRandom;
	}
}
