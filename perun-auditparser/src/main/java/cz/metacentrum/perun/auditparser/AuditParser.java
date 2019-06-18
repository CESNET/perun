package cz.metacentrum.perun.auditparser;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.model.TaskResult.TaskResultStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author NorexanWORK
 */
public class AuditParser {
	private final static Logger loger = LoggerFactory.getLogger(AuditParser.class);
	static final Pattern perunBeanStartPattern = Pattern.compile("\\w+:\\[");
	static final Pattern pointyAndSquareBracket = Pattern.compile(".\\[|.\\]|[^\\\\](\\\\\\\\)*(<|>)");


	public static List<PerunBean> parseLog(String log) throws InternalErrorException {
		List<PerunBean> listPerunBeans = new ArrayList<PerunBean>();
		PerunBean perunBean = null;
		//Parse log to List of text Beanss
		List<Pair<String, Map<String, String>>> listOfTextBeans = new ArrayList<Pair<String, Map<String, String>>>();
		try {
			listOfTextBeans = beansToMap(log);
		} catch (RuntimeException ex) {
			loger.error("Message " + log + " was not correctly parsed to Map<NameOfBean,BodyOfBean>", ex);
		}
		//For every bean try to find it and create object from text
		for(Pair<String, Map<String, String>> p: listOfTextBeans) {
			try {
				perunBean = null;
				if(p.getLeft().equals("Attribute")) perunBean = createAttribute(p.getRight());
				else if(p.getLeft().equals("AttributeDefinition")) perunBean = createAttributeDefinition(p.getRight());
				else if(p.getLeft().equals("Candidate")) perunBean = createCandidate(p.getRight());
				else if(p.getLeft().equals("Destination")) perunBean = createDestination(p.getRight());
				else if(p.getLeft().equals("ExtSource")) perunBean = createExtSource(p.getRight());
				else if(p.getLeft().equals("RichFacility")) perunBean = createRichFacility(p.getRight());
				else if(p.getLeft().equals("Facility")) perunBean = createFacility(p.getRight());
				else if(p.getLeft().equals("Group")) perunBean = createGroup(p.getRight());
				else if(p.getLeft().equals("Host")) perunBean = createHost(p.getRight());
				else if(p.getLeft().equals("Member")) perunBean = createMember(p.getRight());
				else if(p.getLeft().equals("Owner")) perunBean = createOwner(p.getRight());
				else if(p.getLeft().equals("Resource")) perunBean = createResource(p.getRight());
				else if(p.getLeft().equals("RichDestination")) perunBean = createRichDestination(p.getRight());
				else if(p.getLeft().equals("RichMember")) perunBean = createRichMember(p.getRight());
				else if(p.getLeft().equals("RichUser")) perunBean = createRichUser(p.getRight());
				else if(p.getLeft().equals("RichGroup")) perunBean = createRichGroup(p.getRight());
				else if(p.getLeft().equals("RichResource")) perunBean = createRichResource(p.getRight());
				else if(p.getLeft().equals("Service")) perunBean = createService(p.getRight());
				else if(p.getLeft().equals("User")) perunBean = createUser(p.getRight());
				else if(p.getLeft().equals("UserExtSource")) perunBean = createUserExtSource(p.getRight());
				else if(p.getLeft().equals("Vo")) perunBean = createVo(p.getRight());
				else if(p.getLeft().equals("Authorship")) perunBean = createAuthorship(p.getRight());
				else if(p.getLeft().equals("ResourceTag")) perunBean = createResourceTag(p.getRight());
				else if(p.getLeft().equals("SecurityTeam")) perunBean = createSecurityTeam(p.getRight());
				else if(p.getLeft().equals("TaskResult")) perunBean = createTaskResult(p.getRight());
				else if(p.getLeft().equals("BanOnResource")) perunBean = createBanOnResource(p.getRight());
				else if(p.getLeft().equals("BanOnFacility")) perunBean = createBanOnFacility(p.getRight());
				else loger.debug("Object of this type can't be parsed cause there is no such object in parser's branches. ObjectName:" + p.getLeft());
				if(perunBean != null) listPerunBeans.add(perunBean);
			} catch (RuntimeException e) {
				loger.error("Object name " + p.getLeft() + " with attributes " + p.getRight() + " was not parsed due to fail {} ", e);
			}
		}
		return listPerunBeans;
	}


