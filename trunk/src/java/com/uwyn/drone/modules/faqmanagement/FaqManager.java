/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.faqmanagement;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.modules.exceptions.FaqManagerException;

public interface FaqManager
{
	public int addFaq(Bot bot, FaqData faqData) throws FaqManagerException;
	public boolean editFaq(FaqData faqData) throws FaqManagerException;
	public FaqData getFaq(Bot bot, String name) throws FaqManagerException;
	public FaqData getFaqById(int id) throws FaqManagerException;
	public FaqData getRandomFaq(Bot bot) throws FaqManagerException;
	public boolean removeFaq(Bot bot, String name) throws FaqManagerException;
	public boolean setRandom(Bot bot, String name, boolean state) throws FaqManagerException;
}
