/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.protocol;

import com.uwyn.rife.datastructures.EnumClass;

public class ResponseCode extends EnumClass
{
	public static final ResponseCode RPL_WELCOME = new ResponseCode("001", "RPL_WELCOME");
	public static final ResponseCode RPL_YOURHOST = new ResponseCode("002", "RPL_YOURHOST");
	public static final ResponseCode RPL_CREATED = new ResponseCode("003", "RPL_CREATED");
	public static final ResponseCode RPL_MYINFO = new ResponseCode("004", "RPL_MYINFO");
	public static final ResponseCode RPL_BOUNCE = new ResponseCode("005", "RPL_BOUNCE");
	public static final ResponseCode RPL_USERHOST = new ResponseCode("302", "RPL_USERHOST");
	public static final ResponseCode RPL_ISON = new ResponseCode("303", "RPL_ISON");
	public static final ResponseCode RPL_AWAY = new ResponseCode("301", "RPL_AWAY");
	public static final ResponseCode RPL_UNAWAY = new ResponseCode("305", "RPL_UNAWAY");
	public static final ResponseCode RPL_NOWAWAY = new ResponseCode("306", "RPL_NOWAWAY");
	public static final ResponseCode RPL_WHOISUSER = new ResponseCode("311", "RPL_WHOISUSER");
	public static final ResponseCode RPL_WHOISSERVER = new ResponseCode("312", "RPL_WHOISSERVER");
	public static final ResponseCode RPL_WHOISOPERATOR = new ResponseCode("313", "RPL_WHOISOPERATOR");
	public static final ResponseCode RPL_WHOISIDLE = new ResponseCode("317", "RPL_WHOISIDLE");
	public static final ResponseCode RPL_ENDOFWHOIS = new ResponseCode("318", "RPL_ENDOFWHOIS");
	public static final ResponseCode RPL_WHOISCHANNELS = new ResponseCode("319", "RPL_WHOISCHANNELS");
	public static final ResponseCode RPL_WHOWASUSER = new ResponseCode("314", "RPL_WHOWASUSER");
	public static final ResponseCode RPL_ENDOFWHOWAS = new ResponseCode("369", "RPL_ENDOFWHOWAS");
	public static final ResponseCode RPL_LISTSTART = new ResponseCode("321", "RPL_LISTSTART");
	public static final ResponseCode RPL_LIST = new ResponseCode("322", "RPL_LIST");
	public static final ResponseCode RPL_LISTEND = new ResponseCode("323", "RPL_LISTEND");
	public static final ResponseCode RPL_UNIQOPIS = new ResponseCode("325", "RPL_UNIQOPIS");
	public static final ResponseCode RPL_CHANNELMODEIS = new ResponseCode("324", "RPL_CHANNELMODEIS");
	public static final ResponseCode RPL_NOTOPIC = new ResponseCode("331", "RPL_NOTOPIC");
	public static final ResponseCode RPL_TOPIC = new ResponseCode("332", "RPL_TOPIC");
	public static final ResponseCode RPL_INVITING = new ResponseCode("341", "RPL_INVITING");
	public static final ResponseCode RPL_SUMMONING = new ResponseCode("342", "RPL_SUMMONING");
	public static final ResponseCode RPL_INVITELIST = new ResponseCode("346", "RPL_INVITELIST");
	public static final ResponseCode RPL_ENDOFINVITELIST = new ResponseCode("347", "RPL_ENDOFINVITELIST");
	public static final ResponseCode RPL_EXCEPTLIST = new ResponseCode("348", "RPL_EXCEPTLIST");
	public static final ResponseCode RPL_ENDOFEXCEPTLIST = new ResponseCode("349", "RPL_ENDOFEXCEPTLIST");
	public static final ResponseCode RPL_VERSION = new ResponseCode("351", "RPL_VERSION");
	public static final ResponseCode RPL_WHOREPLY = new ResponseCode("352", "RPL_WHOREPLY");
	public static final ResponseCode RPL_ENDOFWHO = new ResponseCode("315", "RPL_ENDOFWHO");
	public static final ResponseCode RPL_NAMREPLY = new ResponseCode("353", "RPL_NAMREPLY");
	public static final ResponseCode RPL_ENDOFNAMES = new ResponseCode("366", "RPL_ENDOFNAMES");
	public static final ResponseCode RPL_LINKS = new ResponseCode("364", "RPL_LINKS");
	public static final ResponseCode RPL_ENDOFLINKS = new ResponseCode("365", "RPL_ENDOFLINKS");
	public static final ResponseCode RPL_BANLIST = new ResponseCode("367", "RPL_BANLIST");
	public static final ResponseCode RPL_ENDOFBANLIST = new ResponseCode("368", "RPL_ENDOFBANLIST");
	public static final ResponseCode RPL_INFO = new ResponseCode("371", "RPL_INFO");
	public static final ResponseCode RPL_ENDOFINFO = new ResponseCode("374", "RPL_ENDOFINFO");
	public static final ResponseCode RPL_MOTDSTART = new ResponseCode("375", "RPL_MOTDSTART");
	public static final ResponseCode RPL_MOTD = new ResponseCode("372", "RPL_MOTD");
	public static final ResponseCode RPL_ENDOFMOTD = new ResponseCode("376", "RPL_ENDOFMOTD");
	public static final ResponseCode RPL_YOUREOPER = new ResponseCode("381", "RPL_YOUREOPER");
	public static final ResponseCode RPL_REHASHING = new ResponseCode("382", "RPL_REHASHING");
	public static final ResponseCode RPL_YOURESERVICE = new ResponseCode("383", "RPL_YOURESERVICE");
	public static final ResponseCode RPL_TIME = new ResponseCode("391", "RPL_TIME");
	public static final ResponseCode RPL_USERSSTART = new ResponseCode("392", "RPL_USERSSTART");
	public static final ResponseCode RPL_USERS = new ResponseCode("393", "RPL_USERS");
	public static final ResponseCode RPL_ENDOFUSERS = new ResponseCode("394", "RPL_ENDOFUSERS");
	public static final ResponseCode RPL_NOUSERS = new ResponseCode("395", "RPL_NOUSERS");
	public static final ResponseCode RPL_TRACELINK = new ResponseCode("200", "RPL_TRACELINK");
	public static final ResponseCode RPL_TRACECONNECTING = new ResponseCode("201", "RPL_TRACECONNECTING");
	public static final ResponseCode RPL_TRACEHANDSHAKE = new ResponseCode("202", "RPL_TRACEHANDSHAKE");
	public static final ResponseCode RPL_TRACEUNKNOWN = new ResponseCode("203", "RPL_TRACEUNKNOWN");
	public static final ResponseCode RPL_TRACEOPERATOR = new ResponseCode("204", "RPL_TRACEOPERATOR");
	public static final ResponseCode RPL_TRACEUSER = new ResponseCode("205", "RPL_TRACEUSER");
	public static final ResponseCode RPL_TRACESERVER = new ResponseCode("206", "RPL_TRACESERVER");
	public static final ResponseCode RPL_TRACESERVICE = new ResponseCode("207", "RPL_TRACESERVICE");
	public static final ResponseCode RPL_TRACENEWTYPE = new ResponseCode("208", "RPL_TRACENEWTYPE");
	public static final ResponseCode RPL_TRACECLASS = new ResponseCode("209", "RPL_TRACECLASS");
	public static final ResponseCode RPL_TRACERECONNECT = new ResponseCode("210", "RPL_TRACERECONNECT");
	public static final ResponseCode RPL_TRACELOG = new ResponseCode("261", "RPL_TRACELOG");
	public static final ResponseCode RPL_TRACEEND = new ResponseCode("262", "RPL_TRACEEND");
	public static final ResponseCode RPL_STATSLINKINFO = new ResponseCode("211", "RPL_STATSLINKINFO");
	public static final ResponseCode RPL_STATSCOMMANDS = new ResponseCode("212", "RPL_STATSCOMMANDS");
	public static final ResponseCode RPL_ENDOFSTATS = new ResponseCode("219", "RPL_ENDOFSTATS");
	public static final ResponseCode RPL_STATSUPTIME = new ResponseCode("242", "RPL_STATSUPTIME");
	public static final ResponseCode RPL_STATSOLINE = new ResponseCode("243", "RPL_STATSOLINE");
	public static final ResponseCode RPL_UMODEIS = new ResponseCode("221", "RPL_UMODEIS");
	public static final ResponseCode RPL_SERVLIST = new ResponseCode("234", "RPL_SERVLIST");
	public static final ResponseCode RPL_SERVLISTEND = new ResponseCode("235", "RPL_SERVLISTEND");
	public static final ResponseCode RPL_LUSERCLIENT = new ResponseCode("251", "RPL_LUSERCLIENT");
	public static final ResponseCode RPL_LUSEROP = new ResponseCode("252", "RPL_LUSEROP");
	public static final ResponseCode RPL_LUSERUNKNOWN = new ResponseCode("253", "RPL_LUSERUNKNOWN");
	public static final ResponseCode RPL_LUSERCHANNELS = new ResponseCode("254", "RPL_LUSERCHANNELS");
	public static final ResponseCode RPL_LUSERME = new ResponseCode("255", "RPL_LUSERME");
	public static final ResponseCode RPL_ADMINME = new ResponseCode("256", "RPL_ADMINME");
	public static final ResponseCode RPL_ADMINLOC1 = new ResponseCode("257", "RPL_ADMINLOC1");
	public static final ResponseCode RPL_ADMINLOC2 = new ResponseCode("258", "RPL_ADMINLOC2");
	public static final ResponseCode RPL_ADMINEMAIL = new ResponseCode("259", "RPL_ADMINEMAIL");
	public static final ResponseCode RPL_TRYAGAIN = new ResponseCode("263", "RPL_TRYAGAIN");
	public static final ResponseCode ERR_NOSUCHNICK = new ResponseCode("401", "ERR_NOSUCHNICK");
	public static final ResponseCode ERR_NOSUCHSERVER = new ResponseCode("402", "ERR_NOSUCHSERVER");
	public static final ResponseCode ERR_NOSUCHCHANNEL = new ResponseCode("403", "ERR_NOSUCHCHANNEL");
	public static final ResponseCode ERR_CANNOTSENDTOCHAN = new ResponseCode("404", "ERR_CANNOTSENDTOCHAN");
	public static final ResponseCode ERR_TOOMANYCHANNELS = new ResponseCode("405", "ERR_TOOMANYCHANNELS");
	public static final ResponseCode ERR_WASNOSUCHNICK = new ResponseCode("406", "ERR_WASNOSUCHNICK");
	public static final ResponseCode ERR_TOOMANYTARGETS = new ResponseCode("407", "ERR_TOOMANYTARGETS");
	public static final ResponseCode ERR_NOSUCHSERVICE = new ResponseCode("408", "ERR_NOSUCHSERVICE");
	public static final ResponseCode ERR_NOORIGIN = new ResponseCode("409", "ERR_NOORIGIN");
	public static final ResponseCode ERR_NORECIPIENT = new ResponseCode("411", "ERR_NORECIPIENT");
	public static final ResponseCode ERR_NOTEXTTOSEND = new ResponseCode("412", "ERR_NOTEXTTOSEND");
	public static final ResponseCode ERR_NOTOPLEVEL = new ResponseCode("413", "ERR_NOTOPLEVEL");
	public static final ResponseCode ERR_WILDTOPLEVEL = new ResponseCode("414", "ERR_WILDTOPLEVEL");
	public static final ResponseCode ERR_BADMASK = new ResponseCode("415", "ERR_BADMASK");
	public static final ResponseCode ERR_UNKNOWNCOMMAND = new ResponseCode("421", "ERR_UNKNOWNCOMMAND");
	public static final ResponseCode ERR_NOMOTD = new ResponseCode("422", "ERR_NOMOTD");
	public static final ResponseCode ERR_NOADMININFO = new ResponseCode("423", "ERR_NOADMININFO");
	public static final ResponseCode ERR_FILEERROR = new ResponseCode("424", "ERR_FILEERROR");
	public static final ResponseCode ERR_NONICKNAMEGIVEN = new ResponseCode("431", "ERR_NONICKNAMEGIVEN");
	public static final ResponseCode ERR_ERRONEUSNICKNAME = new ResponseCode("432", "ERR_ERRONEUSNICKNAME");
	public static final ResponseCode ERR_NICKNAMEINUSE = new ResponseCode("433", "ERR_NICKNAMEINUSE");
	public static final ResponseCode ERR_NICKCOLLISION = new ResponseCode("436", "ERR_NICKCOLLISION");
	public static final ResponseCode ERR_UNAVAILRESOURCE = new ResponseCode("437", "ERR_UNAVAILRESOURCE");
	public static final ResponseCode ERR_USERNOTINCHANNEL = new ResponseCode("441", "ERR_USERNOTINCHANNEL");
	public static final ResponseCode ERR_NOTONCHANNEL = new ResponseCode("442", "ERR_NOTONCHANNEL");
	public static final ResponseCode ERR_USERONCHANNEL = new ResponseCode("443", "ERR_USERONCHANNEL");
	public static final ResponseCode ERR_NOLOGIN = new ResponseCode("444", "ERR_NOLOGIN");
	public static final ResponseCode ERR_SUMMONDISABLED = new ResponseCode("445", "ERR_SUMMONDISABLED");
	public static final ResponseCode ERR_USERSDISABLED = new ResponseCode("446", "ERR_USERSDISABLED");
	public static final ResponseCode ERR_NOTREGISTERED = new ResponseCode("451", "ERR_NOTREGISTERED");
	public static final ResponseCode ERR_NEEDMOREPARAMS = new ResponseCode("461", "ERR_NEEDMOREPARAMS");
	public static final ResponseCode ERR_ALREADYREGISTRED = new ResponseCode("462", "ERR_ALREADYREGISTRED");
	public static final ResponseCode ERR_NOPERMFORHOST = new ResponseCode("463", "ERR_NOPERMFORHOST");
	public static final ResponseCode ERR_PASSWDMISMATCH = new ResponseCode("464", "ERR_PASSWDMISMATCH");
	public static final ResponseCode ERR_YOUREBANNEDCREEP = new ResponseCode("465", "ERR_YOUREBANNEDCREEP");
	public static final ResponseCode ERR_YOUWILLBEBANNED = new ResponseCode("466", "ERR_YOUWILLBEBANNED");
	public static final ResponseCode ERR_KEYSET = new ResponseCode("467", "ERR_KEYSET");
	public static final ResponseCode ERR_CHANNELISFULL = new ResponseCode("471", "ERR_CHANNELISFULL");
	public static final ResponseCode ERR_UNKNOWNMODE = new ResponseCode("472", "ERR_UNKNOWNMODE");
	public static final ResponseCode ERR_INVITEONLYCHAN = new ResponseCode("473", "ERR_INVITEONLYCHAN");
	public static final ResponseCode ERR_BANNEDFROMCHAN = new ResponseCode("474", "ERR_BANNEDFROMCHAN");
	public static final ResponseCode ERR_BADCHANNELKEY = new ResponseCode("475", "ERR_BADCHANNELKEY");
	public static final ResponseCode ERR_BADCHANMASK = new ResponseCode("476", "ERR_BADCHANMASK");
	public static final ResponseCode ERR_NOCHANMODES = new ResponseCode("477", "ERR_NOCHANMODES");
	public static final ResponseCode ERR_BANLISTFULL = new ResponseCode("478", "ERR_BANLISTFULL");
	public static final ResponseCode ERR_NOPRIVILEGES = new ResponseCode("481", "ERR_NOPRIVILEGES");
	public static final ResponseCode ERR_CHANOPRIVSNEEDED = new ResponseCode("482", "ERR_CHANOPRIVSNEEDED");
	public static final ResponseCode ERR_CANTKILLSERVER = new ResponseCode("483", "ERR_CANTKILLSERVER");
	public static final ResponseCode ERR_RESTRICTED = new ResponseCode("484", "ERR_RESTRICTED");
	public static final ResponseCode ERR_UNIQOPPRIVSNEEDED = new ResponseCode("485", "ERR_UNIQOPPRIVSNEEDED");
	public static final ResponseCode ERR_NOOPERHOST = new ResponseCode("491", "ERR_NOOPERHOST");
	public static final ResponseCode ERR_UMODEUNKNOWNFLAG = new ResponseCode("501", "ERR_UMODEUNKNOWNFLAG");
	public static final ResponseCode ERR_USERSDONTMATCH = new ResponseCode("502", "ERR_USERSDONTMATCH");

	private String	mDescription = null;
	
	ResponseCode(String code, String description)
	{
		super(code);
		
		assert code != null;
		assert code.length() > 0;
		assert description != null;
		
		mDescription = description;
	}

	public static ResponseCode get(String code)
	{
		return (ResponseCode)ResponseCode.getMember(ResponseCode.class, code);
	}
	
	public String getCode()
	{
		return (String)mIdentifier;
	}
	
	public String getDescription()
	{
		return mDescription;
	}
	
	public String toString()
	{
		return mIdentifier+":"+mDescription;
	}
}