	/**
	 * This method take log message and return List of Pair where left is Name
	 * of object and right is Map of object attributes and their values. For
	 * Example "Group - {id=21, voId=21, description=Group containing VO
	 * members, name=members}"
	 *
	 * This method using for function method parseOfLog to get listOfObjects in
	 * Strings from log message
	 *
	 * @param log log message
	 * @return list of pairs name of bean to map of his attributes and values
	 */
	private static List<Pair<String, Map<String, String>>> beansToMap(String log) {
		if(log.equals("\\0")) return null;
		//First get list of beans from log message
		List<String> listOfNonparsedBeans = parseOfLog(log);
		List<Pair<String, Map<String, String>>> listOfBeans = new ArrayList<Pair<String, Map<String, String>>>();

		//For every object in list of nonparsed beans try to parse it
		for (String s : listOfNonparsedBeans) {
			//Prepare empty map and empty string for name of bean
			String nameOfBean = null;
			Map<String, String> map = new HashMap<String, String>();

			//Find, save and cut name of Bean from beanstring
			for (int i = 0; i < s.length(); i++) {
				//After correct bean name there is everytime char ":"
				if (s.charAt(i) == ':') {
					nameOfBean = s.substring(0, i);
					//Cut name of Bean + chars ":[" after it
					s = s.substring(i + 2);
					break;
				}
			}

			if(s.charAt(0) == '\\' && s.charAt(1) == '0') map = null;
			else {
				/*
				//Find name of Bean form beanString
				String nameOfBean = null;
				Matcher beanNameMatcher = beanName.matcher(s);
				beanNameMatcher.find();
				nameOfBean = s.substring(beanNameMatcher.start(), beanNameMatcher.end()-1);
				*/

				//From the rest get pair attribute of object / value of attribute and put it to the MAP
				//Helping variables to find start of attribute name, end of attribute name and the same for attribute value its always quaternion
				int startName = -1;
				int endName = -1;
				int startValue = -1;
				int endValue = -1;
				int pointyBrackets = 0;
				boolean isName = true;

				//For the rest of object String searching for attributes names and their values
				for (int i = 0; i < s.length(); i++) {
					//found first Letter when no start still exist and searching for name and save it
					if(Character.isLetter(s.charAt(i)) && startName == -1 && isName) {
						startName = i;
					} //found for last Letter symbol in beans attribute name
					else if(Character.isLetter(s.charAt(i)) && endName == -1 && isName) {
						//If there is still some symbol after this one and if it is not Letter, i save my end name position
						if (i + 1 != s.length()) {
							if(!Character.isLetter(s.charAt(i+1))) {
								endName = i;
								//when i found all name, i will be searching for value
								isName = false;
							}
						}
					} //If i found name already, trying to find nonescaped < and count it
					else if (s.charAt(i) == '<' && !isName) {
						//if its first, its my start of value position and i save it
						if (pointyBrackets == 0) {
							if (!BeansUtils.isEscaped(s, i - 1)) {
								startValue = i;
							}
						}
						//if this bracket is nonescaped so count it
						if (!BeansUtils.isEscaped(s, i - 1)) {
							pointyBrackets++;
						}
					} //If i found name already, there are some open angle breackets and is nonescaped so count this one off
					else if (pointyBrackets != 0 && s.charAt(i) == '>' && !isName) {
						//if this bracket is nonescaped so count it off
						if (!BeansUtils.isEscaped(s, i - 1)) {
							pointyBrackets--;
						}
						//if there left no brackets after counting off, so its my end bracket and i save it
						if (pointyBrackets == 0) {
							if (!BeansUtils.isEscaped(s, i - 1)) {
								endValue = i;
								isName = true;
							}
						}
					}
					//If i have already all quaternion of position i will put this attribute to the map and set helping variables to start
					if (startName != -1 && endName != -1 && startValue != -1 && endValue != -1) {
						map.put(s.substring(startName, endName + 1), s.substring(startValue + 1, endValue));
						startName = -1;
						endName = -1;
						startValue = -1;
						endValue = -1;
					}

				}
			}
			//Put name of object and map of attribute and values to the list of all objects
			Pair<String, Map<String, String>> pair = new Pair<String, Map<String, String>>();
			pair.put(nameOfBean, map);
			listOfBeans.add(pair);
		}
		return listOfBeans;
	}

