package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
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
	 * @param securityTeam SecurityTeam Security team to create
	 * @throws SecurityTeamExistsException When name of SecurityTeam is not unique.
	 * @return SecurityTeam Newly create SecurityTeam with <code>id</code> set.
	 */
	createSecurityTeam {
		@Override
		public SecurityTeam call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getSecurityTeamsManager().createSecurityTeam(ac.getSession(), parms.read("securityTeam", SecurityTeam.class));
		}
	},

	/*#
	 * Update existing SecurityTeam name and description by teams <code>id</id>.
	 * Name must be <= 128 and must be unique.
	 *
	 * @param securityTeam SecurityTeam Security team <code>id</code>
	 * @throws SecurityTeamNotExistsException When <code>id</code> of a team doesn't exists in Perun.
	 * @throws SecurityTeamExistsException When new name of security team is not unique.
	 * @return SecurityTeam Team with updated values
	 */
	updateSecurityTeam {
		@Override
		public SecurityTeam call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

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
			ac.stateChangingCheck();

			if (parms.contains("force")) {
				ac.getSecurityTeamsManager().deleteSecurityTeam(ac.getSession(), ac.getSecurityTeamById(parms.readInt("securityTeam")), parms.readBoolean("force"));
			} else {
				ac.getSecurityTeamsManager().deleteSecurityTeam(ac.getSession(),  ac.getSecurityTeamById(parms.readInt("securityTeam")));
			}

			return null;
		}
	},

	/*#
	 * Get existing SecurityTeam by <code>id</id>.
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
	 * Get all managers (members) of SecurityTeam by its <code>id</id>.
	 *
	 * @param securityTeam int Security team <code>id</code>
	 * @return List<User> List of Users who are managers (members) of specified SecurityTeam.
	 */
	getAdmins {
		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getSecurityTeamsManager().getAdmins(ac.getSession(), ac.getSecurityTeamById(parms.readInt("securityTeam")));
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
			ac.stateChangingCheck();

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
			ac.stateChangingCheck();

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
			ac.stateChangingCheck();

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
			ac.stateChangingCheck();

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
	};
}
