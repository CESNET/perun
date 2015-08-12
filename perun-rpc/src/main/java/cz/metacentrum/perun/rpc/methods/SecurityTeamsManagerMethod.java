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
	 * Get list of SecurityTeams by access rights
	 *  - PERUNADMIN : all teams
	 *  - SECURITYADMIN : teams where user is admin
	 *
	 * @param perunSession
	 * @return List of SecurityTeams or empty ArrayList<SecurityTeam>
	 */
	getSecurityTeams {
		@Override
		public List<SecurityTeam> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getSecurityTeamsManager().getSecurityTeams(ac.getSession());
		}
	},

	/*#
	 * get all security teams in perun system
	 *
	 * @param perunSession
	 * @return List of SecurityTeams or empty List
	 */
	getAllSecurityTeams {
		@Override
		public List<SecurityTeam> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getSecurityTeamsManager().getAllSecurityTeams(ac.getSession());
		}
	},

	/*#
	 * Create new SecurityTeam.
	 *
	 * @param perunSession
	 * @param securityTeam SecurityTeam object with prefilled name
	 * @return Newly created Security team with new id
	 */
	createSecurityTeam {
		@Override
		public SecurityTeam call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getSecurityTeamsManager().createSecurityTeam(ac.getSession(), parms.read("securityTeam", SecurityTeam.class));
		}
	},

	/*#
	 * Updates SecurityTeam.
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @return returns updated SecurityTeam
	 */
	updateSecurityTeam {
		@Override
		public SecurityTeam call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getSecurityTeamsManager().updateSecurityTeam(ac.getSession(), parms.read("securityTeam", SecurityTeam.class));
		}
	},

	/*#
	 * Delete SecurityTeam.
	 *
	 * @param perunSession
	 * @param securityTeam
	 */
	deleteSecurityTeam {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getSecurityTeamsManager().deleteSecurityTeam(ac.getSession(),  ac.getSecurityTeamById(parms.readInt("securityTeam")));
			return null;
		}
	},

	/*#
	 * Find existing SecurityTeam by ID.
	 *
	 * @param perunSession
	 * @param id
	 * @return security team with given id
	 */
	getSecurityTeamById {
		@Override
		public SecurityTeam call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getSecurityTeamsManager().getSecurityTeamById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * get all security admins of given security team
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @return list of users which are admis of given security team
	 */
	getAdmins {
		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getSecurityTeamsManager().getAdmins(ac.getSession(), ac.getSecurityTeamById(parms.readInt("securityTeam")));
		}
	},

	/*#
	 * Create group as security admins group of given security team (all users in group will have security admin rights)
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @param group group which members will became a security administrators
	 */
	/*#
	 * create security admin from given user and add him as security admin of given security team
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @param user user who will became a security administrator
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
	 * Remove security admin role for given security team from group
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @param group
	 */
	/*#
	 * Remove security admin role for given security team from user
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @param user
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
	 * Add User to black list of security team to filter him out.
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @param user
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
	 * remove user from blacklist of given security team
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @param user user who will became a security administrator
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
	 * get union of blacklists of all security teams assigned to facility
	 *
	 * @param perunSession
	 * @param facility
	 * @return list of blacklisted users for facility
	 */
	/*#
	 * get list of blacklisted users by security team
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @return lis of blacklisted users by security team
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