	/**
	 * This method get log message and parse it to list of beans in Strings For
	 * example item in array is everytime something like -> Bean:[*] where * is
	 * anything
	 *
	 * This method using method "CutStartOfLog" for searching beans in log
	 *
	 * @param log log message
	 * @return list of object in String
	 */
	private static List<String> parseOfLog(String log) {
		List<String> results = new ArrayList<String>();
		//Load helping variables for counting brackets
		int squareBrackets = 0;
		int pointyBrackets = 0;
		boolean enableCountSquareBrackets = true;

		log = CutStartOfLog(log);
		while(log!=null) {
			//TODO this regular expresion expect, that never get char <,>,[,] or \ like first char in string log!!!
			Matcher pointyAndSquareBracketMatcher = pointyAndSquareBracket.matcher(log);
			int endOfObject = 0;
			int start = 0;
			while(pointyAndSquareBracketMatcher.find(start)) {
				if(log.charAt(pointyAndSquareBracketMatcher.end()-1)=='<') {
					enableCountSquareBrackets = false;
					pointyBrackets++;
				}else if(log.charAt(pointyAndSquareBracketMatcher.end()-1)=='>') {
					pointyBrackets--;
					//If it means that this is our searching end anglebracket, so enable counting square brackets again
					if (pointyBrackets == 0) {
						enableCountSquareBrackets = true;
					}
				}else if(enableCountSquareBrackets && log.charAt(pointyAndSquareBracketMatcher.end()-1)=='[') {
					squareBrackets++;
				}else if(enableCountSquareBrackets && log.charAt(pointyAndSquareBracketMatcher.end()-1)==']') {
					squareBrackets--;
					if (squareBrackets == 0) {
						//This is end position of searching object
						endOfObject = pointyAndSquareBracketMatcher.end();
						break;
					}
				}
				start = pointyAndSquareBracketMatcher.end()-1;
			}
			if(endOfObject != 0) {
				results.add(log.substring(0, endOfObject));
				log = log.substring(endOfObject);
			}else {
				return results;
			}
			log = CutStartOfLog(log);
		}
		return results;
	}

	/**
	 * Get log message and find first real object thx '*:[' where * is name of
	 * object cut it on the start of name example 'blabluble ,l;[0Bean:[*'
	 * return 'Bean:[*'
	 *
	 * Warning: If log content bad definition of bean, have *:[ in text without
	 * bean inside, parsing failed
	 *
	 * @param log log message
	 * @return Message cutted to Start of first real bean in log
	 */
	private static String CutStartOfLog(String log) {

		if(log == null) return null;
		Matcher perunBeanStartMatcher = perunBeanStartPattern.matcher(log);
		if(perunBeanStartMatcher.find()) {
			log = log.substring(perunBeanStartMatcher.start());
		} else return null;
		return log;
	}

	//--------------------------------------------------------------------------
	//--------------------------BEANS CREATORS---------------------------------
	private static User createUser(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		User user = new User();
		user.setId(Integer.valueOf(beanAttr.get("id")));
		user.setTitleBefore(BeansUtils.eraseEscaping(beanAttr.get("titleBefore")));
		user.setTitleAfter(BeansUtils.eraseEscaping(beanAttr.get("titleAfter")));
		user.setFirstName(BeansUtils.eraseEscaping(beanAttr.get("firstName")));
		user.setLastName(BeansUtils.eraseEscaping(beanAttr.get("lastName")));
		user.setMiddleName(BeansUtils.eraseEscaping(beanAttr.get("middleName")));
		user.setServiceUser(Boolean.valueOf(beanAttr.get("serviceAccount")));
		user.setSponsoredUser(Boolean.valueOf(beanAttr.get("sponsoredAccount")));
		return user;
	}

	private static Attribute createAttribute(Map<String, String> beanAttr) throws InternalErrorException {
		if(beanAttr==null) return null;
		Attribute attribute = new Attribute();
		attribute.setId(Integer.valueOf(beanAttr.get("id")));
		attribute.setFriendlyName(BeansUtils.eraseEscaping(beanAttr.get("friendlyName")));
		attribute.setNamespace(BeansUtils.eraseEscaping(beanAttr.get("namespace")));
		attribute.setType(BeansUtils.eraseEscaping(beanAttr.get("type")));
		attribute.setValue(BeansUtils.stringToAttributeValue(BeansUtils.eraseEscaping(beanAttr.get("value")), attribute.getType()));
		attribute.setUnique(Boolean.valueOf(beanAttr.get("unique")));
		return attribute;
	}

