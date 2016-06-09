package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.voot.comparators.vootgroupcomparator.VOOTGroupDefaultDescComparator;
import cz.metacentrum.perun.voot.comparators.vootgroupcomparator.VOOTGroupDescriptionAscComparator;
import cz.metacentrum.perun.voot.comparators.vootgroupcomparator.VOOTGroupDescriptionDescComparator;
import cz.metacentrum.perun.voot.comparators.vootgroupcomparator.VOOTGroupIdAscComparator;
import cz.metacentrum.perun.voot.comparators.vootgroupcomparator.VOOTGroupIdDescComparator;
import cz.metacentrum.perun.voot.comparators.vootgroupcomparator.VOOTGroupMembershipRoleAscComparator;
import cz.metacentrum.perun.voot.comparators.vootgroupcomparator.VOOTGroupMembershipRoleDescComparator;
import cz.metacentrum.perun.voot.comparators.vootgroupcomparator.VOOTGroupTitleAscComparator;
import cz.metacentrum.perun.voot.comparators.vootgroupcomparator.VOOTGroupTitleDescComparator;
import cz.metacentrum.perun.voot.comparators.vootmembercomparator.VOOTMemberDefaultDescComparator;
import cz.metacentrum.perun.voot.comparators.vootmembercomparator.VOOTMemberDisplayNameAscComparator;
import cz.metacentrum.perun.voot.comparators.vootmembercomparator.VOOTMemberDisplayNameDescComparator;
import cz.metacentrum.perun.voot.comparators.vootmembercomparator.VOOTMemberIdAscComparator;
import cz.metacentrum.perun.voot.comparators.vootmembercomparator.VOOTMemberIdDescComparator;
import cz.metacentrum.perun.voot.comparators.vootmembercomparator.VOOTMemberMembershipRoleAscComparator;
import cz.metacentrum.perun.voot.comparators.vootmembercomparator.VOOTMemberMembershipRoleDescComparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * VOOT protocol
 *
 * VOOT(Virtual Organization Orthogonal Technology) is simple protocol for cross-domain read only access to information about user's group membership
 * and members of specific group, in which user is member of.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VOOT {

	@Autowired
	private PerunBl perun;

	private PerunSession session;
	private PerunPrincipal perunPrincipal;
	private User user;

	private Map<String, String> parameters;

	private Response response;

	//protocol calls
	//voot service endpoints
	private static final String GET_PERSON_PATTERN = "(?i)people/@me(|/)";
	private static final String GET_GROUP_MEMBERS_PATTERN = "(?i)people/@me/[A-Za-z0-9_-]+(:[A-Za-z0-9_-]+)+(|/)";
	private static final String IS_MEMBER_OF_PATTERN = "(?i)groups/@me(|/)";

	//request parameters field name
	private static final String FORMAT = "format";
	private static final String START_INDEX = "startIndex";
	private static final String COUNT = "count";
	private static final String FILTER_BY = "filterBy";
	private static final String FILTER_OP = "filterOp";
	private static final String FILTER_VALUE = "filterValue";
	private static final String SORT_BY = "sortBy";
	private static final String SORT_ORDER = "sortOrder";

	//request parameters values

	//only JSON data format is supported
	private String formatValue = JSON;
	private static final String JSON = "json";

	private Integer startIndexValue = 0;
	private Integer countValue = 0;
	private String filterByValue;
	private String filterOpValue;

	//choose value for filterOp, default value is contains
	private static final String CONTAINS = "contains";
	private static final String EQUALS = "equals";
	private static final String STARTS_WITH= "startsWith";
	private static final String PRESENT= "present";

	private String filterValueValue;
	private String sortByValue;

	//default value for sortOrder is ascending
	private String sortOrderValue;

	//choose value for sortOrder
	private static final String ASCENDING = "ascending";
	private static final String DESCENDING = "descending";

	/**
	 * Protocol call is consist of used method, which is value of path, and request parameters determine in restOfPath.
	 *
	 * Allowed method:
	 * 'people/@me' Request for personal information about current user.
	 * 'groups/@me' Request for groups that current user is member of.
	 * 'groups/@me/{group_id}' Request for members of specific group, in which current user is member of.
	 *
	 * e.g. 'groups/@me/vo1:group1'
	 *
	 * Allowed parameters request:
	 * @see http://opensocial-resources.googlecode.com/svn/spec/2.0.1/Core-API-Server.xml#rfc.section.6
	 *
	 * In section 6.2 Collection request parameters are all available and it is possible to use parameter SortBy.
	 * Value of SortBy is singular attribute of object, e.g. for group 'title' or for member 'displayName'.
	 * This value is used to sort by this. If value is invalid items are sorting by default value.
	 *
	 * @param session           session of Perun
	 * @param path              protocol call
	 * @param restOfPath        request parameters
	 * @return                  response of collection or info about person
	 * @throws VOOTException    if can not return correct response
	 */
	public Object process(PerunSession session, String path, String restOfPath) throws VOOTException {

		this.session = session;
		perun = (PerunBl) session.getPerun();
		perunPrincipal = session.getPerunPrincipal();
		user = session.getPerunPrincipal().getUser();
		if(user == null) throw new VOOTException("invalid_user");

		response = new Response();
		this.parameters = parseParameters(restOfPath);
		setParamters();

		if(checkMethod(path, GET_PERSON_PATTERN)) {
			VOOTPerson vootPerson = new VOOTPerson(user, getEmails(user));
			return vootPerson;
		}

		if(checkMethod(path, GET_GROUP_MEMBERS_PATTERN)){
			String[] pathArray = path.split("(?i)people/@me/");
			String groupName = pathArray[1];
			groupName.replace('/',' ');
			Group group = getGroupByName(groupName);

			List<Member> members = getGroupMembers(group);
			VOOTMember[] vootMembers = createVOOTMembers(members, group);
			vootMembers = filterVOOTMembers(vootMembers);
			sortVOOTMembers(vootMembers);
			vootMembers = (VOOTMember[]) limitResult(vootMembers, response);
			response.setEntry(vootMembers);

			return response;
		}

		if(checkMethod(path, IS_MEMBER_OF_PATTERN)){
			List<Group> groups = isMemberOf();
			VOOTGroup[] vootGroups = createVOOTGroups(groups);
			vootGroups = filterVOOTGroups(vootGroups);
			vootGroups = sortVOOTGroups(vootGroups);
			vootGroups = (VOOTGroup[]) limitResult(vootGroups, response);

			response.setEntry(vootGroups);

			return response;
		}

		throw new VOOTException("invalid_request");

	}

	/**
	 * Limit array of items, that could be presented to end-user. This method sets startIndex, totalResults and itemsPerPage of reponse by request parameters.
	 * If indexes of response(e.g, the number of count is higher than number of available items or startIndex is less than zero),
	 * then method returns original array of items.
	 *
	 * @param items   array of items, that could be limited
	 * @param response  response, in which are set indexes.
	 * @return          array of limited items or original array of items
	 */
	private Object[] limitResult(Object[] items, Response response){

		//total items, that are available without limiting. Not depending on startIndex or count
		response.setTotalResults(items.length);

		//count value, when is not defined by user
		if(countValue.equals(Integer.valueOf(0))) countValue = Integer.valueOf(items.length) - startIndexValue;

		try{
			items = Arrays.copyOfRange(items, startIndexValue, startIndexValue + countValue);
			response.setStartIndex(startIndexValue);
		}catch(Exception ex){
			//return original array of items
			//set startIndex on zero available for all items
			response.setStartIndex(Integer.valueOf(0));
		}

		//set itemPerPage, number of limited items
		response.setItemsPerPage(items.length);

		return items;
	}

	/**
	 * This method creates groups used by VOOT, that are represented to end-user. They are created from groups by provider.
	 *
	 * @param groups            groups from provider
	 * @return                  array of groups, that are represented to end-user
	 * @throws VOOTException    if can not read groups by provider
	 */
	private VOOTGroup[] createVOOTGroups(List<Group> groups) throws VOOTException{

		GroupsManagerBl groupManager = perun.getGroupsManagerBl();

		VOOTGroup[] vootGroups = new VOOTGroup[groups.size()];

		int i=0;

		for(Group group : groups){

			String vootMembership;

			try{
				if(groupManager.getAdmins(session, group).contains(user)){
					vootMembership = "admin";
				}
				else{
					vootMembership = "member";
				}

				vootGroups[i] = new VOOTGroup (group, groupManager.getVo(session, group).getShortName(), vootMembership);
				i++;
			}catch(InternalErrorException ex){
				throw new VOOTException("internal_server_error");
			}
		}

		return vootGroups;
	}

	/**
	 * This method creates members used by VOOT, that are represented to end-user. They are created from members by provider and membership role
	 * is set by relationship of member and specific group.
	 *
	 * @param members           members by provider
	 * @param group             specific group
	 * @return                  array of members, that are represented to end-use
	 * @throws VOOTException    if can not read groups by provider
	 */
	private VOOTMember[] createVOOTMembers(List<Member> members, Group group) throws VOOTException{

		VOOTMember[] vootMembers = new VOOTMember[members.size()];

		int i=0;

		for(Member member : members){

			User userOfMember = new User();

			try{
				userOfMember = perun.getUsersManagerBl().getUserByMember(session, member);
			}catch(InternalErrorException ex){
				throw new VOOTException("internal_server_error");
			}

			Email[] emails = getEmails(userOfMember);

			String vootMembership = new String();

			try{
				if(perun.getGroupsManagerBl().getAdmins(session, group).contains(userOfMember)){
					vootMembership = "admin";
				}else{
					vootMembership = "member";
				}
			}catch(InternalErrorException ex){
				throw new VOOTException("internal_server_error");
			}

			vootMembers[i] = new VOOTMember(userOfMember, emails, vootMembership);
			i++;
		}

		return vootMembers;
	}

	/**
	 * Return email addresses of specific user. Now is only preferred mail required. If user has not email, then is returned empty array.
	 *
	 * @param user              specific user
	 * @return                  emails of user, if user has not emails is returned empty array
	 * @throws VOOTException    if cannot read emails of user
	 */
	private Email[] getEmails(User user) throws VOOTException{

		//preferred mail
		Email[] emails = new Email[1];

		Attribute preferredEmailAttribute = new Attribute();

		try{
			preferredEmailAttribute = perun.getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":preferredMail");
			if(preferredEmailAttribute.getValue() != null){
				Email email = new Email();
				email.setType("mail");
				email.setValue((String) preferredEmailAttribute.getValue());
				emails[0] = email;
			}else{
				emails = null;
			}
		}catch(Exception ex){
			emails = null;
		}

		return emails;
	}

	/**
	 * Control of correct form of protocol call. The method is not case sensitive.
	 *
	 * @param path             used method
	 * @param allowedMethod    allowed method of VOOT
	 * @return                 true, if the method is allowed, false otherwise
	 */
	private boolean checkMethod(String path, String allowedMethod){
		Matcher matcher = null;
		Pattern pattern = null;
		pattern = Pattern.compile(allowedMethod);
		matcher = pattern.matcher(path);
		return matcher.matches();
	}

	/**
	 * Return group by name, which is consist of short name of VO, short name of parents group and short name of current group, e.g. 'vo1:group1:group2'.
	 *
	 * @param name              name of group, e.g. 'vo1:group1:group2'
	 * @return                  group
	 * @throws VOOTException    if can not read group
	 */
	private Group getGroupByName(String name) throws VOOTException{

		String voName = name.split(":")[0];

		Vo vo = null;

		try{
			vo = perun.getVosManagerBl().getVoByShortName(session, voName);
		}catch(InternalErrorException ex){
			throw new VOOTException("internal_server_error");
		}catch(VoNotExistsException ex){
			throw new VOOTException("internal_server_error", "vo not exists");
		}

		Group group = null;

		try{
			group = perun.getGroupsManagerBl().getGroupByName(session, vo, name.substring(name.indexOf(":")+1, name.length()));
		}catch(GroupNotExistsException ex){
			throw new VOOTException("internal_server_error", "group not exists");
		}catch(InternalErrorException ex){
			throw new VOOTException("internal_server_error");
		}

		return group;
	}

	/**
	 * Return all members of specific group. .
	 *
	 * @param group    group    specific group
	 * @return         group    members of group
	 * @throws VOOTException    cannot read members of group
	 */
	private List<Member> getGroupMembers(Group group) throws VOOTException {
		List<Member> members = new ArrayList<Member>();

		try {

			if (!perun.getGroupsManagerBl().isUserMemberOfGroup(session, user, group)) {
				// if not group member, check authorization in Entry
				members = perun.getGroupsManager().getGroupMembers(session, group);
			} else {
				members = perun.getGroupsManagerBl().getGroupMembers(session, group);
			}

		} catch (InternalErrorException ex){
			throw new VOOTException("internal_server_error");
		} catch (PrivilegeException ex) {
			throw new VOOTException("insufficient_privileges");
		} catch (GroupNotExistsException ex) {
			throw new VOOTException("group_not_exists");
		}

		return members;
	}

	/**
	 * Return groups that user is member of.
	 *
	 * @return                  groups that user is member of
	 * @throws VOOTException    if the can not read groups of user
	 */
	private List<Group> isMemberOf() throws VOOTException{

		List<Group> groups = new ArrayList<Group>();
		List<Vo> vos = new ArrayList<Vo>();

		try{
			vos.addAll(perun.getUsersManagerBl().getVosWhereUserIsMember(session, user));
		}catch(InternalErrorException ex){
			throw new VOOTException("internal_server_error");
		}

		try{
			for (Vo vo : vos) {
				Member member = perun.getMembersManagerBl().getMemberByUser(session, vo, user);
				groups.addAll(perun.getGroupsManagerBl().getAllMemberGroups(session, member));
			}
		}catch(InternalErrorException ex){
			throw new VOOTException("internal_server_error");
		}catch(MemberNotExistsException ex){
			throw new VOOTException("not_a_member");
		}

		return groups;
	}

	/**
	 * Sorting array of members by value of sortBy, that are presented to end-user. If the value of sortBy is not match with
	 * any singular attribute of members, then array is sorting by default value. The array is sort ascending or descending. If user determines value
	 * of sortOrder descending, then array is sort descending, otherwise is sorting ascending.
	 *
	 * @param vootMembers    array of members
	 */
	private VOOTMember[] sortVOOTMembers(VOOTMember[] vootMembers){

		if(sortByValue == null){
			//default sort
			if(sortOrderValue.equalsIgnoreCase(DESCENDING)){
				Arrays.sort(vootMembers,new VOOTMemberDefaultDescComparator());
			}else{
				Arrays.sort(vootMembers);
			}

			return vootMembers;
		}

		if(sortByValue.equals("id")){
			if(sortOrderValue.equalsIgnoreCase(DESCENDING)){
				Arrays.sort(vootMembers, new VOOTMemberIdDescComparator());
			}else{
				Arrays.sort(vootMembers, new VOOTMemberIdAscComparator());
			}
		}

		if(sortByValue.equals("displayName")){
			if(sortOrderValue.equalsIgnoreCase(DESCENDING)){
				Arrays.sort(vootMembers,new VOOTMemberDisplayNameDescComparator());
			}else{
				Arrays.sort(vootMembers, new VOOTMemberDisplayNameAscComparator());
			}
		}

		if(sortByValue.equals("voot_membership_role")){
			if(sortOrderValue.equalsIgnoreCase(DESCENDING)){
				Arrays.sort(vootMembers, new VOOTMemberMembershipRoleDescComparator());
			}else{
				Arrays.sort(vootMembers, new VOOTMemberMembershipRoleAscComparator());
			}
		}

		return vootMembers;
	}

	/**
	 * Sorting array of groups by value of sortBy, that are presented to end-user. If the value of sortBy is not match with
	 * any singular attribute of groups, then array is sorting by default value. The array is sort ascending or descending. If user determines value
	 * of sortOrder descending, then array is sort descending, otherwise is sorting ascending.
	 *
	 * @param vootGroups    array of groups
	 */
	private VOOTGroup[] sortVOOTGroups(VOOTGroup[] vootGroups){

		if(sortByValue == null){

			//default sort
			if(sortOrderValue.equalsIgnoreCase(DESCENDING)){
				Arrays.sort(vootGroups,new VOOTGroupDefaultDescComparator());
			}else{
				Arrays.sort(vootGroups);
			}

			return vootGroups;
		}

		if(sortByValue.equals("id")){
			if(sortOrderValue.equalsIgnoreCase(DESCENDING)){
				Arrays.sort(vootGroups,new VOOTGroupIdDescComparator());
			}else{
				Arrays.sort(vootGroups, new VOOTGroupIdAscComparator());
			}
		}

		if(sortByValue.equals("title")){
			if(sortOrderValue.equalsIgnoreCase(DESCENDING)){
				Arrays.sort(vootGroups, new VOOTGroupTitleDescComparator());
			}else{
				Arrays.sort(vootGroups,new VOOTGroupTitleAscComparator());
			}
		}

		if(sortByValue.equals("description")){
			if(sortOrderValue.equalsIgnoreCase(DESCENDING)){
				Arrays.sort(vootGroups, new VOOTGroupDescriptionDescComparator());
			}else{
				Arrays.sort(vootGroups, new VOOTGroupDescriptionAscComparator());
			}
		}

		if(sortByValue.equals("voot_membership_role")){
			if(sortOrderValue.equalsIgnoreCase(DESCENDING)){
				Arrays.sort(vootGroups, new VOOTGroupMembershipRoleDescComparator());
			}else{
				Arrays.sort(vootGroups, new VOOTGroupMembershipRoleAscComparator());
			}
		}

		return vootGroups;
	}

	/**
	 * Filtering array of members by request parameters. Fields names for parameters are filterBy, filterOp and filterValue.
	 * FilterBy has default value by id, filterOp and filterValue have to be determined together. Array is filtering by key,
	 * which is value of filterBy and where key has value of filterValue's value. Value of filterOp specified operation used to filtering.
	 * E.g. for parameters request 'filterBy=displayName, filterOp=contains, filterValue=Peter' correct results have displayName e.g. 'Peter Druhy or Peter Treti'.
	 *
	 * Value of filterBy - singular attribute's name of member presented to end-user.
	 * Value of filterOp - one of values : contains, equals, startsWith, present, where default value is contains
	 * Value of filterValue - value of attribute determines in filterBy
	 *
	 * @param vootMembers       array of original members
	 * @return                  filtered array of members
	 * @throws VOOTException    if can not filter members
	 */
	private VOOTMember[] filterVOOTMembers(VOOTMember[] vootMembers) throws VOOTException{

		if(filterByValue == null && filterValueValue==null) return vootMembers;

		List<VOOTMember> filterMembers = new ArrayList<VOOTMember>();

		final String id = "id";
		final String displayName = "displayName";
		final String emails = "emails";
		final String vootMembership = "voot_membership_role";

		//add all attributes of VOOTGroup to filterByValues to control
		List<String> filterByValues = new ArrayList<String>();
		filterByValues.add(id);
		filterByValues.add(displayName);
		filterByValues.add(emails);
		filterByValues.add(vootMembership);

		checkFilterOpValue();

		//filterOp has default value contains
		if(filterByValue != null  && filterValueValue != null){

			checkFilterByValueForVOOTMember(filterByValues);

			for(VOOTMember filterMember : vootMembers){

				//contains for - id, displayName, voot_membership_role
				if(filterOpValue.equals(CONTAINS)){

					if(filterByValue.equalsIgnoreCase(id)){
						if(filterMember.getId().contains(filterValueValue)){
							filterMembers.add(filterMember);
						}
					}

					if(filterByValue.equalsIgnoreCase(displayName)){
						if(filterMember.getDisplayName().contains(filterValueValue)){
							filterMembers.add(filterMember);
						}
					}

					if(filterByValue.equalsIgnoreCase(vootMembership)){
						if(filterMember.getVoot_membership_role().contains(filterValueValue)){
							filterMembers.add(filterMember);
						}
					}
				}

				//equals for - id, displayName, voot_membership_role
				if(filterOpValue.equals(EQUALS)){
					if(filterByValue.equalsIgnoreCase(id)){
						if(filterValueValue.equals(filterMember.getId())){
							filterMembers.add(filterMember);
						}
					}

					if(filterByValue.equalsIgnoreCase(displayName)){
						if(filterValueValue.equals(filterMember.getDisplayName())){
							filterMembers.add(filterMember);
						}
					}

					if(filterByValue.equalsIgnoreCase(vootMembership)){
						if(filterValueValue.equals(filterMember.getVoot_membership_role())){
							filterMembers.add(filterMember);
						}
					}
				}

				//startsWith for - id, displayName, voot_membership_role
				if(filterOpValue.equals(STARTS_WITH)){
					if(filterByValue.equalsIgnoreCase(id)){
						if(filterMember.getId().startsWith(filterValueValue)){
							filterMembers.add(filterMember);
						}
					}

					if(filterByValue.equalsIgnoreCase(displayName)){
						if(filterMember.getDisplayName().startsWith(filterValueValue)){
							filterMembers.add(filterMember);
						}
					}

					if(filterByValue.equalsIgnoreCase(vootMembership)){
						if(filterMember.getVoot_membership_role().startsWith(filterValueValue)){
							filterMembers.add(filterMember);
						}
					}
				}
			}
		}else if(filterByValue == null && filterValueValue != null){
			throw new VOOTException("internal_server_error","filterBy field is missing");
		}else if(filterByValue != null && filterValueValue == null){

			if(!filterByValue.equals(PRESENT))throw new VOOTException("internal_server_error", "invalid filterValue field");

			for(VOOTMember filterMember : filterMembers){

				//present for - id, displayName, voot_membership_role
				if(filterOpValue.equals(PRESENT)){

					if(filterByValue.equalsIgnoreCase(id)){
						if(filterMember.getId() != null && !filterMember.getId().isEmpty()){
							filterMembers.add(filterMember);
						}
					}

					if(filterByValue.equalsIgnoreCase(displayName)){
						if(filterMember.getDisplayName()!= null && !filterMember.getDisplayName().isEmpty()){
							filterMembers.add(filterMember);
						}
					}

					if(filterByValue.equalsIgnoreCase(vootMembership)){
						if(filterMember.getVoot_membership_role() != null && !filterMember.getVoot_membership_role().isEmpty()){
							filterMembers.add(filterMember);
						}
					}
				}
			}
		}

		return filterMembers.toArray(new VOOTMember[filterMembers.size()]);
	}

	/**
	 * Filtering array of groups by request parameters. Fields names for parameters are filterBy, filterOp and filterValue.
	 * FilterBy has default value by id, filterOp and filterValue have to be determined together. Array is filtering by key,
	 * which is value of filterBy and where key has value of filterValue's value. Value of filterOp specified operation used to filtering.
	 * E.g. for parameters request 'filterBy=title, filterOp=contains, filterValue=meta' correct results have title e.g. 'meta1 or meta2'.
	 *
	 * Value of filterBy - singular attribute's name of group presented to end-user.
	 * Value of filterOp - one of values : contains, equals, startsWith, present, where default value is contains
	 * Value of filterValue - value of attribute determines in filterBy
	 *
	 * @param vootGroups        array of original groups
	 * @return                  filtered array of groups
	 * @throws VOOTException    if can not filter groups
	 */
	private VOOTGroup[] filterVOOTGroups(VOOTGroup[] vootGroups) throws VOOTException{

		if(filterByValue == null && filterValueValue==null) return vootGroups;

		List<VOOTGroup> filterGroups = new ArrayList<VOOTGroup>();

		final String id = "id";
		final String title = "title";
		final String description = "description";
		final String vootMembership = "voot_membership_role";

		//add all attributes of VOOTGroup to filterByValues for check
		List<String> filterByValues = new ArrayList<String>();
		filterByValues.add(id);
		filterByValues.add(title);
		filterByValues.add(description);
		filterByValues.add(vootMembership);

		checkFilterOpValue();

		//filterOp has default value contains
		if(filterByValue != null && filterValueValue != null){

			checkFilterByValueForVOOTGroup(filterByValues);

			for(VOOTGroup filterGroup : vootGroups){

				//contains for - id, title, description, voot_memberhip_role
				if(filterOpValue.equals(CONTAINS) && filterValueValue != null){

					if(filterByValue.equalsIgnoreCase(id)){
						if(filterGroup.getId().contains(filterValueValue)){
							filterGroups.add(filterGroup);
						}
					}

					if(filterByValue.equalsIgnoreCase(title)){
						if(filterGroup.getTitle().contains(filterValueValue)){
							filterGroups.add(filterGroup);
						}
					}

					if(filterByValue.equalsIgnoreCase(description)){
						if(filterGroup.getDescription().contains(filterValueValue)){
							filterGroups.add(filterGroup);
						}
					}

					if(filterByValue.equalsIgnoreCase(vootMembership)){
						if(filterGroup.getVoot_membership_role().contains(filterValueValue)){
							filterGroups.add(filterGroup);
						}
					}
				}

				//equals for - id, title, description, voot_memberhip_role
				if(filterOpValue.equals(EQUALS)){
					if(filterByValue.equalsIgnoreCase(id)){
						if(filterValueValue.equals(filterGroup.getId())){
							filterGroups.add(filterGroup);
						}
					}

					if(filterByValue.equalsIgnoreCase(title)){
						if(filterValueValue.equals(filterGroup.getTitle())){
							filterGroups.add(filterGroup);
						}
					}

					if(filterByValue.equalsIgnoreCase(description)){
						if(filterValueValue.equals(filterGroup.getDescription())){
							filterGroups.add(filterGroup);
						}
					}

					if(filterByValue.equalsIgnoreCase(vootMembership)){
						if(filterValueValue.equals(filterGroup.getVoot_membership_role())){
							filterGroups.add(filterGroup);
						}
					}
				}

				//startsWith for - id, title, description, voot_memberhip_role
				if(filterOpValue.equals(STARTS_WITH)){
					if(filterByValue.equalsIgnoreCase(id)){
						if(filterGroup.getId().startsWith(filterValueValue)){
							filterGroups.add(filterGroup);
						}
					}

					if(filterByValue.equalsIgnoreCase(title)){
						if(filterGroup.getTitle().startsWith(filterValueValue)){
							filterGroups.add(filterGroup);
						}
					}

					if(filterByValue.equalsIgnoreCase(description)){
						if(filterGroup.getDescription().startsWith(filterValueValue)){
							filterGroups.add(filterGroup);
						}
					}

					if(filterByValue.equalsIgnoreCase(vootMembership)){
						if(filterGroup.getVoot_membership_role().startsWith(filterValueValue)){
							filterGroups.add(filterGroup);
						}
					}
				}

			}
		}else if(filterByValue == null && filterValueValue != null){
			throw new VOOTException("internal_server_error", "filterBy field is missing");

		}else if(filterByValue != null && filterValueValue == null){

			if(!filterByValue.equals(PRESENT)) throw new VOOTException("internal_server_error", "invalid filterValue field");

			for(VOOTGroup filterGroup : vootGroups){

				//present for - id, title, description, voot_memberhip_role
				if(filterOpValue.equals(PRESENT)){

					if(filterByValue.equalsIgnoreCase(id)){
						if(filterGroup.getId() != null && !filterGroup.getId().isEmpty()){
							filterGroups.add(filterGroup);
						}
					}

					if(filterByValue.equalsIgnoreCase(title)){
						if(filterGroup.getTitle() != null && !filterGroup.getTitle().isEmpty()){
							filterGroups.add(filterGroup);
						}
					}

					if(filterByValue.equalsIgnoreCase(description)){
						if(filterGroup.getTitle() != null && !filterGroup.getTitle().isEmpty()){
							filterGroups.add(filterGroup);
						}
					}

					if(filterByValue.equalsIgnoreCase(vootMembership)){
						if(filterGroup.getVoot_membership_role() != null && !filterGroup.getVoot_membership_role().isEmpty()){
							filterGroups.add(filterGroup);
						}
					}
				}
			}
		}

		return filterGroups.toArray(new VOOTGroup[filterGroups.size()]);
	}

	/**
	 * Parse request parameters and give them to map, which is consist of keys that are form of parameters field name
	 * and values of map are values of parameters key.
	 *
	 * @param parameters    request parameters
	 * @return              map, consist of parametes field name and parameters field value
	 */
	private Map<String, String> parseParameters(String parameters){
		String parms [] = parameters.split(",");
		Map<String, String> parmsMap = new HashMap<String, String>();

		for(int i=0;i<parms.length;i++){
			String[] param = parms[i].split("=", 2);

			if(param.length < 2){
				return parmsMap;
			}

			parmsMap.put(param[0].toLowerCase(), param[1]);
		}

		return parmsMap;
	}

	//Check fied name of filterOpValue in parameters.
	private void checkFilterOpValue() throws VOOTException{
		List<String> filterOpValues = new ArrayList<String>();
		filterOpValues.add(CONTAINS.toLowerCase());
		filterOpValues.add(EQUALS.toLowerCase());
		filterOpValues.add(STARTS_WITH.toLowerCase());
		filterOpValues.add(PRESENT.toLowerCase());

		if(!filterOpValues.contains(filterOpValue.toLowerCase())){
			throw new VOOTException("internal_server_error", "value of filterOp has bad format");
		}
	}

	//Check value of filterBy for VOOTGroup.
	private void checkFilterByValueForVOOTGroup(final List<String> filterByValues) throws VOOTException{
		if(!filterByValues.contains(filterByValue)){
			throw new VOOTException("internal_server_error", "value of filterBy is not attribute of group");
		}
	}

	//Check value of filterBy for VOOTMember.
	private void checkFilterByValueForVOOTMember(final List<String> filterByValues) throws VOOTException{
		if(!filterByValues.contains(filterByValue)){
			throw new VOOTException("internal_server_error", "value of filterBy is not attribute of member");
		}
	}

	//Set request parameters.
	private void setParamters() throws VOOTException{

		Set<String> keys = parameters.keySet();

		if(keys.contains(FORMAT.toLowerCase())){
			String format = parameters.get(FORMAT.toLowerCase());
			if(format.equalsIgnoreCase(JSON)){
				formatValue = JSON;
			}
		}

		if(keys.contains(START_INDEX.toLowerCase())){
			startIndexValue = Integer.valueOf(parameters.get(START_INDEX.toLowerCase()));
		}else{
			startIndexValue  = new Integer(0);
		}

		if(keys.contains(COUNT.toLowerCase())){
			countValue = Integer.valueOf(parameters.get(COUNT.toLowerCase()));
		}else{
			countValue = new Integer(0);
		}

		if(keys.contains(FILTER_BY.toLowerCase())){
			filterByValue = parameters.get(FILTER_BY.toLowerCase());
		}else{
			filterByValue = null;
		}

		if(keys.contains(FILTER_OP.toLowerCase())){
			filterOpValue = parameters.get(FILTER_OP.toLowerCase());
		}else{
			//default value of filterOp is contains
			filterOpValue = CONTAINS;
		}

		if(keys.contains(FILTER_VALUE.toLowerCase())){
			filterValueValue = parameters.get(FILTER_VALUE.toLowerCase());
		}else{
			filterValueValue = null;
		}

		if(keys.contains(SORT_BY.toLowerCase())){
			sortByValue = parameters.get(SORT_BY.toLowerCase());
		}else{
			sortByValue = null;
		}

		if(keys.contains(SORT_ORDER.toLowerCase())){
			String sortOrderValue = parameters.get(SORT_ORDER.toLowerCase());
			if(sortOrderValue.equalsIgnoreCase(DESCENDING)){
				this.sortOrderValue = sortOrderValue;
			}else{
				this.sortOrderValue = ASCENDING;
			}

		}else{
			//sortOrder has default value ascending
			this.sortOrderValue = ASCENDING;
		}
	}
}
