package cz.metacentrum.perun.registrar.impl;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.entry.ExtSourcesManagerEntry;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import net.jodah.expiringmap.ExpirationPolicy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.registrar.ConsolidatorManager;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.exceptions.*;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import cz.metacentrum.perun.registrar.model.Identity;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Manager for Identity consolidation in Registrar.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ConsolidatorManagerImpl implements ConsolidatorManager {

	private final static Logger log = LoggerFactory.getLogger(ConsolidatorManagerImpl.class);

	@Autowired RegistrarManager registrarManager;
	@Autowired PerunBl perun;
	private JdbcPerunTemplate jdbc;
	private PerunSession registrarSession;
	// expiring thread safe map cache
	private ExpiringMap<String, Map<String, Object>> requestCache;

	public void setRegistrarManager(RegistrarManager registrarManager) {
		this.registrarManager = registrarManager;
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbc = new JdbcPerunTemplate(dataSource);
		this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	protected void initialize() throws PerunException {

		// gets session for a system principal "perunRegistrar"
		final PerunPrincipal pp = new PerunPrincipal("perunRegistrar",
				ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
				ExtSourcesManager.EXTSOURCE_INTERNAL);
		registrarSession = perun.getPerunSession(pp, new PerunClient());

		// cache expires after 5 minutes from creation
		requestCache = ExpiringMap.builder().expiration(5, TimeUnit.MINUTES).expirationPolicy(ExpirationPolicy.CREATED).build();

	}

	@Override
	public List<Identity> checkForSimilarUsers(PerunSession sess) throws PerunException {

		// if user known, doesn't actually search and offer joining.
		if (sess.getPerunPrincipal().getUser() != null) {
			return new ArrayList<>();
		}

		// if user known, doesn't actually search and offer joining.
		try {
			perun.getUsersManager().getUserByExtSourceNameAndExtLogin(registrarSession, sess.getPerunPrincipal().getExtSourceName(), sess.getPerunPrincipal().getActor());
			return new ArrayList<>();
		} catch (Exception ex) {
			// we don't care, that search failed. That is actually OK case.
		}

		String name;
		String mail;

		Set<RichUser> res = new HashSet<>();

		List<String> attrNames = new ArrayList<>();
		attrNames.add("urn:perun:user:attribute-def:def:preferredMail");
		attrNames.add("urn:perun:user:attribute-def:def:organization");

		mail = sess.getPerunPrincipal().getAdditionalInformations().get("mail");

		if (mail != null) {
			if (mail.contains(";")) {
				String[] mailSearch = mail.split(";");
				for (String m : mailSearch) {
					if (m != null && !m.isEmpty())
						res.addAll(perun.getUsersManager().findRichUsersWithAttributesByExactMatch(registrarSession, m, attrNames));
				}
			} else {
				res.addAll(perun.getUsersManager().findRichUsersWithAttributesByExactMatch(registrarSession, mail, attrNames));
			}
		}

		// check by mail is more precise, so check by name only if nothing is found.
		if (res.isEmpty()) {

			name = sess.getPerunPrincipal().getAdditionalInformations().get("cn");

			if (name != null && !name.isEmpty()) res.addAll(perun.getUsersManager().findRichUsersWithAttributesByExactMatch(registrarSession, name, attrNames));

			name = sess.getPerunPrincipal().getAdditionalInformations().get("displayName");

			if (name != null && !name.isEmpty()) res.addAll(perun.getUsersManager().findRichUsersWithAttributesByExactMatch(registrarSession, name, attrNames));

		}

		return convertToIdentities(new ArrayList<>(res));

	}

	@Override
	public List<Identity> checkForSimilarUsers(PerunSession sess, Vo vo, Group group, Application.AppType type) throws PerunException {

		Application app = getLatestApplication(sess, vo, group, type);
		if (app != null) {
			return checkForSimilarUsers(sess, app.getId());
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public List<Identity> checkForSimilarUsers(PerunSession sess, List<ApplicationFormItemData> formItems) throws PerunException {

		if (sess.getPerunPrincipal().getUser() != null || formItems == null) {
			return new ArrayList<>();
		}

		Set<RichUser> res = new HashSet<>();
		List<String> attrNames = new ArrayList<>();
		attrNames.add("urn:perun:user:attribute-def:def:preferredMail");
		attrNames.add("urn:perun:user:attribute-def:def:organization");

		for (ApplicationFormItemData item : formItems) {

			String value = item.getValue();

			if (item.getFormItem().getType().equals(ApplicationFormItem.Type.VALIDATED_EMAIL)) {
				// search by email
				if (value != null && !value.isEmpty()) res.addAll(perun.getUsersManager().findRichUsersWithAttributesByExactMatch(registrarSession, value, attrNames));
			}
			if (Objects.equals(item.getFormItem().getPerunDestinationAttribute(), "urn:perun:user:attribute-def:core:displayName")) {
				// search by name
				if (value != null && !value.isEmpty()) res.addAll(perun.getUsersManager().findRichUsersWithAttributesByExactMatch(registrarSession, value, attrNames));
			}

		}

		return convertToIdentities(new ArrayList<>(res));

	}

	@Override
	public List<Identity> checkForSimilarUsers(PerunSession sess, int appId) throws PerunException {

		String email = "";
		String name = "";
		List<RichUser> result = new ArrayList<>();

		List<String> attrNames = new ArrayList<>();
		attrNames.add("urn:perun:user:attribute-def:def:preferredMail");
		attrNames.add("urn:perun:user:attribute-def:def:organization");

		Application app = registrarManager.getApplicationById(registrarSession, appId);

		if (app.getGroup() == null) {
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, app.getVo()) &&
					!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
				if (sess.getPerunPrincipal().getUser() != null) {
					// check if application to find similar users by belongs to user
					if (!sess.getPerunPrincipal().getUser().equals(app.getUser())) throw new PrivilegeException("checkForSimilarUsers");
				} else {
					if (!sess.getPerunPrincipal().getExtSourceName().equals(app.getExtSourceName()) &&
							!sess.getPerunPrincipal().getActor().equals(app.getCreatedBy())) throw new PrivilegeException("checkForSimilarUsers");
				}
			}
		} else {
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, app.getVo()) &&
					!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, app.getGroup()) &&
					!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
				if (sess.getPerunPrincipal().getUser() != null) {
					// check if application to find similar users by belongs to user
					if (!sess.getPerunPrincipal().getUser().equals(app.getUser())) throw new PrivilegeException("checkForSimilarUsers");
				} else {
					if (!sess.getPerunPrincipal().getExtSourceName().equals(app.getExtSourceName()) &&
							!sess.getPerunPrincipal().getActor().equals(app.getCreatedBy())) throw new PrivilegeException("checkForSimilarUsers");
				}
			}
		}

		// only for initial VO applications if user==null
		if (app.getType().equals(Application.AppType.INITIAL) && app.getGroup() == null && app.getUser() == null) {

			try {
				User u = perun.getUsersManager().getUserByExtSourceNameAndExtLogin(registrarSession, app.getExtSourceName(), app.getCreatedBy());
				if (u != null) {
					// user connected his identity after app creation and before it's approval.
					// do not show error message in GUI by returning an empty array.
					return convertToIdentities(result);
				}
			} catch (Exception ex){
				// we don't care, let's try to search by name
			}

			List<ApplicationFormItemData> data = registrarManager.getApplicationDataById(sess, appId);

			// search by email, which should be unique (check is more precise)
			for (ApplicationFormItemData item : data) {
				if ("urn:perun:user:attribute-def:def:preferredMail".equals(item.getFormItem().getPerunDestinationAttribute())) {
					email = item.getValue();
				}
				if (email != null && !email.isEmpty()) break;
			}

			List<RichUser> users = (email != null && !email.isEmpty()) ? perun.getUsersManager().findRichUsersWithAttributesByExactMatch(registrarSession, email, attrNames) : new ArrayList<>();

			if (users != null && !users.isEmpty()) {
				// found by preferredMail
				return convertToIdentities(users);
			}

			// search by different mail

			email = ""; // clear previous value
			for (ApplicationFormItemData item : data) {
				if ("urn:perun:member:attribute-def:def:mail".equals(item.getFormItem().getPerunDestinationAttribute())) {
					email = item.getValue();
				}
				if (email != null && !email.isEmpty()) break;
			}

			users = (email != null && !email.isEmpty()) ? perun.getUsersManager().findRichUsersWithAttributesByExactMatch(registrarSession, email, attrNames) : new ArrayList<>();
			if (users != null && !users.isEmpty()) {
				// found by member mail
				return convertToIdentities(users);
			}

			// continue to search by display name

			for (ApplicationFormItemData item : data) {
				if (RegistrarManagerImpl.URN_USER_DISPLAY_NAME.equals(item.getFormItem().getPerunDestinationAttribute())) {
					name = item.getValue();
					// use parsed name to drop mistakes on IDP side
					try {
						if (name != null && !name.isEmpty()) {
							Map<String, String> nameMap = Utils.parseCommonName(name);
							// drop name titles to spread search
							String newName = "";
							if (nameMap.get("firstName") != null
									&& !nameMap.get("firstName").isEmpty()) {
								newName += nameMap.get("firstName") + " ";
							}
							if (nameMap.get("lastName") != null
									&& !nameMap.get("lastName").isEmpty()) {
								newName += nameMap.get("lastName");
							}
							// fill parsed name instead of input
							if (StringUtils.isNotBlank(newName)) {
								name = newName;
							}
						}
					} catch (Exception ex) {
						log.error("[REGISTRAR] Unable to parse new user's display/common name when searching for similar users.", ex);
					}
					if (name != null && !name.isEmpty()) break;
				}
			}

			users = (name != null && !name.isEmpty()) ? perun.getUsersManager().findRichUsersWithAttributesByExactMatch(registrarSession, name, attrNames) : new ArrayList<>();
			if (users != null && !users.isEmpty()) {
				// found by member display name
				return convertToIdentities(users);
			}

			// continue to search by last name

			name = ""; // clear previous value
			for (ApplicationFormItemData item : data) {
				if (RegistrarManagerImpl.URN_USER_LAST_NAME.equals(item.getFormItem().getPerunDestinationAttribute())) {
					name = item.getValue();
					if (name != null && !name.isEmpty()) break;
				}
			}

			if (name != null && !name.isEmpty()) {
				// what was found by name
				return convertToIdentities(perun.getUsersManager().findRichUsersWithAttributesByExactMatch(registrarSession, name, attrNames));
			} else {
				// not found by name
				return convertToIdentities(result);
			}

		} else {
			// not found, since not proper type of application to check users for
			return convertToIdentities(result);
		}

	}

	@Override
	public String getConsolidatorToken(PerunSession sess) throws PerunException {

		Map<String, Object> value = new HashMap<>();

		String actor = sess.getPerunPrincipal().getActor();
		String extSourceName = sess.getPerunPrincipal().getExtSourceName();
		String extSourceType = sess.getPerunPrincipal().getExtSourceType();
		Integer extSourceLoa = sess.getPerunPrincipal().getExtSourceLoa();
		User user = sess.getPerunPrincipal().getUser();

		value.put("actor", actor);
		value.put("extSourceName", extSourceName);
		value.put("extSourceType", extSourceType);
		value.put("extSourceLoa", extSourceLoa);
		value.put("user", user);
		value.put("additionalInformation", sess.getPerunPrincipal().getAdditionalInformations());

		// create token from actual properties
		String token = registrarManager.getMailManager().getMessageAuthenticationCode(System.currentTimeMillis() + actor + extSourceName + extSourceType + extSourceLoa);

		requestCache.putIfAbsent(token, value);

		return token;

	}

	@Override
	public List<UserExtSource> consolidateIdentityUsingToken(PerunSession sess, String token) throws PerunException {

		Map<String, Object> originalIdentity = requestCache.get(token);

		if (originalIdentity == null) {
			throw new InvalidTokenException("Your token for joining identities is no longer valid. Please retry from the start.");
		}

		User originalUser = (User)originalIdentity.get("user");
		User currentUser = sess.getPerunPrincipal().getUser();

		if (originalUser == null && currentUser == null) {
			IdentityUnknownException ex = new IdentityUnknownException("Neither original or current identity is know to Perun. Please use at least one identity known to Perun.");
			ex.setLogin((String) originalIdentity.get("actor"));
			ex.setSource2((String) originalIdentity.get("extSourceName"));
			ex.setSourceType2((String) originalIdentity.get("extSourceType"));
			ex.setLogin2(sess.getPerunPrincipal().getActor());
			ex.setSource2(sess.getPerunPrincipal().getExtSourceName());
			ex.setSourceType2(sess.getPerunPrincipal().getExtSourceType());
			throw ex;
		}

		if (originalIdentity.get("extSourceName").equals(sess.getPerunPrincipal().getExtSourceName()) &&
				originalIdentity.get("actor").equals(sess.getPerunPrincipal().getActor()) &&
				originalIdentity.get("extSourceType").equals(sess.getPerunPrincipal().getExtSourceType())) {
			IdentityIsSameException ex = new IdentityIsSameException("You tried to join same identity with itself. Please try again but select different identity.");
			ex.setLogin(sess.getPerunPrincipal().getActor());
			ex.setSource(sess.getPerunPrincipal().getExtSourceName());
			ex.setSourceType(sess.getPerunPrincipal().getExtSourceType());
			throw ex;
		}

		if (originalUser != null && originalUser.equals(currentUser)) {
			throw new IdentitiesAlreadyJoinedException("You already have both identities joined.");
		}

		if (originalUser != null && currentUser != null && !originalUser.equals(currentUser)) {
			throw new IdentityAlreadyInUseException("Your identity is already associated with a different user. If you are really the same person, please contact support to help you.", originalUser, currentUser);
		}

		// merge original identity into current user
		if (originalUser == null) {
			UserExtSource ues = createExtSourceAndUserExtSource(currentUser, (String) originalIdentity.get("actor"),
					(String)originalIdentity.get("extSourceName"), (String)originalIdentity.get("extSourceType"),
					(Integer) originalIdentity.get("extSourceLoa"));
			((PerunBlImpl)perun).setUserExtSourceAttributes(sess, ues, (Map<String,String>)originalIdentity.get("additionalInformation"));
		}

		// merge current identity into original user
		if (currentUser == null) {
			UserExtSource ues = createExtSourceAndUserExtSource(originalUser, sess.getPerunPrincipal().getActor(),
					sess.getPerunPrincipal().getExtSourceName(), sess.getPerunPrincipal().getExtSourceType(),
					sess.getPerunPrincipal().getExtSourceLoa());
			((PerunBlImpl)perun).setUserExtSourceAttributes(sess, ues, sess.getPerunPrincipal().getAdditionalInformations());
		}

		AuthzResolverBlImpl.refreshSession(sess);

		requestCache.remove(token);

		return perun.getUsersManager().getUserExtSources(sess, sess.getPerunPrincipal().getUser());

	}

	/**
	 * Creates ExtSource and UserExtSource if necessary for the purpose of joining users identities.
	 *
	 * @param user User to add UES to
	 * @param actor Actor to add
	 * @param extSourceName ExtSource name to add
	 * @param extSourceType ExtSource type to add
	 * @param loa loa in ext source
	 * @return created UserExtSource
	 * @throws PerunException when anything fails
	 */
	private UserExtSource createExtSourceAndUserExtSource(User user, String actor, String extSourceName, String extSourceType, int loa) throws PerunException {

		ExtSource extSource = new ExtSource(extSourceName, extSourceType);
		try {
			extSource = perun.getExtSourcesManagerBl().getExtSourceByName(registrarSession, extSourceName);
		} catch (ExtSourceNotExistsException ex) {
			extSource = perun.getExtSourcesManager().createExtSource(registrarSession, extSource, null);
		}

		UserExtSource ues = new UserExtSource();
		ues.setLogin(actor);
		ues.setLoa(loa);
		ues.setExtSource(extSource);

		return perun.getUsersManager().addUserExtSource(registrarSession, user, ues);

	}

	/**
	 * Retrieves whole application object from DB
	 * (authz in parent methods)
	 *
	 * @param sess PerunSession for Authz and to resolve User
	 * @param vo VO to get application for
	 * @param group Group
	 *
	 * @return application object / null if not exists
	 */
	private Application getLatestApplication(PerunSession sess, Vo vo, Group group, Application.AppType type) {
		try {

			if (sess.getPerunPrincipal().getUser() != null) {

				if (group != null) {

					return jdbc.queryForObject(RegistrarManagerImpl.APP_SELECT + " where a.id=(select max(id) from application where vo_id=? and group_id=? and apptype=? and user_id=? )", RegistrarManagerImpl.APP_MAPPER, vo.getId(), group.getId(), String.valueOf(type), sess.getPerunPrincipal().getUserId());

				} else {

					return jdbc.queryForObject(RegistrarManagerImpl.APP_SELECT + " where a.id=(select max(id) from application where vo_id=? and apptype=? and user_id=? )", RegistrarManagerImpl.APP_MAPPER, vo.getId(), String.valueOf(type), sess.getPerunPrincipal().getUserId());

				}

			} else {

				if (group != null) {

					return jdbc.queryForObject(RegistrarManagerImpl.APP_SELECT + " where a.id=(select max(id) from application where vo_id=? and group_id=? and apptype=? and created_by=? and extsourcename=? )", RegistrarManagerImpl.APP_MAPPER, vo.getId(), group.getId(), String.valueOf(type), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getExtSourceName());

				} else {

					return jdbc.queryForObject(RegistrarManagerImpl.APP_SELECT + " where a.id=(select max(id) from application where vo_id=? and apptype=? and created_by=? and extsourcename=? )", RegistrarManagerImpl.APP_MAPPER, vo.getId(), String.valueOf(type), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getExtSourceName());

				}

			}

		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	/**
	 * Convert RichUsers to Identity objects with obfuscated email address and limited set of ext sources.
	 * Service users are removed from the list.
	 *
	 * @param list RichUsers to convert
	 * @return list of Identities without service ones
	 * @throws PerunException
	 */
	private List<Identity> convertToIdentities(List<RichUser> list) throws PerunException {

		List<Identity> result = new ArrayList<>();

		if (list != null && !list.isEmpty()) {

			for (RichUser u : list) {

				// skip service users
				if (u.isServiceUser()) continue;

				Identity identity = new Identity();
				identity.setName(u.getDisplayName());
				identity.setId(u.getId());

				for (Attribute a : u.getUserAttributes()) {

					if (MailManagerImpl.URN_USER_PREFERRED_MAIL.equals(a.getName())) {
						if (a.getValue() != null && !((String)a.getValue()).isEmpty()) {

							String safeMail = ((String) a.getValue()).split("@")[0];

							if (safeMail.length() > 2) {
								safeMail = safeMail.substring(0, 1) + "****" + safeMail.substring(safeMail.length()-1);
							}

							safeMail += "@"+((String) a.getValue()).split("@")[1];

							identity.setEmail(safeMail);

						}
					} else if ("urn:perun:user:attribute-def:def:organization".equals(a.getName())) {
						if (a.getValue() != null) {
							identity.setOrganization((String)a.getValue());
						}
					}

				}

				Set<ExtSource> es = new HashSet<>();
				for (UserExtSource ues : u.getUserExtSources()) {

					if (ues.getExtSource().getType().equals(ExtSourcesManagerEntry.EXTSOURCE_X509)) {
						es.add(ues.getExtSource());
					} else if (ues.getExtSource().getType().equals(ExtSourcesManagerEntry.EXTSOURCE_IDP)) {

						if (ues.getExtSource().getName().equals("https://extidp.cesnet.cz/idp/shibboleth")) {
							// FIXME - hack Social IdP to let us know proper identity source
							String type = ues.getLogin().split("@")[1].split("\\.")[0];
							ues.getExtSource().setName("https://extidp.cesnet.cz/idp/shibboleth&authnContextClassRef=urn:cesnet:extidp:authn:"+type);
						}

						/* WE NO LONGER WANT TO SPECIFY source IdPs for ELIXIR, just reference the proxy itself
						else if (ues.getExtSource().getName().equals("https://login.elixir-czech.org/idp/")) {
							// FIXME - hack Elixir proxy IdP to let us know proper identity source
							String type = ues.getLogin().split("@")[1];
							ues.getExtSource().setName("https://login.elixir-czech.org/idp/@"+type);
						}
						*/

						// FOR FOREIGN PROXIES WHICH CREATES NEW IDENTITY ON ANY ATTRIBUTE CHANGE WE MUST USE
						// SOURCING IDENTITY, SO USER CAN RELATE
						try {
							Attribute uesAttr = perun.getAttributesManagerBl().getAttribute(registrarSession, ues, AttributesManager.NS_UES_ATTR_DEF + ":authenticating-authority");
							if (uesAttr.getValue() != null && !((String) uesAttr.getValue()).isEmpty()) {
								// clear ID so equals() used by Set<ExtSource> will merge same values by name
								ues.getExtSource().setId(0);
								ues.getExtSource().setName((String) uesAttr.getValue());
							}
						} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
							// don't care
						}
						es.add(ues.getExtSource());
					} else if (ues.getExtSource().getType().equals(ExtSourcesManagerEntry.EXTSOURCE_KERBEROS)) {
						es.add(ues.getExtSource());
					}
				}
				identity.setIdentities(new ArrayList<>(es));

				result.add(identity);

			}

		}

		return result;

	}

}