	private static AttributeDefinition createAttributeDefinition(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		AttributeDefinition attributeDefinition = new AttributeDefinition();
		attributeDefinition.setId(Integer.valueOf(beanAttr.get("id")));
		attributeDefinition.setFriendlyName(BeansUtils.eraseEscaping(beanAttr.get("friendlyName")));
		attributeDefinition.setNamespace(BeansUtils.eraseEscaping(beanAttr.get("namespace")));
		attributeDefinition.setType(BeansUtils.eraseEscaping(beanAttr.get("type")));
		attributeDefinition.setUnique(Boolean.valueOf(beanAttr.get("unique")));
		return attributeDefinition;
	}

	private static Candidate createCandidate(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		Candidate candidate = new Candidate();
		candidate.setAttributes(BeansUtils.deserializeStringToMap(beanAttr.get("attributes")));
		//Parse and get ExtSource
		UserExtSource userExtSource;
		if(beanAttr.get("userExtSource").equals("\\0")) userExtSource = null;
		else {
			List<Pair<String, Map<String, String>>> userExtSourceMap = beansToMap(beanAttr.get("userExtSource"));
			userExtSource = createUserExtSource(userExtSourceMap.get(0).getRight());
		}
		candidate.setUserExtSource(userExtSource);
		//Parse and get list of UserExtSources
		List<UserExtSource> additionalUserExtSources = new ArrayList<UserExtSource>();
		if(beanAttr.get("additionalUserExtSources").equals("\\0")) additionalUserExtSources = null;
		else {
			List<Pair<String, Map<String, String>>> userExtSourcesList = beansToMap(beanAttr.get("additionalUserExtSources"));
			for(Pair<String, Map<String, String>> p: userExtSourcesList) {
				userExtSource = createUserExtSource(p.getRight());
				additionalUserExtSources.add(userExtSource);
			}
		}
		candidate.setAdditionalUserExtSources(additionalUserExtSources);
		return candidate;
	}

	private static Destination createDestination(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		Destination destination = new Destination();
		destination.setId(Integer.valueOf(beanAttr.get("id")));
		destination.setDestination(BeansUtils.eraseEscaping(beanAttr.get("destination")));
		destination.setType(BeansUtils.eraseEscaping(beanAttr.get("type")));
		destination.setPropagationType(BeansUtils.eraseEscaping(beanAttr.get("propagationtype")));
		return destination;
	}

	private static ExtSource createExtSource(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		ExtSource extSource = new ExtSource();
		extSource.setId(Integer.valueOf(beanAttr.get("id")));
		extSource.setName(BeansUtils.eraseEscaping(beanAttr.get("name")));
		extSource.setType(BeansUtils.eraseEscaping(beanAttr.get("type")));
		return extSource;
	}

	private static Facility createFacility(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		Facility facility = new Facility();
		facility.setId(Integer.valueOf(beanAttr.get("id")));
		facility.setName(BeansUtils.eraseEscaping(beanAttr.get("name")));
		facility.setDescription(BeansUtils.eraseEscaping(beanAttr.get("description")));
		return facility;
	}

	private static Group createGroup(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		Group group = new Group();
		if(beanAttr.get("parentGroupId").equals("\\0")) group.setParentGroupId(null);
		else group.setParentGroupId(Integer.valueOf(beanAttr.get("parentGroupId")));
		group.setId(Integer.valueOf(beanAttr.get("id")));
		group.setName(BeansUtils.eraseEscaping(beanAttr.get("name")));
		group.setDescription(BeansUtils.eraseEscaping(beanAttr.get("description")));
		group.setVoId(Integer.valueOf(beanAttr.get("voId")));
		return group;
	}

	private static Host createHost(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		Host host = new Host();
		host.setId(Integer.valueOf(beanAttr.get("id")));
		host.setHostname(BeansUtils.eraseEscaping(beanAttr.get("hostname")));
		return host;
	}

