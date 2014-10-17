package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Member;
import java.util.List;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.LinkedHashMap;

public enum SearcherMethod implements ManagerMethod {

	/*#
	 * This method get Map of Attributes with searching values and try to find all users, which have specific attributes in format.
	 * Better information about format below. When there are more than 1 attribute in Map, it means all must be true "looking for all of them" (AND)
	 *
	 * @param attributesWithSearchingValues Map<String,String> map of attributes names
	 *        when attribute is type String, so value is string and we are looking for total match (Partial is not supported now, will be supported later by symbol *)
	 *        when attribute is type Integer, so value is integer in String and we are looking for total match
	 *        when attribute is type List<String>, so value is String and we are looking for at least one total or partial matching element
	 *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total match of both or if is it "key" so we are looking for total match of key
	 *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
	 * @return List<User> list of users who have attributes with specific values (behaviour above)
	 *        if no user exist, return empty list of users
	 *        if attributeWithSearchingValues is empty, return allUsers
	 */
	getUsers {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getSearcher().getUsers(ac.getSession(),
					parms.read("attributesWithSearchingValues", LinkedHashMap.class));
		}
	},

	/*#
	 * This method get Map of user Attributes with searching values and try to find all members, which have specific attributes in format for specific VO.
	 * Better information about format below. When there are more than 1 attribute in Map, it means all must be true "looking for all of them" (AND)
	 *
	 * if principal has no rights for operation, throw exception
	 * if principal has no rights for some attribute on specific user, do not return this user
	 * if attributesWithSearchingValues is null or empty, return all members from vo if principal has rights for this operation
	 *
	 * @param userAttributesWithSearchingValues Map<String,String> map of attributes names
	 *        when attribute is type String, so value is string and we are looking for total match (Partial is not supported now, will be supported later by symbol *)
	 *        when attribute is type Integer, so value is integer in String and we are looking for total match
	 *        when attribute is type List<String>, so value is String and we are looking for at least one total or partial matching element
	 *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total match of both or if is it "key" so we are looking for total match of key
	 *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
	 * @return List<Member> list of users who have attributes with specific values (behaviour above)
	 *        if no user exist, return empty list of users
	 *        if attributeWithSearchingValues is empty, return allUsers
	 */
	getMembersByUserAttributes {

		@Override
		public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getSearcher().getMembersByUserAttributes(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					parms.read("userAttributesWithSearchingValues", LinkedHashMap.class));
		}
	};
}
