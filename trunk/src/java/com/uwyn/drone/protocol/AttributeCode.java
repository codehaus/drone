/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.protocol;

import com.uwyn.rife.datastructures.EnumClass;

public class AttributeCode extends EnumClass
{
	public static final AttributeCode ENDLINE = new AttributeCode("\r");
	public static final AttributeCode BOLD = new AttributeCode("\u0002");
	public static final AttributeCode NORMAL = new AttributeCode("\u000f");
	public static final AttributeCode REVERSE = new AttributeCode("\u0016");
	public static final AttributeCode UNDERLINE = new AttributeCode("\u001f");
	
	public static final AttributeCode WHITE = new AttributeCode("\u00030");
	public static final AttributeCode BLACK = new AttributeCode("\u00031");
	public static final AttributeCode DARKBLUE = new AttributeCode("\u00032");
	public static final AttributeCode GREEN = new AttributeCode("\u00033");
	public static final AttributeCode RED = new AttributeCode("\u00034");
	public static final AttributeCode BROWN = new AttributeCode("\u00035");
	public static final AttributeCode PURPLE = new AttributeCode("\u00036");
	public static final AttributeCode ORANGE = new AttributeCode("\u00037");
	public static final AttributeCode YELLOW = new AttributeCode("\u00038");
	public static final AttributeCode LIGHTGREEN = new AttributeCode("\u00039");
	public static final AttributeCode CYAN = new AttributeCode("\u000310");
	public static final AttributeCode LIGHTBLUE = new AttributeCode("\u000311");
	public static final AttributeCode BLUE = new AttributeCode("\u000312");
	public static final AttributeCode LAVENDER = new AttributeCode("\u000313");
	public static final AttributeCode GRAY = new AttributeCode("\u000314");
	public static final AttributeCode LIGHTGRAY = new AttributeCode("\u000315");
	
	public static final AttributeCode BG_WHITE = new AttributeCode("\u0003,0");
	public static final AttributeCode BG_BLACK = new AttributeCode("\u0003,1");
	public static final AttributeCode BG_DARKBLUE = new AttributeCode("\u0003,2");
	public static final AttributeCode BG_GREEN = new AttributeCode("\u0003,3");
	public static final AttributeCode BG_RED = new AttributeCode("\u0003,4");
	public static final AttributeCode BG_BROWN = new AttributeCode("\u0003,5");
	public static final AttributeCode BG_PURPLE = new AttributeCode("\u0003,6");
	public static final AttributeCode BG_ORANGE = new AttributeCode("\u0003,7");
	public static final AttributeCode BG_YELLOW = new AttributeCode("\u0003,8");
	public static final AttributeCode BG_LIGHTGREEN = new AttributeCode("\u0003,9");
	public static final AttributeCode BG_CYAN = new AttributeCode("\u0003,10");
	public static final AttributeCode BG_LIGHTBLUE = new AttributeCode("\u0003,11");
	public static final AttributeCode BG_BLUE = new AttributeCode("\u0003,12");
	public static final AttributeCode BG_LAVENDER = new AttributeCode("\u0003,13");
	public static final AttributeCode BG_GRAY = new AttributeCode("\u0003,14");
	public static final AttributeCode BG_LIGHTGRAY = new AttributeCode("\u0003,15");
	
	AttributeCode(String code)
	{
		super(code);
	}

	public static AttributeCode get(String code)
	{
		return (AttributeCode)AttributeCode.getMember(AttributeCode.class, code);
	}
	
	public String getCode()
	{
		return (String)mIdentifier;
	}
	
	public String toString()
	{
		return (String)mIdentifier;
	}
}