	private static Member createMember(Map<String, String> beanAttr) throws InternalErrorException {
		if(beanAttr==null) return null;
		Member member = new Member();
		member.setId(Integer.valueOf(beanAttr.get("id")));
		member.setUserId(Integer.valueOf(beanAttr.get("userId")));
		member.setVoId(Integer.valueOf(beanAttr.get("voId")));
		member.setStatus(BeansUtils.eraseEscaping(beanAttr.get("status")));
		member.setMembershipType(BeansUtils.eraseEscaping(beanAttr.get("type")));
		member.setSourceGroupId(beanAttr.get("sourceGroupId").equals("\\0") ? null : Integer.valueOf(beanAttr.get("sourceGroupId")));
		try {
			member.setSuspendedTo(beanAttr.get("suspendedTo").equals("\\0") ? null : BeansUtils.getDateFormatter().parse(BeansUtils.eraseEscaping(beanAttr.get("suspendedTo"))));
		} catch (ParseException ex) {
			throw new InternalErrorException("Can't parse date for member suspendedTo from the string representation!" , ex);
		}
		member.setSponsored(Boolean.valueOf(beanAttr.get("sponsored")));
		return member;
	}

	private static Owner createOwner(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		Owner owner = new Owner();
		owner.setId(Integer.valueOf(beanAttr.get("id")));
		owner.setName(BeansUtils.eraseEscaping(beanAttr.get("name")));
		owner.setContact(BeansUtils.eraseEscaping(beanAttr.get("contact")));
		owner.setTypeByString(BeansUtils.eraseEscaping(beanAttr.get("type")));
		return owner;
	}

	private static Resource createResource(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		Resource resource = new Resource();
		resource.setId(Integer.valueOf(beanAttr.get("id")));
		resource.setVoId(Integer.valueOf(beanAttr.get("voId")));
		resource.setFacilityId(Integer.valueOf(beanAttr.get("facilityId")));
		resource.setName(BeansUtils.eraseEscaping(beanAttr.get("name")));
		resource.setDescription(BeansUtils.eraseEscaping(beanAttr.get("description")));
		return resource;
	}

	private static Service createService(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		Service service = new Service();
		service.setId(Integer.valueOf(beanAttr.get("id")));
		service.setName(BeansUtils.eraseEscaping(beanAttr.get("name")));
		service.setDescription(BeansUtils.eraseEscaping(beanAttr.get("description")));
		service.setDelay(Integer.valueOf(beanAttr.get("delay")).intValue());
		service.setRecurrence(Integer.valueOf(beanAttr.get("recurrence")).intValue());
		service.setEnabled(Boolean.valueOf(beanAttr.get("enabled")).booleanValue());
		service.setScript(BeansUtils.eraseEscaping(beanAttr.get("script")));
		return service;
	}

	private static UserExtSource createUserExtSource(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		UserExtSource userExtSource = new UserExtSource();
		userExtSource.setId(Integer.valueOf(beanAttr.get("id")));
		userExtSource.setLoa(Integer.valueOf(beanAttr.get("loa")));
		userExtSource.setLogin(BeansUtils.eraseEscaping(beanAttr.get("login")));
		//Add userId if exists
		if(beanAttr.get("userId") != null) userExtSource.setUserId(Integer.valueOf(beanAttr.get("userId")));
		//Parse and get ExtSource
		ExtSource extSource;
		if(beanAttr.get("source").equals("\\0")) extSource = null;
		else {
			List<Pair<String, Map<String, String>>> extSourceList = beansToMap(beanAttr.get("source"));
			extSource = createExtSource(extSourceList.get(0).getRight());
		}
		userExtSource.setLastAccess(BeansUtils.eraseEscaping(beanAttr.get("lastAccess")));
		userExtSource.setExtSource(extSource);
		return userExtSource;
	}

	private static Vo createVo(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		Vo vo = new Vo();
		vo.setId(Integer.valueOf(beanAttr.get("id")));
		vo.setName(BeansUtils.eraseEscaping(beanAttr.get("name")));
		vo.setShortName(BeansUtils.eraseEscaping(beanAttr.get("shortName")));
		return vo;
	}

