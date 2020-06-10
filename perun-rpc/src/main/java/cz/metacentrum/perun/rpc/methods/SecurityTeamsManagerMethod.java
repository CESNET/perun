package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

import java.util.List;

public enum SecurityTeamsManagerMethod implements ManagerMethod {

	/*#
	 * List SecurityTeams your are member of or all for PerunAdmin.
	 *
	 * @return List<SecurityTeam> List of your security teams.
	 */
	getSecurityTeams {
		@Override
		public List<SecurityTeam> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getSecurityTeamsManager().getSecurityTeams(ac.getSession());
		}
	},

	/*#
	 * List all SecurityTeams in Perun.
	 *
	 * @return List<SecurityTeam> List of all security teams.
	 */
	getAllSecurityTeams {
		@Override
		public List<SecurityTeam> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getSecurityTeamsManager().getAllSecurityTeams(ac.getSession());
		}
	},

	/*#
	 * Create SecurityTeam.
	 *
	 * SecurityTeam object must contain name which must match [-_a-zA-z0-9.]+ and not be longer than 128 characters.
	 * Parameter description is optional.
	 * Other parameters are ignored.
	 *
	 * @param securityTeam SecurityTeam Security team to create
	 * @throw SecurityTeamExistsException When name of SecurityTeam is not unique.
	 * @return SecurityTeam Newly create SecurityTeam with <code>id</code> set.
	 * @exampleParam securityTeam { "name" : "My_new_security-team2" }
	 */
	/*#
	 * Create SecurityTeam.
	 *
	 * @param name String name which must match [-_a-zA-z0-9.]+ and not be longer than 128 characters.
	 * @param description String description
	 * @throw SecurityTeamExistsException When name of SecurityTeam is not unique.
	 * @return SecurityTeam Newly create SecurityTeam with <code>id</code> set.
	 * @exampleParam name "My_new_security-team2"
	 */
	createSecurityTeam {
		@Override
		public SecurityTeam call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			if (parms.contains("securityTeam")) {
				return ac.getSecurityTeamsManager().createSecurityTeam(ac.getSession(), parms.read("securityTeam", SecurityTeam.class));
			} else if (parms.contains("name") && parms.contains("description")) {
				String name = parms.readString("name");
				String description = parms.readString("description");
				SecurityTeam securityTeam = new SecurityTeam(name, description);
				return ac.getSecurityTeamsManager().createSecurityTeam(ac.getSession(), securityTeam);
			} else {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER);
			}
		}
	},

	/*#
	 * Update existing SecurityTeam name and description by teams <code>id</code>.
	 * Name must be <= 128 and must be unique.
	 *
	 * @param securityTeam SecurityTeam Security team <code>id</code>
	 * @throw SecurityTeamNotExistsException When <code>id</code> of a team doesn't exists in Perun.
	 * @throw SecurityTeamExistsException When new name of security team is not unique.
	 * @return SecurityTeam Team with updated values
	 */
	updateSecurityTeam {
		@Override
		public SecurityTeam call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			return ac.getSecurityTeamsManager().updateSecurityTeam(ac.getSession(), parms.read("securityTeam", SecurityTeam.class));
		}
	},

	/*#
	 * Delete SecurityTeam by its <code>id</code>. If force is <code>true</code> team is deleted even if it
	 * has some users on blacklist or is assigned to some facility.
	 *
	 * @param securityTeam int Security team <code>id</code>
	 * @param force boolean <code>true</code> if force delete
	 */
	/*#
	 * Delete SecurityTeam by its <code>id</code>. If team has any users on blacklist or is assigned
	 * some facility, it is not deleted.
	 *
	 * @param securityTeam int Security team <code>id</code>
	 */
	deleteSecurityTeam {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			if (parms.contains("force")) {
				ac.getSecurityTeamsManager().deleteSecurityTeam(ac.getSession(), ac.getSecurityTeamById(parms.readInt("securityTeam")), parms.readBoolean("force"));
			} else {
				ac.getSecurityTeamsManager().deleteSecurityTeam(ac.getSession(),  ac.getSecurityTeamById(parms.readInt("securityTeam")));
			}

			return null;
		}
	},

	/*#
	 * Get existing SecurityTeam by <code>id</code>.
	 *
	 * @param id int Security team <code>id</code>
	 * @return SecurityTeam Team with given <code>id</code>
	 */
	getSecurityTeamById {
		@Override
		public SecurityTeam call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getSecurityTeamsManager().getSecurityTeamById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Get all managers (members) of SecurityTeam by its <code>id</code>.
	 *
	 * @param securityTeam int Security team <code>id</code>
	 * @return List<User> List of Users who are managers (members) of specified SecurityTeam.
	 */
	/*#
	 * Get managers of SecurityTeam by its <code>id</code>.
	 * @param securityTeam int Security team <code>id</code>
	 * @param onlyDirectAdmins boolean if true, get only direct SecurityTeam administrators (if false, get both direct and indirect)
	 * @return List<User> List of Users who are managers (members) of specified SecurityTeam.
	 */
	getAdmins {
		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("onlyDirectAdmins")) {                       
				return ac.getSecurityTeamsManager().getAdmins(ac.getSession(), 
					ac.getSecurityTeamById(parms.readInt("securityTeam")),
					 parms.readBoolean("onlyDirectAdmins"));
			} else {
				return ac.getSecurityTeamsManager().getAdmins(ac.getSession(),
				ac.getSecurityTeamById(parms.readInt("securityTeam")),false);
			}
        	}             
	},

	/*#
	 * Get all SecurityTeam groups of admins.
	 *
	 * @param  securityTeam int SecurityTeam <code>id</code>
	 * @return List<Group> admins
	 */
	getAdminGroups {

		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getSecurityTeamsManager().getAdminGroups(ac.getSession(),
			ac.getSecurityTeamById(parms.readInt("securityTeam")));
		}
	},

	/*#
	 * Add User as a manager to SecurityTeam
	 *
	 * @param securityTeam int <code>id</code> of SecurityTeam to add manager (member) to
	 * @param user int <code>id</code> of User to be added as a manager (member) of SecurityTeam
	 */
	/*#
	 * Add group as a manager to SecurityTeam
	 *
	 * @param securityTeam int <code>id</code> of SecurityTeam to add manager (member) to
	 * @param group int <code>id</code> of Group to be added as a manager (member) of SecurityTeam
	 */
	addAdmin {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			if (parms.contains("group")) {
				ac.getSecurityTeamsManager().addAdmin(ac.getSession(), ac.getSecurityTeamById(parms.readInt("securityTeam")),
						ac.getGroupById(parms.readInt("group")));
			} else {
				ac.getSecurityTeamsManager().addAdmin(ac.getSession(), ac.getSecurityTeamById(parms.readInt("securityTeam")),
						ac.getUserById(parms.readInt("user")));
			}
			return null;
		}
	},

	/*#
	 * Remove User as a manager from SecurityTeam.
	 *
	 * @param securityTeam int <code>id</code> of SecurityTeam to remove manager (member) from
	 * @param user int <code>id</code> of User to be removed as a manager (member) of SecurityTeam
	 */
	/*#
	 * Remove group as a manager from SecurityTeam.
	 *
	 * @param securityTeam int <code>id</code> of SecurityTeam to remove manager (member) from
	 * @param group int <code>id</code> of Group to be removed as a manager (member) of SecurityTeam
	 */
	removeAdmin {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			if (parms.contains("group")) {
				ac.getSecurityTeamsManager().removeAdmin(ac.getSession(), ac.getSecurityTeamById(parms.readInt("securityTeam")),
						ac.getGroupById(parms.readInt("group")));
			} else {
				ac.getSecurityTeamsManager().removeAdmin(ac.getSession(), ac.getSecurityTeamById(parms.readInt("securityTeam")),
						ac.getUserById(parms.readInt("user")));
			}
			return null;
		}
	},

	/*#
	 * Add user to blacklist of given SecurityTeam
	 *
	 * @param securityTeam int <code>id</code> of SecurityTeam to add user to blacklist
	 * @param user int <code>id</code> of User to be added to blacklist of SecurityTeam
	 */
	addUserToBlacklist {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getSecurityTeamsManager().addUserToBlacklist(ac.getSession(), ac.getSecurityTeamById(parms.readInt("securityTeam")),
					ac.getUserById(parms.readInt("user")),
					parms.readString("description"));
			return null;
		}
	},

	/*#
	 * Remove user from blacklist of given SecurityTeam
	 *
	 * @param securityTeam int <code>id</code> of SecurityTeam to remove user from blacklist
	 * @param user int <code>id</code> of User to be removed from blacklist of SecurityTeam
	 */
	removeUserFromBlacklist {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getSecurityTeamsManager().removeUserFromBlacklist(ac.getSession(), ac.getSecurityTeamById(parms.readInt("securityTeam")),
					ac.getUserById(parms.readInt("user")));
			return null;
		}
	},

	/*#
	 * Get blacklisted users on selected Facility. List is a union of all blacklists of
	 * SecurityTeams assigned to selected Facility.
	 *
	 * @param facility int <code>id</code> of Facility to get blacklist for
	 * @return List<User> List of users blacklisted on selected facility.
	 */
	/*#
	 * Get users blacklisted by selected SecurityTeam.
	 *
	 * @param securityTeam int <code>id</code> of SecurityTeam to get blacklist for
	 * @return List<User> Blacklisted users
	 */
	getBlacklist {
		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("facility")) {
				return ac.getSecurityTeamsManager().getBlacklist(ac.getSession(), ac.getFacilityById(parms.readInt("facility")));
			} else {
				return ac.getSecurityTeamsManager().getBlacklist(ac.getSession(), ac.getSecurityTeamById(parms.readInt("securityTeam")));
			}
		}
	},

	/*#
	 * Get blacklisted users on selected Facility. List consists of Pairs. Left is a item from union of all blacklists of
	 * SecurityTeams assigned to selected Facility and right item is a description why the user is on the blacklist.
	 *
	 * @param facility int <code>id</code> of Facility to get blacklist for
	 * @return List<Pair<User,String>> List of users blacklisted on selected facility.
	 */
	/*#
	 * Get users blacklisted by selected SecurityTeam with a description why the users are on the blacklist.
	 *
	 * @param securityTeam int <code>id</code> of SecurityTeam to get blacklist for
	 * @return List<Pair<User,String>> Blacklisted users with description
	 */
	getBlacklistWithDescription {
		@Override
		public List<Pair<User,String>> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("facility")) {
				return ac.getSecurityTeamsManager().getBlacklistWithDescription(ac.getSession(), ac.getFacilityById(parms.readInt("facility")));
			} else {
				return ac.getSecurityTeamsManager().getBlacklistWithDescription(ac.getSession(), ac.getSecurityTeamById(parms.readInt("securityTeam")));
			}
		}
	};
}
