package cz.metacentrum.perun.rpc.methods;

import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.ArrayList;
import java.util.LinkedHashMap;

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
     * Returns user by its ID.
     *
     * @param id int User ID
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
     * Returns all service users in Perun.
     *
     * @return List<User> All Perun service users
     */
    getServiceUsers {

        @Override
        public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getUsersManager().getServiceUsers(ac.getSession());
        }
    },

    /*#
     * Return all serviceUsers who are owned by the user.
     *
     * @param user int User ID
     * @return List<User> Service users for a user
     */
    getServiceUsersByUser {

        @Override
        public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getUsersManager().getServiceUsersByUser(ac.getSession(),
                    ac.getUserById(parms.readInt("user")));
        }
    },

    /*#
     * Return all users who owns the serviceUser.
     *
     * @param serviceUser int Service User ID
     * @return List<User> Users for a service user
     */
    getUsersByServiceUser {

        @Override
        public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getUsersManager().getUsersByServiceUser(ac.getSession(),
                    ac.getUserById(parms.readInt("serviceUser")));
        }
    },

    /*#
     * Add serviceUser owner (the user).
     * @param user int User ID
     * @param serviceUser int Service user ID
     */
    addServiceUser {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();
            ac.getUsersManager().addServiceUserOwner(ac.getSession(),
                    ac.getUserById(parms.readInt("user")),
                    ac.getUserById(parms.readInt("serviceUser")));

            return null;
        }
    },

    /*#
     * Remove serviceUser owner (the user).
     *
     * @param user int User ID
     * @param serviceUser int Service user ID
     */
    removeServiceUser {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();
            ac.getUsersManager().removeServiceUserOwner(ac.getSession(),
                    ac.getUserById(parms.readInt("user")),
                    ac.getUserById(parms.readInt("serviceUser")));

            return null;
        }
    },

    getRichUser {

        @Override
        public RichUser call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getUsersManager().getRichUser(ac.getSession(),
                    ac.getUserById(parms.readInt("user")));
        }
    },

    getRichUserWithAttributes {

        @Override
        public RichUser call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getUsersManager().getRichUserWithAttributes(ac.getSession(),
                    ac.getUserById(parms.readInt("user")));
        }
    },

    getAllRichUsers {

        @Override
        public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getUsersManager().getAllRichUsers(ac.getSession(),
                    parms.readInt("includedServiceUsers") == 1);
        }
    },

    getAllRichUsersWithAttributes {

        @Override
        public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getUsersManager().getAllRichUsersWithAttributes(ac.getSession(),
                    parms.readInt("includedServiceUsers") == 1);
        }
    },

    getRichUsersFromListOfUsers {

        @Override
        public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            return ac.getUsersManager().getRichUsersFromListOfUsers(ac.getSession(),
                    parms.readList("users", User.class));
        }
    },

    getRichUsersFromListOfUsersWithAttributes {

        @Override
        public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            return ac.getUsersManager().getRichUsersWithAttributesFromListOfUsers(ac.getSession(),
                    parms.readList("users", User.class));
        }
    },

    getRichUsersWithoutVoAssigned {

        @Override
        public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getUsersManager().getRichUsersWithoutVoAssigned(ac.getSession());
        }
    },

    getRichUsersWithAttributes {
        @Override
        public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

            if (parms.contains("attrsNames[]")) {
                return ac.getUsersManager().getAllRichUsersWithAttributes(ac.getSession(),
                        parms.readInt("includedServiceUsers") == 1,
                        parms.readList("attrsNames", String.class));
            } else {
                return ac.getUsersManager().getAllRichUsersWithAttributes(ac.getSession(),
                        parms.readInt("includedServiceUsers") == 1, null);
            }
        }
    },

    findRichUsersWithAttributes {
        @Override
        public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

            if (parms.contains("attrsNames[]")) {
                return ac.getUsersManager().findRichUsersWithAttributes(ac.getSession(),
                        parms.readString("searchString"),
                        parms.readList("attrsNames", String.class));
            } else {
                return ac.getUsersManager().findRichUsersWithAttributes(ac.getSession(),
                        parms.readString("searchString"), null);
            }

        }
    },

    getRichUsersWithoutVoWithAttributes {
        @Override
        public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

            if (parms.contains("attrsNames[]")) {
                return ac.getUsersManager().getRichUsersWithoutVoWithAttributes(ac.getSession(),
                        parms.readList("attrsNames", String.class));
            } else {
                return ac.getUsersManager().getRichUsersWithoutVoWithAttributes(ac.getSession(), null);
            }
        }
    },

    findRichUsersWithoutSpecificVoWithAttributes {
        @Override
        public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

            if (parms.contains("attrsNames[]")) {
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
   * Deletes a user.
   * 
   * @param user int User ID
   */
  /*#
   * Deletes a user (force).
   * Also removes associeted members.
   * 
   * @param user int User ID
   * @param force int Parameter force must == 1
   */
    deleteUser {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            if (parms.contains("force") && parms.readInt("force") == 1) {
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
     * @param user int User ID
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
     * @param user int User ID
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
     * Remove user's external sources.
     * @param user int User ID
     * @param userExtSource int UserExtSource ID
     */
    removeUserExtSource {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getUsersManager().removeUserExtSource(ac.getSession(),
                    ac.getUserById(parms.readInt("user")),
                    ac.getUserExtSourceById(parms.readInt("userExtSource")));
            return null;
        }
    },

    /*#
     * Get the user ext source by its id.
     *
     * @param userExtSource int UserExtSource ID
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
     * @param member int Member ID
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

    findRichUsers {

        @Override
        public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getUsersManager().findRichUsers(ac.getSession(),
                    parms.readString("searchString"));
        }
    },

    /*#
     * Return list of users who matches the searchString, searching name, email and logins
     * and are not member in specific VO.
     *
     * @param vo int VO ID
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
    getUsersByAttribute {
        @Override
        public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
            if (parms.contains("attributeName")) {
                if (parms.contains("attributeValue") || parms.contains("attributeValue[]")) {
                    String attributeName = parms.readString("attributeName");
                    Attribute attr = new Attribute(ac.getAttributesManager().getAttributeDefinition(ac.getSession(), attributeName));

                    if(attr.getType().equals(Integer.class.getName())) {
                        attr.setValue(parms.readInt("attributeValue"));
                    } else if(attr.getType().equals(String.class.getName())) {
                        attr.setValue(parms.readString("attributeValue"));
                        return ac.getUsersManager().getUsersByAttribute(ac.getSession(),attr);
                    } else if(attr.getType().equals(ArrayList.class.getName())) {
                        attr.setValue(parms.readList("attributeValue", String.class));
                    } else if(attr.getType().equals(LinkedHashMap.class.getName())) {
                        attr.setValue(parms.read("attributeValue", LinkedHashMap.class));
                    } else {
                        throw new RpcException(RpcException.Type.CANNOT_SERIALIZE_VALUE, "attributeValue is not the same type like value of attribute with the attributeName.");
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
     * @param attribute Attribute JSON object
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
     * @param user int User ID
     * @return List<VirtualOrganization> Found VOs
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
     * @param user int User ID
     * @return List<VirtualOrganization> Found VOs
     */
    getVosWhereUserIsMember {

        @Override
        public List<Vo> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getUsersManager().getVosWhereUserIsMember(ac.getSession(),
                    ac.getUserById(parms.readInt("user")));
        }
    },

    /*#
     * Returns list of Groups, where the user is an Administrator.
     *
     * @param user int User ID
     * @return List<Group> Found Groups
     */
    getGroupsWhereUserIsAdmin {

        @Override
        public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getUsersManager().getGroupsWhereUserIsAdmin(ac.getSession(),
                    ac.getUserById(parms.readInt("user")));
        }
    },

    /*#
     * Get all resources from the facility which have the user access on.
     *
     * @param user int User ID
     * @param facility int Facility ID
     * @return List<Resource> Allowed resources
     */
  /*#
   * Get all resources which have the user access on.
   * 
   * @param user int User ID
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
     * Checks if the login is available in the namespace.
     *
     * @param loginNamespace String Namespace
     * @param login String Login
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
     * @param user int User ID
     */
    makeUserPerunAdmin {
        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            User user = ac.getUserById(parms.readInt("user"));
            ac.getUsersManager().makeUserPerunAdmin(ac.getSession(), user);
            return null;
        }
    },


    /*#
     * Changes user password in defined login-namespace.
     *
     * @param user int User ID
     * @param loginNamespace String Namespace
     * @param newPassword String New password
     * @param checkOldPassword int checkOldPassword must be 0
     */
  /*#
   * Changes user password in defined login-namespace.
   * You must send the old password, which will be checked
   * 
   * @param user int User ID
   * @param loginNamespace String Namespace
   * @param oldPassword String Old password which will be checked.
   * @param newPassword String New password
   * @param checkOldPassword int checkOldPassword must be 1
   */
    changePassword {
        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            User user = ac.getUserById(parms.readInt("user"));

            if (parms.readInt("checkOldPassword") == 1) {
                ac.getUsersManager().changePassword(ac.getSession(), user, parms.readString("loginNamespace"), parms.readString("oldPassword"), parms.readString("newPassword"), true);
            } else {
                ac.getUsersManager().changePassword(ac.getSession(), user, parms.readString("loginNamespace"), parms.readString("oldPassword"), parms.readString("newPassword"), false);
            }
            return null;
        }
    },
    /*#
     * Creates a password.
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
     * Reserves a random password.
     * 
     * @param user int User ID
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
     * Reserves a password.
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
     * Validates a password.
     *
     * @param login String Login
     * @param namespace String Namespace
     * @param password String password
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
     * Validates a password and set ext sources
     *
     * @param login String Login
     * @param namespace String Namespace
     * @param user int User ID
     * @param password String password
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
     * !! Works only for service users !!
     *
     * @param user Integer User id
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
    };
}