	private static Authorship createAuthorship(Map<String, String> beanAttr) throws InternalErrorException {
		if(beanAttr==null) return null;
		Authorship authorship = new Authorship();
		authorship.setId(Integer.valueOf(beanAttr.get("id")));
		authorship.setPublicationId(Integer.valueOf(beanAttr.get("publicationId")));
		authorship.setUserId(Integer.valueOf(beanAttr.get("userId")));
		authorship.setCreatedBy(BeansUtils.eraseEscaping(beanAttr.get("createdBy")));
		authorship.setCreatedByUid((beanAttr.get("createdByUid").equals("\\0")) ? null : Integer.valueOf(beanAttr.get("createdByUid")));
		if(BeansUtils.eraseEscaping(beanAttr.get("createdDate"))== null) authorship.setCreatedDate(null);
		else {
			Date date;
			try {
				date = BeansUtils.getDateFormatter().parse(BeansUtils.eraseEscaping(beanAttr.get("createdDate")));
			} catch (ParseException ex) {
				throw new InternalErrorException("Error when date was parsing from String to Date.", ex);
			}
			authorship.setCreatedDate(date);
		}
		return authorship;
	}

	private static ResourceTag createResourceTag(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		ResourceTag resourceTag = new ResourceTag();
		resourceTag.setId(Integer.valueOf(beanAttr.get("id")));
		resourceTag.setVoId(Integer.valueOf(beanAttr.get("voId")));
		resourceTag.setTagName(BeansUtils.eraseEscaping(beanAttr.get("tagName")));
		return resourceTag;
	}

	private static SecurityTeam createSecurityTeam(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		SecurityTeam securityTeam = new SecurityTeam();
		securityTeam.setId(Integer.valueOf(beanAttr.get("id")));
		securityTeam.setName(BeansUtils.eraseEscaping(beanAttr.get("name")));
		securityTeam.setDescription(BeansUtils.eraseEscaping(beanAttr.get("description")));
		return securityTeam;
	}

	private static TaskResult createTaskResult(Map<String, String> beanAttr) throws InternalErrorException {
		if (beanAttr == null) return null;
		TaskResult taskResult = new TaskResult();
		taskResult.setId(Integer.valueOf(beanAttr.get("id")));
		taskResult.setTaskId(Integer.valueOf(beanAttr.get("taskId")));
		taskResult.setDestinationId(Integer.valueOf(beanAttr.get("destinationId")));
		String errorMessage;
		if (beanAttr.get("errorMessage").equals("\\0")) errorMessage = null;
		else {
			errorMessage = BeansUtils.eraseEscaping(beanAttr.get("errorMessage"));
		}
		taskResult.setErrorMessage(errorMessage);
		String standardMessage;
		if (beanAttr.get("standardMessage").equals("\\0")) standardMessage = null;
		else {
			standardMessage = BeansUtils.eraseEscaping(beanAttr.get("standardMessage"));
		}
		taskResult.setStandardMessage(standardMessage);
		taskResult.setReturnCode(Integer.valueOf(beanAttr.get("returnCode")));
		try {
			taskResult.setTimestamp(BeansUtils.getDateFormatter().parse(BeansUtils.eraseEscaping(beanAttr.get("timestamp"))));
		} catch (ParseException e) {
			throw new InternalErrorException("Error when date was parsing from String to Date.", e);
		}
		String status = BeansUtils.eraseEscaping(beanAttr.get("status"));
		TaskResultStatus st;
		if (status.equals("\\0")) st = null;
		else {
			if (status.equals("DENIED")) st = TaskResultStatus.DENIED;
			else if (status.equals("DONE")) st = TaskResultStatus.DONE;
			else if (status.equals("ERROR")) st = TaskResultStatus.ERROR;
			else if (status.equals("FATAL_ERROR")) st = TaskResultStatus.FATAL_ERROR;
			else if (status.equals("WARN")) st = TaskResultStatus.WARN;
			else st = null;
		}
		taskResult.setStatus(st);
		Service service;
		if (beanAttr.get("service").equals("\\0")) service = null;
		else {
			List<Pair<String, Map<String, String>>> serviceList = beansToMap(beanAttr.get("service"));
			if (serviceList.size() > 0) {
				service = createService(serviceList.get(0).getRight());
			} else service = null;
		}
		taskResult.setService(service);

		return taskResult;

	}

	private static Ban createBanOnResource(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		BanOnResource banOnResource = new BanOnResource();
		banOnResource.setId(Integer.valueOf(beanAttr.get("id")));
		banOnResource.setMemberId(Integer.valueOf(beanAttr.get("memberId")));
		banOnResource.setResourceId(Integer.valueOf(beanAttr.get("resourceId")));
		banOnResource.setDescription(BeansUtils.eraseEscaping(beanAttr.get("description")));
		Date validityTo;
		if(beanAttr.get("validityTo").equals("\\0")) validityTo = null;
		else validityTo = new Date(Long.valueOf(beanAttr.get("validityTo")));
		banOnResource.setValidityTo(validityTo);
		return banOnResource;
	}

