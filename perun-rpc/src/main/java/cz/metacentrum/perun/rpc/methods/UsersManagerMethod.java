package cz.metacentrum.perun.rpc.methods;

import java.util.HashMap;
import java.util.List;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public enum UsersManagerMethod implements ManagerMethod {

	/*#
	 * Returns user based on one of the userExtSource.
	 *
	 * @param userExtSource UserExtSource JSON object UserExtSource
	 * @return User User object
	 */
	getUserByUserExtSource {

		@Override
		public User call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getUserByUserExtSource(ac.getSession(),
					parms.read("userExtSource", UserExtSource.class));
		}
	},

	/*#
	 * Returns user by his login in external source and external source.
	 *
	 * @param extSourceName String Ext source name
	 * @param extLogin String Ext source login
	 * @return User User object
	 */
	getUserByExtSourceNameAndExtLogin {

		@Override
		public User call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getUserByExtSourceNameAndExtLogin(ac.getSession(),
					parms.readString("extSourceName"),
					parms.readString("extLogin"));
		}
	},

	/*#
	 * Returns user by its <code>id</code>.
	 *
	 * @param id int User <code>id</code>
	 * @return User User object
	 */
	getUserById {

		@Override
		public User call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUserById(parms.readInt("id"));
		}
	},

	/*#
	 * Returns all users in Perun.
	 *
	 * @return List<User> All Perun users
	 */
	getUsers {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getUsers(ac.getSession());
		}
	},

	/*#
	 * Returns all specific users in Perun.
	 *
	 * @return List<User> All Perun service users
	 */
	getSpecificUsers {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getSpecificUsers(ac.getSession());
		}
	},

	/*#
	 * Return all specific users who are owned by the user.
	 *
	 * @param user int User <code>id</code>
	 * @return List<User> Specific users for a user
	 */
	getSpecificUsersByUser {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getSpecificUsersByUser(ac.getSession(),
					ac.getUserById(parms.readInt("user")));
		}
	},

	/*#
	 * Return all users who owns the specific user.
	 *
	 * @param specificUser int Specific User <code>id</code>
	 * @return List<User> Users for a service user
	 */
	getUsersBySpecificUser {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getUsersBySpecificUser(ac.getSession(),
					ac.getUserById(parms.readInt("specificUser")));
		}
	},

	/*#
	 * Add specific user owner (the user).
	 *
	 * @param user int User <code>id</code>
	 * @param specificUser int Specific user <code>id</code>
	 */
	addSpecificUserOwner {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			ac.getUsersManager().addSpecificUserOwner(ac.getSession(),
					ac.getUserById(parms.readInt("user")),
					ac.getUserById(parms.readInt("specificUser")));

			return null;
		}
	},

	/*#
	 * Remove specific user owner (the user).
	 *
	 * @param user int User <code>id</code>
	 * @param specificUser int Specific user <code>id</code>
	 */
	removeSpecificUserOwner {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			ac.getUsersManager().removeSpecificUserOwner(ac.getSession(),
					ac.getUserById(parms.readInt("user")),
					ac.getUserById(parms.readInt("specificUser")));

			return null;
		}
	},

	/*#
	 * Get User to RichUser without attributes.
	 *
	 * @param user int user <code>id</code>
	 * @return RichUser found rich user
	 */
	getRichUser {

		@Override
		public RichUser call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getRichUser(ac.getSession(),
					ac.getUserById(parms.readInt("user")));
		}
	},

	/*#
	 * Get User to RichUser with attributes.
	 *
	 * @param user int user <code>id</code>
	 * @return RichUser found rich user with attributes
	 */
	getRichUserWithAttributes {

		@Override
		public RichUser call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getRichUserWithAttributes(ac.getSession(),
					ac.getUserById(parms.readInt("user")));
		}
	},

	/*#
	 * Get All richUsers with or without specificUsers.
	 *
	 * @param includedSpecificUsers boolean if you want to or don't want to get specificUsers too
	 * @return List<RichUser> all rich users without attributes
	 */
	getAllRichUsers {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getAllRichUsers(ac.getSession(),
					parms.readBoolean("includedSpecificUsers"));
		}
	},

	/*#
	 * Get All richUsers with or without specificUsers.
	 *
	 * @param includedSpecificUsers boolean if you want to or don't want to get specificUsers too
	 * @return List<RichUser> all rich users with attributes
	 */
	getAllRichUsersWithAttributes {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getAllRichUsersWithAttributes(ac.getSession(),
					parms.readBoolean("includedSpecificUsers"));
		}
	},

	/*#
	 * From Users makes RichUsers without attributes.
	 *
	 * @param users List<RichUser> users to convert
	 * @return List<RichUser> list of rich users
	 */
	getRichUsersFromListOfUsers {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getUsersManager().getRichUsersFromListOfUsers(ac.getSession(),
					parms.readList("users", User.class));
		}
	},

	/*#
	 * From Users makes RichUsers with attributes.
	 *
	 * @param users List<RichUser> users to convert
	 * @return List<RichUser> list of richUsers
	 */
	getRichUsersFromListOfUsersWithAttributes {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getUsersManager().getRichUsersWithAttributesFromListOfUsers(ac.getSession(),
					parms.readList("users", User.class));
		}
	},

	/*#
	 * Returns all RichUsers with attributes who are not member of any VO.
	 *
	 * @return List<RichUser> list of richUsers who are not member of any VO
	 */
	getRichUsersWithoutVoAssigned {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getRichUsersWithoutVoAssigned(ac.getSession());
		}
	},

	/*#
	 * Get All richUsers with or without specificUsers with selected attributes.
	 *
	 * @param attrsNames List<String> list of attributes name
	 * @param includedSpecificUsers boolean if you want to or don't want to get specificUsers too
	 * @return List<RichUser> list of RichUsers
	 */
	/*#
	 * Get All richUsers with or without specificUsers with all included attributes.
	 *
	 * @param includedSpecificUsers boolean if you want to or don't want to get specificUsers too
	 * @return List<RichUser> list of RichUsers
	 */
	getRichUsersWithAttributes {
		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("attrsNames")) {
				return ac.getUsersManager().getAllRichUsersWithAttributes(ac.getSession(),
						parms.readBoolean("includedSpecificUsers"),
						parms.readList("attrsNames", String.class));
			} else {
				return ac.getUsersManager().getAllRichUsersWithAttributes(ac.getSession(),
						parms.readBoolean("includedSpecificUsers"), null);
			}
		}
	},

	/*#
	 * Returns list of RichUsers with attributes who matches the searchString
	 *
	 * @param searchString String searched string
	 * @param attrsNames List<String> list of attributes name
	 * @return List<RichUser> list of RichUsers
	 */
	findRichUsersWithAttributes {
		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("attrsNames")) {
				return ac.getUsersManager().findRichUsersWithAttributes(ac.getSession(),
						parms.readString("searchString"),
						parms.readList("attrsNames", String.class));
			} else {
				return ac.getUsersManager().findRichUsersWithAttributes(ac.getSession(),
						parms.readString("searchString"), null);
			}

		}
	},

	/*#
	 * Returns list of RichUsers which are not members of any VO and with selected attributes
	 *
	 * @param attrsNames List<String> list of attributes name
	 * @return List<RichUser> list of RichUsers
	 */
	getRichUsersWithoutVoWithAttributes {
		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("attrsNames")) {
				return ac.getUsersManager().getRichUsersWithoutVoWithAttributes(ac.getSession(),
						parms.readList("attrsNames", String.class));
			} else {
				return ac.getUsersManager().getRichUsersWithoutVoWithAttributes(ac.getSession(), null);
			}
		}
	},

	/*#
	 * Return list of RichUsers who matches the searchString and are not member in specific VO and with selected attributes.
	 *
	 * @param vo VO virtual organization
	 * @param searchString String searched string
	 * @param attrsName List<String> list of attributes name
	 * @return List<RichUser> list of RichUsers
	 */
	findRichUsersWithoutSpecificVoWithAttributes {
		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("attrsNames")) {
				return ac.getUsersManager().findRichUsersWithoutSpecificVoWithAttributes(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.readString("searchString"),
						parms.readList("attrsNames", String.class));
			} else {
				return ac.getUsersManager().findRichUsersWithoutSpecificVoWithAttributes(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.readString("searchString"), null);
			}
		}
	},

	/*#
	 * Deletes a user. User is not deleted, if is member of any VO or is associated with any service identity.
	 *
	 * @param user int User <code>id</code>
	 */
	/*#
	 * Deletes a user (force).
	 * Also removes associated members.
	 *
	 * @param user int User <code>id</code>
	 * @param force boolean If true, use force deletion.
	 */
	deleteUser {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("force") && parms.readBoolean("force")) {
				ac.getUsersManager().deleteUser(ac.getSession(),
						ac.getUserById(parms.readInt("user")), true);
			} else {
				ac.getUsersManager().deleteUser(ac.getSession(),
						ac.getUserById(parms.readInt("user")));
			}
			return null;
		}
	},

	/*#
	 * Updates users data in DB.
	 *
	 * @param user User JSON object
	 * @return User Updated user
	 */
	updateUser {

		@Override
		public User call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getUsersManager().updateUser(ac.getSession(),
					parms.read("user", User.class));
		}
	},

	/*#
	 * Updates titles before/after users name
	 *
	 * Titles must be set in User object.
	 * Setting any title to null will remove title from name.
	 * Other user's properties are ignored.
	 *
	 * @param user User JSON object with titles to set
	 * @return User Updated user
	 */
	updateNameTitles {

		@Override
		public User call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getUsersManager().updateNameTitles(ac.getSession(),
					parms.read("user", User.class));
		}
	},

	/*#
	 * Updates user's userExtSource in DB.
	 *
	 * @param userExtSource UserExtSource JSON object
	 * @return UserExtSource Updated userExtSource
	 */
	updateUserExtSource {

		@Override
		public UserExtSource call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getUsersManager().updateUserExtSource(ac.getSession(),
					parms.read("userExtSource", UserExtSource.class));
		}
	},

	/*#
	 * Gets list of all user's external sources of the user.
	 *
	 * @param user int User <code>id</code>
	 * @return List<UserExtSource> list of user's external sources
	 */
	getUserExtSources {

		@Override
		public List<UserExtSource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getUserExtSources(ac.getSession(),
					ac.getUserById(parms.readInt("user")));
		}
	},

	/*#
	 * Adds user's external sources.
	 * @param user int User <code>id</code>
	 * @param userExtSource UserExtSource JSON object
	 * @return UserExtSource Newly added UserExtSource
	 */
	addUserExtSource {

		@Override
		public UserExtSource call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getUsersManager().addUserExtSource(ac.getSession(),
					ac.getUserById(parms.readInt("user")),
					parms.read("userExtSource", UserExtSource.class));
		}
	},

	/*#
	 * Remove user's external source.
	 * Persistent UserExtSources are not removed unless <code>force</code> param is present and set to <code>true</code>.
	 * @param user int User <code>id</code>
	 * @param userExtSource int UserExtSource <code>id</code>
	 * @param force boolean If true, use force deletion.
	 */
	removeUserExtSource {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("force") && parms.readBoolean("force")) {
				ac.getUsersManager().removeUserExtSource(ac.getSession(),
					ac.getUserById(parms.readInt("user")),
					ac.getUserExtSourceById(parms.readInt("userExtSource")), true);
			} else {
				ac.getUsersManager().removeUserExtSource(ac.getSession(),
					ac.getUserById(parms.readInt("user")),
					ac.getUserExtSourceById(parms.readInt("userExtSource")));
			}
				
			return null;
		}
	},

	/*#
	 * Move user's external source from sourceUser to targetUser.
	 * @param sourceUser int User <code>id</code>
	 * @param targetUser int User <code>id</code>
	 * @param userExtSource int UserExtSource <code>id</code>
	 */
	moveUserExtSource {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getUsersManager().moveUserExtSource(ac.getSession(),
					ac.getUserById(parms.readInt("sourceUser")),
					ac.getUserById(parms.readInt("targetUser")),
					ac.getUserExtSourceById(parms.readInt("userExtSource")));
			return null;
		}
	},

	/*#
	 * Get the user ext source by its id.
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
	 * @return UserExtSource User external source for the id
	 */
	getUserExtSourceById {

		@Override
		public UserExtSource call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getUserExtSourceById(ac.getSession(),
					parms.readInt("userExtSource"));
		}
	},

	/*#
	 * Gets user's external source by the user's external login and external source.
	 *
	 * @param extSource ExtSource JSON object
	 * @param extSourceLogin String Login
	 * @return UserExtSource UserExtSource found user's external source
	 */
	getUserExtSourceByExtLogin {

		@Override
		public UserExtSource call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getUserExtSourceByExtLogin(ac.getSession(),
					parms.read("extSource", ExtSource.class),
					parms.readString("extSourceLogin"));
		}
	},

	/*#
	 * Returns user by VO member.
	 *
	 * @param member int Member <code>id</code>
	 * @return User User object
	 */
	getUserByMember {

		@Override
		public User call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getUserByMember(ac.getSession(),
					ac.getMemberById(parms.readInt("member")));
		}
	},

	/*#
	 * Returns list of users who matches the searchString, searching name, email, logins.
	 *
	 * @param searchString String String to search by
	 * @return List<User> Found users
	 */
	findUsers {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().findUsers(ac.getSession(),
					parms.readString("searchString"));
		}
	},

	/*#
	 * Returns list of RichUsers with attributes who matches the searchString, searching name, email, logins.
	 *
	 * @param searchString String searched string
	 * @return List<RichUser> list of RichUsers
	 */
	findRichUsers {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().findRichUsers(ac.getSession(),
					parms.readString("searchString"));
		}
	},

	/*#
	 * Return list of users who matches the searchString, searching name, email and logins
	 * and are not member of specific VO.
	 *
	 * @param vo int VO <code>id</code>
	 * @param searchString String String to search by
	 * @return List<User> Found users
	 */
	getUsersWithoutSpecificVo {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getUsersWithoutSpecificVo(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					parms.readString("searchString"));
		}
	},

	/*#
	 * Returns list of users who matches the searchString.
	 *
	 * @param searchString String String to search by
	 * @return List<User> Found users
	 */
	/*#
	 * Returns list of users who matches the parameters.
	 * All parameters must be present, even if empty.
	 *
	 * @param titleBefore String Title before name
	 * @param firstName String First name
	 * @param middleName String Middle name
	 * @param lastName String Last name
	 * @param titleAfter String Title after
	 * @return List<User> Found users
	 */
	findUsersByName {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("searchString")) {
				return ac.getUsersManager().findUsersByName(ac.getSession(),
						parms.readString("searchString"));
			} else if (parms.contains("titleBefore") && parms.contains("firstName") &&
					parms.contains("middleName") && parms.contains("lastName") && parms.contains("titleAfter")) {
				return ac.getUsersManager().findUsersByName(ac.getSession(),
						parms.readString("titleBefore"), parms.readString("firstName"),
						parms.readString("middleName"), parms.readString("lastName"),
						parms.readString("titleAfter"));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "searchString or (titleBefore and firstName and middleName and lastName and titleAfter)");
			}
		}
	},

	/*#
	 * Returns all users who have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param attribute Attribute JSON object
	 * @return List<User> Found users
	 */
	/*#
	 * Returns all users who have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param attributeName String URN of attribute to search by
	 * @param attributeValue Object Value to search by (type of value must match attribute value type)
	 * @return List<User> Found users
	 */
	getUsersByAttribute {
		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("attributeName")) {
				if (parms.contains("attributeValue")) {
					String attributeName = parms.readString("attributeName");
					Attribute attr = new Attribute(ac.getAttributesManager().getAttributeDefinition(ac.getSession(), attributeName));

					if(attr.getType().equals(Integer.class.getName())) {
						attr.setValue(parms.readInt("attributeValue"));
					} else if(attr.getType().equals(String.class.getName()) || attr.getType().equals(BeansUtils.largeStringClassName)) {
						attr.setValue(parms.readString("attributeValue"));
					} else if(attr.getType().equals(Boolean.class.getName())) {
						attr.setValue(parms.readBoolean("attributeValue"));
					} else if(attr.getType().equals(ArrayList.class.getName()) || attr.getType().equals(BeansUtils.largeArrayListClassName)) {
						attr.setValue(parms.readList("attributeValue", String.class));
					} else if(attr.getType().equals(LinkedHashMap.class.getName())) {
						attr.setValue(parms.read("attributeValue", LinkedHashMap.class));
					} else {
						throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, "attributeValue is not the same type like value of attribute with the attributeName.");
					}
					return ac.getUsersManager().getUsersByAttribute(ac.getSession(),attr);
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "attributeValue");
				}
			} else if (parms.contains("attribute")) {
				return ac.getUsersManager().getUsersByAttribute(ac.getSession(), parms.read("attribute", Attribute.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "attribute or (attributeName and attributeValue)");
			}
		}

	},

	/*#
	 * Returns all users who have attribute which have value which contains searchString.
	 *
	 * @param attributeName String URN of attribute to search by
	 * @param attributeValue String Value to search by
	 * @return List<User> Found users
	 */
	getUsersByAttributeValue {
		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("attributeName")) {
				if (parms.contains("attributeValue")) {
					return ac.getUsersManager().getUsersByAttributeValue(ac.getSession(), parms.readString("attributeName"), parms.readString("attributeValue"));
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "attributeValue");
				}
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "attributeName");
			}
		}
	},

	/*#
	 * Returns list of VOs, where the user is an Administrator.
	 *
	 * @param user int User <code>id</code>
	 * @return List<Vo> Found VOs
	 */
	getVosWhereUserIsAdmin {

		@Override
		public List<Vo> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getVosWhereUserIsAdmin(ac.getSession(),
					ac.getUserById(parms.readInt("user")));
		}
	},

	/*#
	 * Returns list of VOs, where the user is a Member.
	 *
	 * @param user int User <code>id</code>
	 * @return List<Vo> Found VOs
	 */
	getVosWhereUserIsMember {

		@Override
		public List<Vo> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getVosWhereUserIsMember(ac.getSession(),
					ac.getUserById(parms.readInt("user")));
		}
	},

	/*#
	 * Returns list of Groups in Perun, where the User is a direct Administrator
	 * or he is a member of any group which is Administrator of some of these Groups.
	 *
	 * @param user int User <code>id</code>
	 * @return List<Group> Found Groups
	 */
	/*#
	 * Returns list of Groups in selected Vo, where the User is a direct Administrator
	 * or he is a member of any group which is Administrator of some of these Groups.
	 *
	 * @param user int User <code>id</code>
	 * @param vo int Vo <code>id</code>
	 * @return List<Group> Found Groups
	 */
	getGroupsWhereUserIsAdmin {

		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("vo")) {
				return ac.getUsersManager().getGroupsWhereUserIsAdmin(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						ac.getUserById(parms.readInt("user")));
			} else {
				return ac.getUsersManager().getGroupsWhereUserIsAdmin(ac.getSession(),
						ac.getUserById(parms.readInt("user")));
			}
		}
	},

	/*#
	 * Get all resources from the facility which have the user access on.
	 *
	 * @param user int User <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return List<Resource> Allowed resources
	 */
	/*#
	 * Get all resources which have the user access on.
	 *
	 * @param user int User <code>id</code>
	 * @return List<Resource> Allowed resources
	 */
	getAllowedResources {

		@Override
		public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			User user = ac.getUserById(parms.readInt("user"));
			if(parms.contains("facility")) {
				Facility facility = ac.getFacilityById(parms.readInt("facility"));
				return ac.getUsersManager().getAllowedResources(ac.getSession(), facility, user);
			} else {
				return ac.getUsersManager().getAllowedResources(ac.getSession(), user);
			}
		}
	},

	/*#
	 * Get all rich resources which have the user assigned.
	 *
	 * @param user int User <code>id</code>
	 * @return List<RichResource> Assigned rich resources
	 */
	getAssignedRichResources {

		@Override
		public List<RichResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			User user = ac.getUserById(parms.readInt("user"));
			return ac.getUsersManager().getAssignedRichResources(ac.getSession(), user);
		}
	},

	/*#
	 * Checks if the login is available in the namespace.
	 *
	 * @param loginNamespace String Namespace
	 * @param login String Login
	 * @exampleResponse 1
	 * @return int 1: login available, 0: login not available
	 */
	isLoginAvailable {

		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (ac.getUsersManager().isLoginAvailable(ac.getSession(), parms.readString("loginNamespace"), parms.readString("login"))) {
				return 1;
			} else {
				return 0;
			}

		}
	},

	/*#
	 * Returns all users who are not member of any VO.
	 *
	 * @return List<User> Found users
	 */
	getUsersWithoutVoAssigned {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getUsersWithoutVoAssigned(ac.getSession());
		}
	},

	/*#
	 * Adds PERUNADMIN role to the user.
	 *
	 * @param user int User <code>id</code>
	 */
	makeUserPerunAdmin {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			User user = ac.getUserById(parms.readInt("user"));
			ac.getUsersManager().makeUserPerunAdmin(ac.getSession(), user);
			return null;
		}
	},


	/*#
	 * Changes user password in defined login-namespace.
	 *
	 * @param login String Users login
	 * @param loginNamespace String Namespace
	 * @param newPassword String New password
	 * @param oldPassword String Old password which will be checked. This parameter is required only if checkOldPassword is set to true.
	 * @param checkOldPassword boolean True if the oldPassword have to be checked. When omitted it defaults to false.
	 */
	/*#
	 * Changes user password in defined login-namespace.
	 *
	 * @param user int User <code>id</code>
	 * @param loginNamespace String Namespace
	 * @param newPassword String New password
	 * @param oldPassword String Old password which will be checked. This parameter is required only if checkOldPassword is set to true.
	 * @param checkOldPassword boolean True if the oldPassword have to be checked. When omitted it defaults to false.
	 */
	changePassword {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("login")) {
				String login = parms.readString("login");
				if (parms.contains("checkOldPassword") && parms.readBoolean("checkOldPassword")) {
					ac.getUsersManager().changePassword(ac.getSession(), login, parms.readString("loginNamespace"), parms.readString("oldPassword"), parms.readString("newPassword"), true);
				} else {
					ac.getUsersManager().changePassword(ac.getSession(), login, parms.readString("loginNamespace"), parms.readString("oldPassword"), parms.readString("newPassword"), false);
				}
			} else {
				User user = ac.getUserById(parms.readInt("user"));
				if (parms.contains("checkOldPassword") && parms.readBoolean("checkOldPassword")) {
					ac.getUsersManager().changePassword(ac.getSession(), user, parms.readString("loginNamespace"), parms.readString("oldPassword"), parms.readString("newPassword"), true);
				} else {
					ac.getUsersManager().changePassword(ac.getSession(), user, parms.readString("loginNamespace"), parms.readString("oldPassword"), parms.readString("newPassword"), false);
				}
			}
			return null;
		}
	},
	/*#
	 * Changes user's password in namespace based on encrypted parameters
	 *
	 * @param i String first encrypted parameter
	 * @param m String second encrypted parameter
	 * @param password String new password
	 */
	changeNonAuthzPassword {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getUsersManager().changeNonAuthzPassword(ac.getSession(), parms.readString("i"), parms.readString("m"), parms.readString("password"));

			return null;
		}
	},
	/*#
	 * Creates a password in external authz system.
	 *
	 * @param login String Login
	 * @param namespace String Namespace
	 * @param password String password
	 */
	@Deprecated
	createPassword {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("user")) {
				ac.getUsersManager().createPassword(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("namespace"), parms.readString("password"));
			} else {
				ac.getUsersManager().createPassword(ac.getSession(), parms.readString("login"), parms.readString("namespace"), parms.readString("password"));
			}

			return null;

		}
	},
	/*#
	 * Reserves a random password in external authz system. User shouldn't be able to log-in (account disabled, password unknown to him).
	 * This is usefull when manager create account for others and later send them password reset request.
	 *
	 * @param user int User <code>id</code>
	 * @param namespace String Namespace
	 */
	reserveRandomPassword {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getUsersManager().reserveRandomPassword(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("namespace"));

			return null;

		}
	},
	/*#
	 * Reserves password for a user in specified login-namespace.
	 *
	 * @param user int User <code>id</code>
	 * @param namespace String Namespace
	 * @param password String password
	 */
	/*#
	 * Reserves password for a user in specified login-namespace.
	 *
	 * @param login String Login
	 * @param namespace String Namespace
	 * @param password String password
	 */
	reservePassword {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("user")) {
				ac.getUsersManager().reservePassword(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("namespace"), parms.readString("password"));
			} else {
				ac.getUsersManager().reservePassword(ac.getSession(), parms.readString("login"), parms.readString("namespace"), parms.readString("password"));
			}

			return null;

		}
	},
	/*#
	 * Validates password for a user in specified login-namespace. After that, user should be able to log-in
	 * in external authz system using his credentials.
	 *
	 * @param user int User <code>id</code>
	 * @param namespace String Namespace
	 */
	/*#
	 * Validates password for a user in specified login-namespace. After that, user should be able to log-in
	 * in external authz system using his credentials.
	 *
	 * @param login String Login
	 * @param namespace String Namespace
	 */
	validatePassword {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("user")) {
				ac.getUsersManager().validatePassword(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("namespace"));
			} else {
				ac.getUsersManager().validatePassword(ac.getSession(), parms.readString("login"), parms.readString("namespace"));
			}

			return null;

		}
	},

	/*#
	 * Validates password for a user in specified login-namespace. After that, user should be able to log-in
	 * in external authz system using his credentials. It also creates UserExtSource and sets some required attributes.
	 *
	 * @param user int User <code>id</code>
	 * @param login String Login
	 * @param namespace String Namespace
	 */
	validatePasswordAndSetExtSources {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getUsersManager().validatePasswordAndSetExtSources(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("login"), parms.readString("namespace"));

			return null;

		}
	},
	/*#
	 * Set new login in namespace if login is available and user doesn't have login in that namespace.
	 * !! Works only for service/guest users => specific users !!
	 *
	 * @param user int User <code>id</code>
	 * @param login String Login
	 * @param namespace String Namespace
	 */
	setLogin {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getUsersManager().setLogin(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("namespace"), parms.readString("login"));

			return null;

		}
	},
	/*#
	 * Request to change preferred email address of user.
	 * Validation mail is sent on new address.
	 *
	 * Change is not saved until user validate new email address
	 * by calling validatePreferredEmailChange() method with
	 * proper set of parameters (sent in validation mail).
	 *
	 * @param user int User <code>id</code>
	 * @param email String new email address to set
	 */
	requestPreferredEmailChange {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			String referer = parms.getServletRequest().getHeader("Referer");
			if (referer == null || referer.isEmpty()) {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "Missing \"Referer\" header in HTTP request. Please check your browser settings.");
			}

			ac.getUsersManager().requestPreferredEmailChange(ac.getSession(),
					referer,
					ac.getUserById(parms.readInt("user")),
					parms.readString("email"));

			return null;

		}
	},
	/*#
	 * Validate new preferred email address.
	 *
	 * Request to validate is determined based
	 * on encrypted parameters sent in email notice
	 * by requestPreferredEmailChange() method.
	 *
	 * @param i String encrypted request parameter
	 * @param m String encrypted request parameter
	 * @param u int <code>id</code> of user you want to validate preferred email request
	 *
	 * @return String new validated email address
	 */
	validatePreferredEmailChange {
		@Override
		public String call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getUsersManager().validatePreferredEmailChange(ac.getSession(),
					ac.getUserById(parms.readInt("u")),
					parms.readString("i"),
					parms.readString("m"));

		}
	},

	/*#
	 * Return list of email addresses of user, which are
	 * awaiting validation and are inside time window
	 * for validation.
	 *
	 * If there is no preferred email change request pending
	 * or requests are outside time window for validation,
	 * returns empty list.
	 *
	 * @param user int <code>id</code> of user to check
	 *
	 * @return List<String> user's email addresses pending validation
	 */
	getPendingPreferredEmailChanges {
		@Override
		public List<String> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getUsersManager().getPendingPreferredEmailChanges(ac.getSession(),
					ac.getUserById(parms.readInt("user")));

		}
	},

	/*#
	 * Gets count of all users.

	 * @return int Users count
	 */
	getUsersCount {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getUsersManager().getUsersCount(ac.getSession());
		}
	},

	/*#
	 * Creates alternative password in external system.
	 *
	 * @param user int Users <code>id</code>
	 * @param description String Description of a password (e.g. 'mobile phone', 'tablet', ...)
	 * @param loginNamespace String Login namespace
	 * @param password String String representation of password
	 */
	createAlternativePassword {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {

			ac.getUsersManager().createAlternativePassword(ac.getSession(),
					ac.getUserById(parms.readInt("user")),
					parms.readString("description"),
					parms.readString("loginNamespace"),
					parms.readString("password"));

			return null;
		}
	},

	/*#
	 * Deletes alternative password in external system.
	 *
	 * @param user int Users <code>id</code>
	 * @param loginNamespace String Login namespace
	 * @param passwordId String Password <code>id</code>
	 */
	deleteAlternativePassword {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {

			ac.getUsersManager().deleteAlternativePassword(ac.getSession(),
					ac.getUserById(parms.readInt("user")),
					parms.readString("loginNamespace"),
					parms.readString("passwordId"));

			return null;
		}
	},

	/*#
	 * Updates user's userExtSource last access time in DB. We can get information which userExtSource has been used as a last one.
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
	 */
	updateUserExtSourceLastAccess {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			ac.getUsersManager().updateUserExtSourceLastAccess(ac.getSession(),
					ac.getUserExtSourceById(parms.readInt("userExtSource")));

			return null;
		}
	},

	/*#
	 * Generate user account in a backend system associated with login-namespace in Perun.
	 *
	 * This method consumes optional parameters map. Requirements are implementation-dependant
	 * for each login-namespace.
	 *
	 * Returns map with
	 * 1: key=login-namespace attribute urn, value=generated login
	 * 2: rest of opt response attributes...
	 *
	 * @param namespace String
	 * @param parameters Map
	 *
	 * @return Map<String, String> Map of data from backed response
	 * @throws InternalErrorException
	 */
	generateAccount {

		@Override
		public Map<String, String> call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			return ac.getUsersManager().generateAccount(ac.getSession(),
					parms.readString("namespace"),
					parms.read("parameters", HashMap.class));
		}

	};

}
