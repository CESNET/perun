package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BlockedLogin;
import cz.metacentrum.perun.core.api.BlockedLoginsPageQuery;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.RichUserExtSource;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Sponsor;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersPageQuery;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
      return ac.getUsersManager()
          .getUserByUserExtSource(ac.getSession(), parms.read("userExtSource", UserExtSource.class));
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
      return ac.getUsersManager().getUserByExtSourceNameAndExtLogin(ac.getSession(), parms.readString("extSourceName"),
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
      return ac.getUsersManager().getUserById(ac.getSession(), parms.readInt("id"));
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
   * Get page of users with the given attributes.
   * Query parameter specifies offset, page size, sorting order, and sorting column, by default it finds all users,
   * but the search can be customized by optional parameters: whether to return only users without vo, string to
   * search users by (by default it searches in names, user and member ids, user uuids, emails, logins of member or
   * other attributes based on perun configuration), facility id, vo id, service id and resource id to filter users to
   * search only for those assigned to these entities, and option whether to return only allowed users.
   *
   * @param query UsersPageQuery Query with page information
   * @param attrNames List<String> List of attribute names
   *
   * @return Paginated<RichUser> page of requested rich users
   * @throw ResourceNotExistsException When the Resource specified by <code>id</code> in query does not exist.
   * @throw VoNotExistsException When the Vo specified by <code>id</code> in query does not exist.
   * @throw FacilityNotExistsException When the Facility specified by <code>id</code> in query does not exist.
   * @throw ServiceNotExistsException When the Service specified by <code>id</code> in query does not exist.
   */
  getUsersPage {
    @Override
    public Object call(ApiCaller ac, Deserializer params) throws PerunException {
      return ac.getUsersManager().getUsersPage(ac.getSession(), params.read("query", UsersPageQuery.class),
          params.readList("attrNames", String.class));
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
   * Gets users sponsoring a given user in a VO.
   *
   * @param member int member id
   * @param attrNames List<String> names of attributes to return, empty to return all attributes
   * @return List<Sponsor> sponsors
   */
  /*#
   * Gets users sponsoring a given user in a VO.
   *
   * @param vo int VO ID
   * @param extSourceName String external source name, usually SAML IdP entityID
   * @param extLogin String external source login, usually eduPersonPrincipalName
   * @param attrNames List<String> names of attributes to return, empty to return all attributes
   * @return List<Sponsor> sponsors
   */
  getSponsorsForMember {
    @Override
    public List<Sponsor> call(ApiCaller ac, Deserializer params) throws PerunException {
      Member member = null;
      if (params.contains("member")) {
        member = ac.getMemberById(params.readInt("member"));
      } else if (params.contains("vo") && params.contains("extSourceName") && params.contains("extLogin")) {
        Vo vo = ac.getVoById(params.readInt("vo"));
        User user = ac.getUsersManager()
            .getUserByExtSourceNameAndExtLogin(ac.getSession(), params.readString("extSourceName"),
                params.readString("extLogin"));
        member = ac.getMembersManager().getMemberByUser(ac.getSession(), vo, user);
      }
      List<String> attrNames = params.contains("attrNames") ? params.readList("attrNames", String.class) : null;
      return ac.getUsersManager().getSponsorsForMember(ac.getSession(), member, attrNames);
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
      return ac.getUsersManager().getSpecificUsersByUser(ac.getSession(), ac.getUserById(parms.readInt("user")));
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
      return ac.getUsersManager()
          .getUsersBySpecificUser(ac.getSession(), ac.getUserById(parms.readInt("specificUser")));
    }
  },

  /*#
   * Return unanonymized users who owns the specific user.
   *
   * @param specificUser int Specific User <code>id</code>
   * @return List<User> Users for a service user
   */
  getUnanonymizedUsersBySpecificUser {
    @Override
    public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getUsersManager().getUnanonymizedUsersBySpecificUser(
        ac.getSession(), ac.getUserById(parms.readInt("specificUser"))
      );
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
      parms.stateChangingCheck();
      ac.getUsersManager().addSpecificUserOwner(ac.getSession(), ac.getUserById(parms.readInt("user")),
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
      parms.stateChangingCheck();
      ac.getUsersManager().removeSpecificUserOwner(ac.getSession(), ac.getUserById(parms.readInt("user")),
          ac.getUserById(parms.readInt("specificUser")));

      return null;
    }
  },

  /*#
   * Set specific user type for specific user and set ownership of this user for the owner.
   *
   * @param specificUser int User <code>id</code>
   * @param specificUserType String specific user type
   * @param owner int User <code>id</code>
   * @return User user with specific type set
   */
  setSpecificUser {
    @Override
    public User call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      User owner = ac.getUserById(parms.readInt("owner"));
      User specificUser = ac.getUserById(parms.readInt("specificUser"));
      SpecificUserType specificUserType = SpecificUserType.valueOf(parms.readString("specificUserType"));

      return ac.getUsersManager().setSpecificUser(ac.getSession(), specificUser, specificUserType, owner);
    }
  },

  /*#
   * Remove all ownerships of this specific user and unset this specific user type from this specific user.
   *
   * @param specificUser int User <code>id</code>
   * @param specificUserType String specific user type
   * @return User user without specific user type set
   *
   * @throw ServiceOnlyRoleAssignedException when trying to unset service flag from a user with service only role
   */
  unsetSpecificUser {
    @Override
    public User call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      User specificUser = ac.getUserById(parms.readInt("specificUser"));
      SpecificUserType specificUserType = SpecificUserType.valueOf(parms.readString("specificUserType"));

      return ac.getUsersManager().unsetSpecificUser(ac.getSession(), specificUser, specificUserType);
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
      return ac.getUsersManager().getRichUser(ac.getSession(), ac.getUserById(parms.readInt("user")));
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
      return ac.getUsersManager().getRichUserWithAttributes(ac.getSession(), ac.getUserById(parms.readInt("user")));
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
      return ac.getUsersManager().getAllRichUsers(ac.getSession(), parms.readBoolean("includedSpecificUsers"));
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
      return ac.getUsersManager()
          .getAllRichUsersWithAttributes(ac.getSession(), parms.readBoolean("includedSpecificUsers"));
    }
  },

  /*#
   * Returns rich users without attributes by their IDs.
   *
   * @param ids List<Integer> list of users IDs
   * @return List<RichUser> rich users with specified IDs
   */
  getRichUsersByIds {
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getUsersManager().getRichUsersByIds(ac.getSession(), parms.readList("ids", Integer.class));
    }
  },

  /*#
   * Returns rich users with attributes by their IDs.
   *
   * @param ids List<Integer> list of users IDs
   * @return List<RichUser> rich users with specified IDs
   */
  getRichUsersWithAttributesByIds {
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getUsersManager()
          .getRichUsersWithAttributesByIds(ac.getSession(), parms.readList("ids", Integer.class));
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
        return ac.getUsersManager()
            .getAllRichUsersWithAttributes(ac.getSession(), parms.readBoolean("includedSpecificUsers"),
                parms.readList("attrsNames", String.class));
      } else {
        return ac.getUsersManager()
            .getAllRichUsersWithAttributes(ac.getSession(), parms.readBoolean("includedSpecificUsers"), null);
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
        return ac.getUsersManager().findRichUsersWithAttributes(ac.getSession(), parms.readString("searchString"),
            parms.readList("attrsNames", String.class));
      } else {
        return ac.getUsersManager()
            .findRichUsersWithAttributes(ac.getSession(), parms.readString("searchString"), null);
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
        return ac.getUsersManager()
            .getRichUsersWithoutVoWithAttributes(ac.getSession(), parms.readList("attrsNames", String.class));
      } else {
        return ac.getUsersManager().getRichUsersWithoutVoWithAttributes(ac.getSession(), null);
      }
    }
  },

  /*#
   * Return list of RichUsers who matches the searchString and are not member in specific VO and with selected
   * attributes.
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
        return ac.getUsersManager()
            .findRichUsersWithoutSpecificVoWithAttributes(ac.getSession(), ac.getVoById(parms.readInt("vo")),
                parms.readString("searchString"), parms.readList("attrsNames", String.class));
      } else {
        return ac.getUsersManager()
            .findRichUsersWithoutSpecificVoWithAttributes(ac.getSession(), ac.getVoById(parms.readInt("vo")),
                parms.readString("searchString"), null);
      }
    }
  },

  /*#
   * Deletes a user. User is not deleted, if is member of any VO or is associated with any service identity.
   *
   * @param user int User <code>id</code>
   * @throw RelationExistsException             if user has some members assigned
   * @throw MemberAlreadyRemovedException       if there is at least 1 member deleted but not affected by deleting
   * from DB
   * @throw UserAlreadyRemovedException         if there are no rows affected by deleting user in DB
   * @throw SpecificUserAlreadyRemovedException if there are no rows affected by deleting specific user in DB
   * @throw DeletionNotSupportedException       if the deletion of users is not supported at this instance
   */
  /*#
   * Deletes a user (force).
   * Also removes associated members.
   *
   * @param user int User <code>id</code>
   * @param force boolean If true, use force deletion.
   * @throw RelationExistsException             if forceDelete is false and the user has some members assigned
   * @throw MemberAlreadyRemovedException       if there is at least 1 member deleted but not affected by deleting
   * from DB
   * @throw UserAlreadyRemovedException         if there are no rows affected by deleting user in DB
   * @throw SpecificUserAlreadyRemovedException if there are no rows affected by deleting specific user in DBn
   * @throw DeletionNotSupportedException       if the deletion of users is not supported at this instance
   */
  deleteUser {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("force") && parms.readBoolean("force")) {
        ac.getUsersManager().deleteUser(ac.getSession(), ac.getUserById(parms.readInt("user")), true);
      } else {
        ac.getUsersManager().deleteUser(ac.getSession(), ac.getUserById(parms.readInt("user")));
      }
      return null;
    }
  },

  /*#
   * Anonymizes user - according to configuration, each of user's attributes is either
   * anonymized, kept untouched or deleted. Also deletes other user's related data, e.g.
   * authorships of users publications, mail change and password reset requests, bans...
   *
   * @param user int User <code>id</code>
   * @throw UserNotExistsException When the User specified by <code>id</code> doesn't exist.
   * @throw RelationExistsException When the User has some members assigned.
   * @throw AnonymizationNotSupportedException When an attribute should be anonymized but its module doesn't specify
   * the anonymization process or if the anonymization is not supported at this instance.
   */
  /*#
   * Anonymizes user (force) - according to configuration, each of user's attributes is either
   * anonymized, kept untouched or deleted. Also deletes other user's related data, e.g.
   * authorships of users publications, mail change and password reset requests, bans...
   * Also removes associated members.
   *
   * @param user int User <code>id</code>
   * @param force boolean If true, use force anonymization
   * @throw UserNotExistsException When the User specified by <code>id</code> doesn't exist.
   * @throw RelationExistsException When the User has some members assigned.
   * @throw AnonymizationNotSupportedException When an attribute should be anonymized but its module doesn't specify
   * the anonymization process or if the anonymization is not supported at this instance.
   */
  anonymizeUser {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("force")) {
        ac.getUsersManager()
            .anonymizeUser(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readBoolean("force"));
      } else {
        ac.getUsersManager().anonymizeUser(ac.getSession(), ac.getUserById(parms.readInt("user")), false);
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
      parms.stateChangingCheck();

      return ac.getUsersManager().updateUser(ac.getSession(), parms.read("user", User.class));
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
      parms.stateChangingCheck();

      return ac.getUsersManager().updateNameTitles(ac.getSession(), parms.read("user", User.class));
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
      parms.stateChangingCheck();

      return ac.getUsersManager()
          .updateUserExtSource(ac.getSession(), parms.read("userExtSource", UserExtSource.class));
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
      return ac.getUsersManager().getUserExtSources(ac.getSession(), ac.getUserById(parms.readInt("user")));
    }
  },

  /*#
   * Gets list of all user's external sources with attributes.
   *
   * @param user int User <code>id</code>
   * @return List<UserExtSource> list of user's external sources with attributes
   */
  /*#
   * Gets list of all user's external sources with specified attributes. If attrsNames is empty
   * return no attributes. If attrsNames is null, this methods returns all attributes.
   *
   * @param user int User <code>id</code>
   * @param attrsNames List<String> Attribute names
   * @return List<UserExtSource> list of user's external sources with specified attributes
   */
  getRichUserExtSources {
    @Override
    public List<RichUserExtSource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("attrsNames")) {
        return ac.getUsersManager().getRichUserExtSources(ac.getSession(), ac.getUserById(parms.readInt("user")),
            parms.readList("attrsNames", String.class));
      } else {
        return ac.getUsersManager().getRichUserExtSources(ac.getSession(), ac.getUserById(parms.readInt("user")));
      }
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
      parms.stateChangingCheck();

      return ac.getUsersManager().addUserExtSource(ac.getSession(), ac.getUserById(parms.readInt("user")),
          parms.read("userExtSource", UserExtSource.class));
    }
  },

  /*#
   * Adds user's external source and its attributes.
   * @param user int User <code>id</code>
   * @param userExtSource UserExtSource JSON object
   * @param attributes List<Attribute> list of Attribute
   * @return RichUserExtSource Newly added RichUserExtSource
   */
  addUserExtSourceWithAttributes {
    @Override
    public RichUserExtSource call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      return ac.getUsersManager().addUserExtSourceWithAttributes(ac.getSession(), ac.getUserById(parms.readInt("user")),
          parms.read("userExtSource", UserExtSource.class), parms.readList("attributes", Attribute.class));
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
      parms.stateChangingCheck();

      if (parms.contains("force") && parms.readBoolean("force")) {
        ac.getUsersManager().removeUserExtSource(ac.getSession(), ac.getUserById(parms.readInt("user")),
            ac.getUserExtSourceById(parms.readInt("userExtSource")), true);
      } else {
        ac.getUsersManager().removeUserExtSource(ac.getSession(), ac.getUserById(parms.readInt("user")),
            ac.getUserExtSourceById(parms.readInt("userExtSource")));
      }

      return null;
    }
  },

  /*#
   * Remove user's external sources.
   * Persistent UserExtSources are not removed unless <code>force</code> param is present and set to <code>true</code>.
   * @param user int User <code>id</code>
   * @param userExtSources List<Integer> UserExtSource <code>id</code>
   * @param force boolean If true, use force deletion.
   */
  removeUserExtSources {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      List<UserExtSource> sources = new ArrayList<>();

      for (Integer sourceId : parms.readList("userExtSources", Integer.class)) {
        sources.add(ac.getUserExtSourceById(sourceId));
      }

      ac.getUsersManager().removeUserExtSources(ac.getSession(), ac.getUserById(parms.readInt("user")), sources,
          parms.contains("force") && parms.readBoolean("force"));

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
      parms.stateChangingCheck();

      ac.getUsersManager().moveUserExtSource(ac.getSession(), ac.getUserById(parms.readInt("sourceUser")),
          ac.getUserById(parms.readInt("targetUser")), ac.getUserExtSourceById(parms.readInt("userExtSource")));
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
      return ac.getUsersManager().getUserExtSourceById(ac.getSession(), parms.readInt("userExtSource"));
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
      return ac.getUsersManager().getUserExtSourceByExtLogin(ac.getSession(), parms.read("extSource", ExtSource.class),
          parms.readString("extSourceLogin"));
    }
  },

  /*#
   * Gets user's external source by the user's external login and external source name
   *
   * @param extSourceName String Name of ext source (eg. entityID of IdP)
   * @param extSourceLogin String Login
   * @return UserExtSource UserExtSource found user's external source
   */
  getUserExtSourceByExtLoginAndExtSourceName {
    @Override
    public UserExtSource call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getUsersManager()
          .getUserExtSourceByExtLogin(ac.getSession(), ac.getExtSourceByName(parms.readString("extSourceName")),
              parms.readString("extSourceLogin"));
    }
  },

  /*#
   * Returns user ext sources by their IDs.
   *
   * @param ids List<Integer> list of user ext sources IDs
   * @return List<UserExtSource> user ext sources with specified IDs
   */
  getUserExtSourcesByIds {
    @Override
    public List<UserExtSource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getUsersManager().getUserExtSourcesByIds(ac.getSession(), parms.readList("ids", Integer.class));
    }
  },

  /*#
   * Return userExtSource for specific attribute definition (specified by id) and unique value.
   * If not found, throw and exception.
   *
   * It looks for exactly one value of the specific attribute type:
   * - Integer -> exactly match
   * - String -> exactly match
   * - Map -> exactly match of "key=value"
   * - ArrayList -> exactly match of one of the value
   *
   * @param attributeId int value used for founding attribute definition which has to exists, be unique and in
   * userExtSource namespace
   * @param attributeValue String value used for searching
   * @return UserExtSource object found by attribute id and it's unique value
   */
  /*#
   * Return userExtSource for specific attribute definition (specified by name) and unique value.
   * If not found, throw and exception.
   *
   * It looks for exactly one value of the specific attribute type:
   * - Integer -> exactly match
   * - String -> exactly match
   * - Map -> exactly match of "key=value"
   * - ArrayList -> exactly match of one of the value
   *
   * @param attributeName String value used for founding attribute definition which has to exists, be unique and in
   * userExtSource namespace
   * @param attributeValue String value used for searching
   * @return UserExtSource object found by attribute name and it's unique value
   */
  getUserExtSourceByUniqueAttributeValue {
    @Override
    public UserExtSource call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("attributeId")) {
        return ac.getUsersManager()
            .getUserExtSourceByUniqueAttributeValue(ac.getSession(), parms.readInt("attributeId"),
                parms.readString("attributeValue"));
      }
      if (parms.contains("attributeName")) {
        return ac.getUsersManager()
            .getUserExtSourceByUniqueAttributeValue(ac.getSession(), parms.readString("attributeName"),
                parms.readString("attributeValue"));
      }
      throw new RpcException(RpcException.Type.MISSING_VALUE, "attrId or attrName");
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
      return ac.getUsersManager().getUserByMember(ac.getSession(), ac.getMemberById(parms.readInt("member")));
    }
  },

  /*#
   * Returns list of users who matches the searchString, searching name, id, uuid, email, logins.
   *
   * @param searchString String String to search by
   * @return List<User> Found users
   */
  findUsers {
    @Override
    public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getUsersManager().findUsers(ac.getSession(), parms.readString("searchString"));
    }
  },

  /*#
   * Returns list of RichUsers with attributes who matches the searchString, searching name, id, uuid, email, logins.
   *
   * @param searchString String searched string
   * @return List<RichUser> list of RichUsers
   */
  findRichUsers {
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getUsersManager().findRichUsers(ac.getSession(), parms.readString("searchString"));
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
      return ac.getUsersManager().getUsersWithoutSpecificVo(ac.getSession(), ac.getVoById(parms.readInt("vo")),
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
        return ac.getUsersManager().findUsersByName(ac.getSession(), parms.readString("searchString"));
      } else if (parms.contains("titleBefore") && parms.contains("firstName") && parms.contains("middleName") &&
                 parms.contains("lastName") && parms.contains("titleAfter")) {
        return ac.getUsersManager()
            .findUsersByName(ac.getSession(), parms.readString("titleBefore"), parms.readString("firstName"),
                parms.readString("middleName"), parms.readString("lastName"), parms.readString("titleAfter"));
      } else {
        throw new RpcException(RpcException.Type.MISSING_VALUE,
            "searchString or (titleBefore and firstName and middleName and lastName and titleAfter)");
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
          Attribute attr =
              new Attribute(ac.getAttributesManager().getAttributeDefinition(ac.getSession(), attributeName));

          if (attr.getType().equals(Integer.class.getName())) {
            attr.setValue(parms.readInt("attributeValue"));
          } else if (attr.getType().equals(String.class.getName())) {
            attr.setValue(parms.readString("attributeValue"));
          } else if (attr.getType().equals(Boolean.class.getName())) {
            attr.setValue(parms.readBoolean("attributeValue"));
          } else if (attr.getType().equals(ArrayList.class.getName())) {
            attr.setValue(parms.readList("attributeValue", String.class));
          } else if (attr.getType().equals(LinkedHashMap.class.getName())) {
            attr.setValue(parms.read("attributeValue", LinkedHashMap.class));
          } else {
            throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE,
                "attributeValue is not the same type like value of attribute with the attributeName.");
          }
          return ac.getUsersManager().getUsersByAttribute(ac.getSession(), attr);
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
          return ac.getUsersManager().getUsersByAttributeValue(ac.getSession(), parms.readString("attributeName"),
              parms.readString("attributeValue"));
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
   * Including VOs, where the user is a VALID member of authorized group.
   *
   * @param user int User <code>id</code>
   * @return List<Vo> Found VOs
   */
  getVosWhereUserIsAdmin {
    @Override
    public List<Vo> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getUsersManager().getVosWhereUserIsAdmin(ac.getSession(), ac.getUserById(parms.readInt("user")));
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
      return ac.getUsersManager().getVosWhereUserIsMember(ac.getSession(), ac.getUserById(parms.readInt("user")));
    }
  },

  /*#
   * Returns list of Groups in Perun, where the User is a direct Administrator
   * or he is a VALID member of any group which is Administrator of some of these Groups.
   *
   * @param user int User <code>id</code>
   * @return List<Group> Found Groups
   */
  /*#
   * Returns list of Groups in selected Vo, where the User is a direct Administrator
   * or he is a VALID member of any group which is Administrator of some of these Groups.
   *
   * @param user int User <code>id</code>
   * @param vo int Vo <code>id</code>
   * @return List<Group> Found Groups
   */
  getGroupsWhereUserIsAdmin {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("vo")) {
        return ac.getUsersManager().getGroupsWhereUserIsAdmin(ac.getSession(), ac.getVoById(parms.readInt("vo")),
            ac.getUserById(parms.readInt("user")));
      } else {
        return ac.getUsersManager().getGroupsWhereUserIsAdmin(ac.getSession(), ac.getUserById(parms.readInt("user")));
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
      if (parms.contains("facility")) {
        Facility facility = ac.getFacilityById(parms.readInt("facility"));
        return ac.getUsersManager().getAllowedResources(ac.getSession(), facility, user);
      } else {
        return ac.getUsersManager().getAllowedResources(ac.getSession(), user);
      }
    }
  },

  /*#
   * Get all assignments of the user. This meaning all the facilities and resources they are assigned to (filtered by
   *  the privileges of the caller)
   *
   * @param user int User <code>id</code>
   * @return Map<Facility, List<Resource>> The assignments
   */
  getUserAssignments {
    @Override
    public Map<Facility, List<Resource>> call(ApiCaller ac, Deserializer parms) throws PerunException {
      User user = ac.getUserById(parms.readInt("user"));
      return ac.getUsersManager().getUserAssignments(ac.getSession(), user);
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
   * Return all resources of specified facility with which user is associated through all his members.
   * Does not require ACTIVE group-resource assignment.
   *
   * @param facility int Facility <code>id</code>
   * @param user int User <code>id</code>
   * @return List<RichResource> All resources with which user is associated
   */

  getAssociatedResources {
    @Override
    public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      Facility facility = ac.getFacilityById(parms.readInt("facility"));
      User user = ac.getUserById(parms.readInt("user"));
      return ac.getUsersManager().getAssociatedResources(ac.getSession(), facility, user);
    }
  },


  /*#
   * Checks if the login is available in the namespace. Return 1 if yes, 0 if no.
   *
   * @param loginNamespace String Namespace
   * @param login String Login
   * @exampleResponse 1
   * @return int 1: login available, 0: login not available
   * @throw InvalidLoginException When login to check has invalid syntax or is not allowed
   */
  isLoginAvailable {
    @Override
    public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (ac.getUsersManager()
          .isLoginAvailable(ac.getSession(), parms.readString("loginNamespace"), parms.readString("login"))) {
        return 1;
      } else {
        return 0;
      }

    }
  },

  /*#
   * Block logins for given namespace
   *
   * @param logins List<String> list of logins
   * @param namespace String Namespace
   * @throw LoginIsAlreadyBlockedException When some login is already blocked for given namespace (or globally)
   * @throw LoginExistsException When some login is already in use
   */
  /*#
   * Block logins globally (for all namespaces)
   *
   * @param logins List<String> list of logins
   * @throw LoginIsAlreadyBlockedException When some login is already blocked for given namespace (or globally)
   * @throw LoginExistsException When some login is already in use
   */
  blockLogins {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getUsersManager().blockLogins(ac.getSession(), parms.readList("logins", String.class),
          parms.contains("namespace") ? parms.readString("namespace") : null);

      return null;
    }
  },

  /*#
   * Unblock logins for given namespace
   *
   * @param logins List<String> list of logins
   * @param namespace String Namespace
   * @throw LoginIsNotBlockedException When some login is not blocked
   */
  /*#
   * Unblock logins globally (for all namespaces)
   *
   * @param logins List<String> list of logins
   * @throw LoginIsNotBlockedException When some login is not blocked
   */
  unblockLogins {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getUsersManager().unblockLogins(ac.getSession(), parms.readList("logins", String.class),
          parms.contains("namespace") ? parms.readString("namespace") : null);

      return null;
    }
  },

  /*#
   * Unblock logins by id
   *
   * @param logins List<Integer> logins <code>id</code>
   * @throw LoginIsNotBlockedException When some login is not blocked
   */
  unblockLoginsById {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getUsersManager().unblockLoginsById(ac.getSession(), parms.readList("logins", Integer.class));

      return null;
    }
  },

  /*#
   * Get a page of blocked logins
   *
   * @param query BlockedLoginsPageQuery
   * @return page of blocked logins based on the query
   */
  getBlockedLoginsPage {
    @Override
    public Object call(ApiCaller ac, Deserializer params) throws PerunException {
      return ac.getUsersManager()
          .getBlockedLoginsPage(ac.getSession(), params.read("query", BlockedLoginsPageQuery.class));
    }
  },

  /*#
   * Returns all blocked logins in namespaces (if namespace is null, then this login is blocked globally)
   *
   * @return List<BlockedLogin> list of all blocked logins in namespaces
   */
  getAllBlockedLoginsInNamespaces {
    @Override
    public List<BlockedLogin> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getUsersManager().getAllBlockedLoginsInNamespaces(ac.getSession());
    }
  },


  /*#
   * Returns users by their IDs.
   *
   * @param ids List<Integer> list of users IDs
   * @return List<User> users with specified IDs
   */
  getUsersByIds {
    @Override
    public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getUsersManager().getUsersByIds(ac.getSession(), parms.readList("ids", Integer.class));
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
   * Changes user password in defined login-namespace.
   *
   * @param login String Users login
   * @param namespace String Namespace
   * @param newPassword String New password
   * @param oldPassword String Old password which will be checked. This parameter is required only if
   * checkOldPassword is set to true.
   * @param checkOldPassword boolean True if the oldPassword have to be checked. When omitted it defaults to false.
   * @throw InvalidLoginException When login of user has invalid syntax (is not allowed)
   * @throw PasswordStrengthException When password doesn't match expected strength by namespace configuration
   */
  /*#
   * Changes user password in defined login-namespace.
   *
   * @param user int User <code>id</code>
   * @param namespace String Namespace
   * @param newPassword String New password
   * @param oldPassword String Old password which will be checked. This parameter is required only if
   * checkOldPassword is set to true.
   * @param checkOldPassword boolean True if the oldPassword have to be checked. When omitted it defaults to false.
   * @throw LoginNotExistsException When user doesn't have login in specified namespace
   * @throw InvalidLoginException When login of user has invalid syntax (is not allowed)
   * @throw PasswordStrengthException When password doesn't match expected strength by namespace configuration
   */
  changePassword {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      boolean checkOldPassword = parms.contains("checkOldPassword") ? parms.readBoolean("checkOldPassword") : false;
      String oldPassword = parms.contains("oldPassword") ? parms.readString("oldPassword") : "";

      if (parms.contains("login")) {
        ac.getUsersManager()
            .changePassword(ac.getSession(), parms.readString("login"), parms.readString("namespace"), oldPassword,
                parms.readString("newPassword"), checkOldPassword);
      } else {
        User user = ac.getUserById(parms.readInt("user"));
        ac.getUsersManager().changePassword(ac.getSession(), user, parms.readString("namespace"), oldPassword,
            parms.readString("newPassword"), checkOldPassword);
      }
      return null;
    }
  }, /*#
   * Checks if the password reset request is valid. The request is valid, if it
   * was created, never used and hasn't expired yet.
   *
   * @param token UUID token for the password reset request
   * @throw PasswordResetLinkExpiredException when the reset link expired
   * @throw PasswordResetLinkNotValidException when the reset link was already used or has never existed
   */
  checkPasswordResetRequestIsValid {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("token")) {
        ac.getUsersManager().checkPasswordResetRequestIsValid(ac.getSession(), parms.readUUID("token"));
      } else {
        throw new RpcException(RpcException.Type.MISSING_VALUE, "token or (i and m)");
      }

      return null;
    }
  }, /*#
   * Changes user password in defined login-namespace based on token parameter.
   *
   * @param token UUID token for the password reset request
   * @param password String new password
   * @param lang language to get notification in (optional).
   * @throw UserNotExistsException When the user who requested the password reset doesn't exist
   * @throw LoginNotExistsException When user doesn't have login in specified namespace
   * @throw InvalidLoginException When login of user has invalid syntax (is not allowed)
   * @throw PasswordStrengthException When password doesn't match expected strength by namespace configuration
   * @throw PasswordResetLinkExpiredException When the password reset request expired
   * @throw PasswordResetLinkNotValidException When the password reset request was already used or has never existed
   * @throw PasswordChangeFailedException When password change failed
   * @throw PasswordOperationTimeoutException When password change timed out
   */
  changeNonAuthzPassword {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("token")) {
        ac.getUsersManager()
            .changeNonAuthzPassword(ac.getSession(), parms.readUUID("token"), parms.readString("password"),
                (parms.contains("lang") ? parms.readString("lang") : null));
      } else {
        throw new RpcException(RpcException.Type.MISSING_VALUE, "token");
      }

      return null;
    }
  }, /*#
   * Reserves a random password in external authz system. User shouldn't be able to log-in (account disabled,
   password unknown to him).
   * This is usefull when manager create account for others and later send them password reset request.
   *
   * @param user int User <code>id</code>
   * @param namespace String Namespace
   * @throw LoginNotExistsException When user doesn't have login in specified namespace
   * @throw InvalidLoginException When login of user has invalid syntax (is not allowed)
   */
  reserveRandomPassword {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getUsersManager()
          .reserveRandomPassword(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("namespace"));

      return null;

    }
  }, /*#
   * Reserves password for a user in specified login-namespace.
   *
   * @param user int User <code>id</code>
   * @param namespace String Namespace
   * @param password String password
   * @throw LoginNotExistsException When user doesn't have login in specified namespace
   * @throw InvalidLoginException When login of user has invalid syntax (is not allowed)
   * @throw PasswordStrengthException When password doesn't match expected strength by namespace configuration
   */
  /*#
   * Reserves password for a user in specified login-namespace.
   *
   * @param login String Login
   * @param namespace String Namespace
   * @param password String password
   * @throw InvalidLoginException When login has invalid syntax (is not allowed)
   * @throw PasswordStrengthException When password doesn't match expected strength by namespace configuration
   */
  reservePassword {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("user")) {
        ac.getUsersManager()
            .reservePassword(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("namespace"),
                parms.readString("password"));
      } else {
        ac.getUsersManager().reservePassword(ac.getSession(), parms.readString("login"), parms.readString("namespace"),
            parms.readString("password"));
      }

      return null;

    }
  }, /*#
   * Validates password for a user in specified login-namespace. After that, user should be able to log-in
   * in external authz system using his credentials. It also creates UserExtSources and sets some required attributes.
   *
   * @param user int User <code>id</code>
   * @param namespace String Namespace
   * @throw LoginNotExistsException When user doesn't have login in specified namespace
   * @throw InvalidLoginException When login of user has invalid syntax (is not allowed)
   */
  /*#
   * Validates password for a user in specified login-namespace. After that, user should be able to log-in
   * in external authz system using his credentials. It also creates UserExtSources and sets some required attributes.
   *
   * @param login String Login
   * @param namespace String Namespace
   * @throw InvalidLoginException When login of user has invalid syntax (is not allowed)
   */
  validatePassword {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("user")) {
        ac.getUsersManager()
            .validatePassword(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("namespace"));
      } else {
        ac.getUsersManager()
            .validatePassword(ac.getSession(), parms.readString("login"), parms.readString("namespace"));
      }

      return null;

    }
  }, /*#
   * Delete password for a user in specified login-namespace.
   *
   * @param user int User <code>id</code>
   * @param namespace String Namespace
   * @throw LoginNotExistsException When user doesn't have login in specified namespace
   * @throw InvalidLoginException When login of user has invalid syntax (is not allowed)
   * @throw PasswordDeletionFailedException When deleting password failed
   */
  /*#
   * Delete password for a user in specified login-namespace.
   *
   * @param login String Login
   * @param namespace String Namespace
   * @throw InvalidLoginException When login of user has invalid syntax (is not allowed)
   * @throw PasswordDeletionFailedException When deleting password failed
   */
  deletePassword {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("user")) {
        ac.getUsersManager()
            .deletePassword(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("namespace"));
      } else {
        ac.getUsersManager().deletePassword(ac.getSession(), parms.readString("login"), parms.readString("namespace"));
      }

      return null;

    }
  }, /*#
   * Check, if login exists in given login-namespace. Not implemented for all namespaces.
   *
   * @param user int User <code>id</code>
   * @param namespace String Namespace
   */
  loginExist {
    @Override
    public Boolean call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getUsersManager()
          .loginExist(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("namespace"));
    }
  }, /*#
   * Validates password for a user in specified login-namespace. After that, user should be able to log-in
   * in external authz system using his credentials. It also creates UserExtSource and sets some required attributes.
   *
   * @deprecated use validatePassword
   * @param user int User <code>id</code>
   * @param login String Login
   * @param namespace String Namespace
   * @throw LoginNotExistsException When user doesn't have login in specified namespace
   * @throw InvalidLoginException When login of user has invalid syntax (is not allowed)
   */
  validatePasswordAndSetExtSources {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getUsersManager()
          .validatePassword(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("namespace"));

      return null;

    }
  }, /*#
   * Set new login in namespace if login is available and user doesn't have login in that namespace.
   * !! Works only for service/guest users => specific users !!
   *
   * @param user int User <code>id</code>
   * @param login String Login
   * @param namespace String Namespace
   * @throw InvalidLoginException When login of user has invalid syntax (is not allowed)
   * @throw LoginExistsException When login is already taken by another user
   */
  setLogin {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getUsersManager()
          .setLogin(ac.getSession(), ac.getUserById(parms.readInt("user")), parms.readString("namespace"),
              parms.readString("login"));

      return null;

    }
  }, /*#
   * Request to change preferred email address of user.
   * Validation mail is sent on new address.
   *
   * Combination of customUrl and linkPath is NOT supported.
   * Referer+linkPath option will be removed in the future.
   *
   * Change is not saved until user validate new email address
   * by calling validatePreferredEmailChange() method with
   * proper set of parameters (sent in validation mail).
   *
   * @param user int User <code>id</code>
   * @param email String new email address to set
   * @param lang String language to get confirmation mail in (optional)
   * @param linkPath path that is appended to the referer and creates the verification link (optional)
   * @param customUrl url to verification link containing path (optional)
   * @param idpFilter authentication method appended to query parameters of verification link (optional)
   */
  requestPreferredEmailChange {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      String referer = parms.getServletRequest().getHeader("Referer");
      String customUrl = parms.contains("customUrl") ? parms.readString("customUrl") : null;
      String customPath = parms.contains("linkPath") ? parms.readString("linkPath") : null;

      if ((referer == null || referer.isEmpty()) && customUrl == null) {
        throw new RpcException(RpcException.Type.MISSING_VALUE,
            "Missing \"Referer\" header in HTTP request and no custom verification link specified.");
      }

      if (customUrl != null) { // customUrl option
        URL url = null;
        try {
          url = new URL(customUrl);
        } catch (MalformedURLException e) {
          throw new RpcException(RpcException.Type.INVALID_URL, "Invalid custom verification link.");
        }
        referer = customUrl;
        customPath = url.getPath();
      } else if (customPath != null) { // referer + linkPath option
        // check that path won't change domain of the url (security risk)
        try {
          URL refUrl = new URL(referer);
          URL refDomainWithPath = new URL(refUrl.getProtocol() + "://" + refUrl.getHost() + customPath);
          if (!refUrl.getHost().equals(refDomainWithPath.getHost())) {
            throw new RpcException(RpcException.Type.INVALID_URL,
                "Invalid verification link - path changes domain: " + refDomainWithPath);
          }
        } catch (MalformedURLException e) {
          throw new RpcException(RpcException.Type.INVALID_URL, "Invalid referer or path.");
        }
      }

      ac.getUsersManager().requestPreferredEmailChange(ac.getSession(), referer, ac.getUserById(parms.readInt("user")),
          parms.readString("email"), parms.contains("lang") ? parms.readString("lang") : null, customPath,
          parms.contains("idpFilter") ? parms.readString("idpFilter") : null);

      return null;

    }
  },

  /*#
   * Validate new preferred email address.
   *
   * Request to validate is determined based
   * on token parameter sent in email notice
   * by requestPreferredEmailChange() method.
   *
   * @param token UUID token for the email change request to validate
   * @param u int <code>id</code> of user you want to validate preferred email request
   *
   * @return String new validated email address
   */
  validatePreferredEmailChange {
    @Override
    public String call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("token")) {
        return ac.getUsersManager().validatePreferredEmailChange(ac.getSession(), ac.getUserById(parms.readInt("u")),
            parms.readUUID("token"));
      } else {
        throw new RpcException(RpcException.Type.MISSING_VALUE, "token");
      }
    }
  },

  /*#
   * Validate ssh public key, throws exception if validation fails
   *
   *
   * @param sshKey String public ssh key to validate
   *
   * @throw SSHKeyNotValidException when validation fails
   */
  validateSSHKey {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      ac.getUsersManager().validateSSHKey(ac.getSession(), parms.readString("sshKey"));
      return null;
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

      return ac.getUsersManager()
          .getPendingPreferredEmailChanges(ac.getSession(), ac.getUserById(parms.readInt("user")));

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

      ac.getUsersManager().createAlternativePassword(ac.getSession(), ac.getUserById(parms.readInt("user")),
          parms.readString("description"), parms.readString("loginNamespace"), parms.readString("password"));

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

      ac.getUsersManager().deleteAlternativePassword(ac.getSession(), ac.getUserById(parms.readInt("user")),
          parms.readString("loginNamespace"), parms.readString("passwordId"));

      return null;
    }
  },

  /*#
   * Updates user's userExtSource last access time in DB. We can get information which userExtSource has been used as
   *  a last one.
   *
   * @param userExtSource int UserExtSource <code>id</code>
   */
  updateUserExtSourceLastAccess {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      ac.getUsersManager()
          .updateUserExtSourceLastAccess(ac.getSession(), ac.getUserExtSourceById(parms.readInt("userExtSource")));

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
   * @throw PasswordStrengthException When password doesn't match expected strength by namespace configuration
   */
  /*#
   * Generate user account in a backend system associated with login-namespace in Perun.
   * Login-namespace might require more parameters, call this method with map of all parameters in a map then.
   *
   * Returns map with
   * 1: key=login-namespace attribute urn, value=generated login
   * 2: rest of opt response attributes...
   *
   * @param namespace String
   * @param name String
   *
   * @return Map<String, String> Map of data from backed response
   * @throw PasswordStrengthException When password doesn't match expected strength by namespace configuration
   */
  generateAccount {
    @Override
    public Map<String, String> call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      if (parms.contains("name")) {
        String name = parms.readString("name");
        HashMap<String, String> accParams = new HashMap<>();
        accParams.put("urn:perun:user:attribute-def:core:lastName", name);
        return ac.getUsersManager().generateAccount(ac.getSession(), parms.readString("namespace"), accParams);
      } else {
        return ac.getUsersManager()
            .generateAccount(ac.getSession(), parms.readString("namespace"), parms.read("parameters", HashMap.class));
      }
    }
  },

  /*#
   * Generates new random password for given user and returns PDF file with information
   * about the new password.
   * <p>
   * The HTML template is taken from entityless attribute randomPwdResetTemplate and the
   * loginNamespace is used as a key.
   * <p>
   * Warning: No matter which serializer you specify, this method always
   * returns .pdf file as an attachment.
   *
   * @param userId Integer ID of User to change password to random
   * @param loginNamespace String namespace that will be used
   * @return PDF PDF document with password
   */
  changePasswordRandom {
    @Override
    public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      return ac.getUsersManager().changePasswordRandom(ac.getSession(), ac.getUserById(parms.readInt("userId")),
          parms.readString("loginNamespace"));
    }
  },

  /*#
   * Check password strength for the given namespace. If the password is too weak,
   * the PasswordStrengthException is thrown
   *
   * @param password String password, that will be checked
   * @param namespace String namespace, that will be used to check the strength of the password
   *
   * @throw PasswordStrengthException When password doesn't match expected strength by namespace configuration
   */
  /*#
   * Check password strength for the given namespace. If the password is too weak,
   * the PasswordStrengthException is thrown
   *
   * @param password String password, that will be checked
   * @param namespace String namespace, that will be used to check the strength of the password
   * @param login String login, which may be required for correct password strength check
   *
   * @throw PasswordStrengthException When password doesn't match expected strength by namespace configuration
   */
  checkPasswordStrength {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getUsersManager()
          .checkPasswordStrength(ac.getSession(), parms.readString("password"), parms.readString("namespace"),
              parms.contains("login") ? parms.readString("login") : null);
      return null;
    }
  },

  /*#
   * Get list of groups of user on specified resource where use is active,
   * that means User is a VALID in the VO and the Group and groups are assigned to the resource.
   *
   * @param resource Integer ID of Resource
   * @param user Integer ID of User
   *
   * @return List<Group> Groups where User is active
   */
  /*#
   * Get list of groups of user on specified resource where use is active,
   * that means User is a VALID in the VO and the Group and groups are assigned to the facility.
   *
   * @param facility Integer ID of Facility
   * @param user Integer ID of User
   *
   * @return List<Group> Groups where User is active
   */
  getGroupsWhereUserIsActive {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

      if (parms.contains("resource")) {
        return ac.getUsersManager()
            .getGroupsWhereUserIsActive(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
                ac.getUserById(parms.readInt("user")));
      } else {
        return ac.getUsersManager()
            .getGroupsWhereUserIsActive(ac.getSession(), ac.getFacilityById(parms.readInt("facility")),
                ac.getUserById(parms.readInt("user")));
      }


    }
  },

  /*#
   * Get list of rich groups of user on specified resource with group attributes specified by the list of their names.
   * Groups where user is active are returned, that means groups, where User is a VALID in the VO and the Group and
   * groups are assigned to the resource.
   *
   * @param resource Integer ID of Resource
   * @param user Integer ID of User
   * @param attrNames List<String> Attribute names (list of their URNs)
   *
   * @return List<RichGroup> Groups where User is active
   */
  /*#
   * Get list of rich groups of user on specified resource with group attributes specified by the list of their names.
   * Groups where user is active are returned, that means groups, where User is a VALID in the VO and the Group and
   * groups are assigned to the facility.
   *
   * @param facility Integer ID of Facility
   * @param user Integer ID of User
   * @param attrNames List<String> Attribute names (list of their URNs)
   *
   * @return List<RichGroup> Groups where User is active
   */
  getRichGroupsWhereUserIsActive {
    @Override
    public List<RichGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {

      if (parms.contains("resource")) {
        return ac.getUsersManager()
            .getRichGroupsWhereUserIsActive(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
                ac.getUserById(parms.readInt("user")), parms.readList("attrNames", String.class));
      } else {
        return ac.getUsersManager()
            .getRichGroupsWhereUserIsActive(ac.getSession(), ac.getFacilityById(parms.readInt("facility")),
                ac.getUserById(parms.readInt("user")), parms.readList("attrNames", String.class));
      }

    }
  },

  /*#
   * From given candidate, creates a service user and assign given owners to him.
   * This method also checks if some of given userExtSources do exist. If so,
   * this method throws a UserExtSourceExistsException.
   * This method can also set only user-def and user-opt attributes for the given candidate.
   *
   * @param candidate Candidate candidate
   * @param specificUserOwners List<User> owners to be set for the new user
   * @return User created service user
   * @throw UserNotExistsException if some of the given owners does not exist
   * @throw AttributeNotExistsException if some of the given attributes dont exist
   * @throw WrongAttributeAssignmentException if some of the given attributes have unsupported namespace
   * @throw UserExtSourceExistsException if some of the given UES already exist
   * @throw WrongReferenceAttributeValueException if some of the given attribute value cannot be set because of
   *                                               some other attribute constraint
   * @throw WrongAttributeValueException if some of the given attribute value is invalid
   * @throw PrivilegeException insufficient permissions
   */
  createServiceUser {
    @Override
    public User call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      return ac.getUsersManager().createServiceUser(ac.getSession(), parms.read("candidate", Candidate.class),
          parms.readList("specificUserOwners", User.class));
    }
  },

  /*#
   * Change organization from which user came to organization from user ext source.
   *
   * @param user user
   * @param newOrganizationName new organization name
   * @throw UserNotExistsException                If user does not exist.
   * @throw PrivilegeException                    if privileges are not given.
   * @throw PersonalDataChangeNotEnabledException If change of organization to organization from ues is not enabled.
   * @throw UserExtSourceNotExistsException       If user ext source with given organization name and required
   *                                               loa does not exist.
   */
  changeOrganization {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      ac.getUsersManager()
          .changeOrganization(ac.getSession(), ac.getUserById(parms.readInt("user")),
              parms.readString("newOrganizationName"));
      return null;
    }
  },

  /*#
   * Change organization from which user came to custom organization. If check from admin is required, then
   * UserOrganizationChangeRequested audit log will be created. Otherwise, it will be set immediately.
   *
   * @param user user
   * @param newOrganizationName new organization name
   * @throw UserNotExistsException                If user does not exist.
   * @throw PrivilegeException                    if privileges are not given.
   * @throw PersonalDataChangeNotEnabledException If change of organization to custom organization is not enabled.
   */
  changeOrganizationCustom {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      ac.getUsersManager()
          .changeOrganizationCustom(ac.getSession(), ac.getUserById(parms.readInt("user")),
              parms.readString("newOrganizationName"));
      return null;
    }
  },

  /*#
   * Change user's name to user's name from user ext source.
   *
   * @param user user
   * @param newUserName new user's name
   * @throw UserNotExistsException                If user does not exist.
   * @throw PrivilegeException                    if privileges are not given.
   * @throw PersonalDataChangeNotEnabledException If change of user's name to name from ues is not enabled.
   * @throw UserExtSourceNotExistsException       If user ext source with given user's name and required
   *                                              loa does not exist.
   */
  changeName {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      ac.getUsersManager()
          .changeName(ac.getSession(), ac.getUserById(parms.readInt("user")),
              parms.readString("newUserName"));
      return null;
    }
  },

  /*#
   * Change user's to name custom name. If check from admin is required, then UserNameChangeRequest audit log will be
   * created.cOtherwise, it will be set immediately.
   *
   * @param user user
   * @param titleBefore new user's title before
   * @param firstName new user's first name
   * @param middleName new user's middle name
   * @param lastName new user's last name
   * @param titleAfter new user's title after
   * @throw UserNotExistsException                If user does not exist.
   * @throw PrivilegeException                    if privileges are not given.
   * @throw PersonalDataChangeNotEnabledException If change of user's name to custom name is not enabled.
   */
  changeNameCustom {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      ac.getUsersManager()
          .changeNameCustom(ac.getSession(),
              ac.getUserById(parms.readInt("user")),
              parms.readString("titleBefore"),
              parms.readString("firstName"),
              parms.readString("middleName"),
              parms.readString("lastName"),
              parms.readString("titleAfter"));
      return null;
    }
  },

  /*#
   * Change user's email to email from user ext source.
   *
   * @param user user
   * @param newEmail new email
   * @throw UserNotExistsException                If user does not exist.
   * @throw PrivilegeException                    if privileges are not given.
   * @throw PersonalDataChangeNotEnabledException If change of user's email to email from ues is not enabled.
   * @throw UserExtSourceNotExistsException       If user ext source with given email and required
   *                                               loa does not exist.
   */
  changeEmail {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      ac.getUsersManager()
          .changeEmail(ac.getSession(), ac.getUserById(parms.readInt("user")),
              parms.readString("newEmail"));
      return null;
    }
  },

  /*#
   * Change user's email to custom email. If verification is required, then verification email will be sent.
   * Otherwise, it will be set immediately.
   *
   * @param user user
   * @param newEmail new email
   * @param lang String language to get confirmation mail in (optional)
   * @param linkPath path that is appended to the referer and creates the verification link (optional)
   * @param customUrl url to verification link containing path (optional)
   * @param idpFilter authentication method appended to query parameters of verification link (optional)
   * @throw UserNotExistsException                If user does not exist.
   * @throw PrivilegeException                    if privileges are not given.
   * @throw PersonalDataChangeNotEnabledException If change of user's email to custom email is not enabled.
   */
  changeEmailCustom {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      String referer = parms.getServletRequest().getHeader("Referer");
      String customUrl = parms.contains("customUrl") ? parms.readString("customUrl") : null;
      String customPath = parms.contains("linkPath") ? parms.readString("linkPath") : null;

      if ((referer == null || referer.isEmpty()) && customUrl == null) {
        throw new RpcException(RpcException.Type.MISSING_VALUE,
            "Missing \"Referer\" header in HTTP request and no custom verification link specified.");
      }

      if (customUrl != null) { // customUrl option
        URL url = null;
        try {
          url = new URL(customUrl);
        } catch (MalformedURLException e) {
          throw new RpcException(RpcException.Type.INVALID_URL, "Invalid custom verification link.");
        }
        referer = customUrl;
        customPath = url.getPath();
      } else if (customPath != null) { // referer + linkPath option
        // check that path won't change domain of the url (security risk)
        try {
          URL refUrl = new URL(referer);
          URL refDomainWithPath = new URL(refUrl.getProtocol() + "://" + refUrl.getHost() + customPath);
          if (!refUrl.getHost().equals(refDomainWithPath.getHost())) {
            throw new RpcException(RpcException.Type.INVALID_URL,
                "Invalid verification link - path changes domain: " + refDomainWithPath);
          }
        } catch (MalformedURLException e) {
          throw new RpcException(RpcException.Type.INVALID_URL, "Invalid referer or path.");
        }
      }

      ac.getUsersManager()
          .changeEmailCustom(ac.getSession(), ac.getUserById(parms.readInt("user")),
              parms.readString("newEmail"), referer, parms.contains("lang") ? parms.readString("lang") : null,
              customPath,
              parms.contains("idpFilter") ? parms.readString("idpFilter") : null);
      return null;
    }
  },

  /*#
   * Gets map with 2 items which are a list of all vos and a list of all groups where given user is member filtered by
   * principal's privileges.
   *
   * @param user Integer ID of User
   *
   * @return Map with lists of vos and groups where given user is member
   *
   * @throw UserNotExistsException If user does not exist.
   */
  getUserRelations {
    @Override
    public Map<String, List<PerunBean>> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getUsersManager().getUserRelations(ac.getSession(), ac.getUserById(parms.readInt("user")));
    }
  }

}