	private static Ban createBanOnFacility(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		BanOnFacility banOnFacility = new BanOnFacility();
		banOnFacility.setId(Integer.valueOf(beanAttr.get("id")));
		banOnFacility.setUserId(Integer.valueOf(beanAttr.get("userId")));
		banOnFacility.setFacilityId(Integer.valueOf(beanAttr.get("facilityId")));
		banOnFacility.setDescription(BeansUtils.eraseEscaping(beanAttr.get("description")));
		Date validityTo;
		if(beanAttr.get("validityTo").equals("\\0")) validityTo = null;
		else validityTo = new Date(Long.valueOf(beanAttr.get("validityTo")));
		banOnFacility.setValidityTo(validityTo);
		return banOnFacility;
	}

	//--------------------------------------------------------------------------
	//------------------------RICH BEANS CREATORS-------------------------------

	private static RichDestination createRichDestination(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		RichDestination richDestination = new RichDestination();
		richDestination.setId(Integer.valueOf(beanAttr.get("id")));
		richDestination.setDestination(BeansUtils.eraseEscaping(beanAttr.get("destination")));
		richDestination.setType(BeansUtils.eraseEscaping(beanAttr.get("type")));
		//Parse and get service
		Service service;
		if(beanAttr.get("service").equals("\\0")) service = null;
		else {
			List<Pair<String, Map<String, String>>> serviceList = beansToMap(beanAttr.get("service"));
			service = createService(serviceList.get(0).getRight());
		}
		richDestination.setService(service);
		//Parse and get Facility
		Facility facility;
		if(beanAttr.get("facility").equals("\\0")) facility = null;
		else {
			List<Pair<String, Map<String, String>>> facilityList = beansToMap(beanAttr.get("facility"));
			facility = createFacility(facilityList.get(0).getRight());
		}
		richDestination.setFacility(facility);
		return richDestination;
	}

	private static RichMember createRichMember(Map<String, String> beanAttr) throws InternalErrorException {
		if(beanAttr==null) return null;
		Member member = createMember(beanAttr);
		User user;
		if(beansToMap(beanAttr.get("user")) == null) user = null;
		else user = createUser(beansToMap(beanAttr.get("user")).get(0).getRight());
		//Parse and get list of UserExtSources
		List<UserExtSource> userExtSources = new ArrayList<UserExtSource>();
		if(beanAttr.get("userExtSources").equals("\\0")) userExtSources = null;
		else {
			List<Pair<String, Map<String, String>>> userExtSourcesList = beansToMap(beanAttr.get("userExtSources"));
			for(Pair<String, Map<String, String>> p: userExtSourcesList) {
				UserExtSource userExtSource = createUserExtSource(p.getRight());
				userExtSources.add(userExtSource);
			}
		}
		//Parse and get list of UserAttributes
		List<Attribute> userAttributes = new ArrayList<Attribute>();
		if(beanAttr.get("userAttributes").equals("\\0")) userAttributes = null;
		else {
			List<Pair<String, Map<String, String>>> userAttributesList = beansToMap(beanAttr.get("userAttributes"));
			for(Pair<String, Map<String, String>> p: userAttributesList) {
				Attribute attribute = createAttribute(p.getRight());
				userAttributes.add(attribute);
			}
		}
		//Parse and get list of MemberAttributes
		List<Attribute> memberAttributes = new ArrayList<Attribute>();
		if(beanAttr.get("memberAttributes").equals("\\0")) memberAttributes = null;
		else {
			List<Pair<String, Map<String, String>>> memberAttributesList = beansToMap(beanAttr.get("memberAttributes"));
			for(Pair<String, Map<String, String>> p: memberAttributesList) {
				Attribute attribute = createAttribute(p.getRight());
				memberAttributes.add(attribute);
			}
		}
		RichMember richMember = new RichMember(user, member, userExtSources, userAttributes, memberAttributes);
		return richMember;
	}

	private static RichUser createRichUser(Map<String, String> beanAttr) throws InternalErrorException {
		if(beanAttr==null) return null;
		User user = createUser(beanAttr);
		//Parse and get list of UserExtSources
		List<UserExtSource> userExtSources = new ArrayList<UserExtSource>();
		if(beanAttr.get("userExtSources").equals("\\0")) userExtSources = null;
		else {
			List<Pair<String, Map<String, String>>> userExtSourcesList = beansToMap(beanAttr.get("userExtSources"));
			for(Pair<String, Map<String, String>> p: userExtSourcesList) {
				UserExtSource userExtSource = createUserExtSource(p.getRight());
				userExtSources.add(userExtSource);
			}
		}
		//Parse and get list of UserAttributes
		List<Attribute> userAttributes = new ArrayList<Attribute>();
		if(beanAttr.get("userAttributes").equals("\\0")) userAttributes = null;
		else {
			List<Pair<String, Map<String, String>>> userAttributesList = beansToMap(beanAttr.get("userAttributes"));
			for(Pair<String, Map<String, String>> p: userAttributesList) {
				Attribute attribute = createAttribute(p.getRight());
				userAttributes.add(attribute);
			}
		}
		RichUser richUser = new RichUser(user, userExtSources, userAttributes);
		return richUser;
	}

	private static RichGroup createRichGroup(Map<String, String> beanAttr) throws InternalErrorException {
		if(beanAttr==null) return null;
		Group group = createGroup(beanAttr);

		//Parse and get list of GroupAttributes
		List<Attribute> groupAttributes = new ArrayList<Attribute>();
		if(beanAttr.get("groupAttributes").equals("\\0")) groupAttributes = null;
		else {
			List<Pair<String, Map<String, String>>> groupAttributesList = beansToMap(beanAttr.get("groupAttributes"));
			for(Pair<String, Map<String, String>> p: groupAttributesList) {
				Attribute attribute = createAttribute(p.getRight());
				groupAttributes.add(attribute);
			}
		}
		RichGroup richGroup = new RichGroup(group, groupAttributes);
		return richGroup;
	}

	private static RichFacility createRichFacility(Map<String, String> beanAttr) throws InternalErrorException {
		if(beanAttr==null) return null;
		Facility facility = createFacility(beanAttr);
		//Parse and get list of Owners
		List<Owner> facilityOwners = new ArrayList<Owner>();
		if(beanAttr.get("facilityOwners").equals("\\0")) facilityOwners = null;
		else {
			List<Pair<String, Map<String, String>>> facilityOwnersList = beansToMap(beanAttr.get("facilityOwners"));
			for(Pair<String, Map<String, String>> p: facilityOwnersList) {
				Owner owner = createOwner(p.getRight());
				facilityOwners.add(owner);
			}
		}
		RichFacility richFacility = new RichFacility(facility, facilityOwners);
		return richFacility;
	}

	private static RichResource createRichResource(Map<String, String> beanAttr) {
		if(beanAttr==null) return null;
		RichResource richResource = new RichResource();
		richResource.setId(Integer.valueOf(beanAttr.get("id")));
		richResource.setVoId(Integer.valueOf(beanAttr.get("voId")));
		richResource.setFacilityId(Integer.valueOf(beanAttr.get("facilityId")));
		richResource.setName(BeansUtils.eraseEscaping(beanAttr.get("name")));
		richResource.setDescription(BeansUtils.eraseEscaping(beanAttr.get("description")));
		//Parse and get Vo
		Vo vo;
		if(beanAttr.get("vo").equals("\\0")) vo = null;
		else {
			List<Pair<String, Map<String, String>>> voMap = beansToMap(beanAttr.get("vo"));
			vo = createVo(voMap.get(0).getRight());
		}
		richResource.setVo(vo);
		//Parse and get Facility
		Facility facility;
		if(beanAttr.get("facility").equals("\\0")) facility = null;
		else {
			List<Pair<String, Map<String, String>>> facilityMap = beansToMap(beanAttr.get("facility"));
			facility = createFacility(facilityMap.get(0).getRight());
		}
		richResource.setFacility(facility);
		//Parse and get List of ResourceTags
		List<ResourceTag> tags = new ArrayList<ResourceTag>();
		if(beanAttr.get("resourceTags").equals("\\0")) tags = null;
		else {
			List<Pair<String, Map<String, String>>> tagsList = beansToMap(beanAttr.get("resourceTags"));
			for(Pair<String, Map<String, String>> p: tagsList) {
				ResourceTag tag = createResourceTag(p.getRight());
				tags.add(tag);
			}
		}
		richResource.setResourceTags(tags);

		return richResource;
	}

}
