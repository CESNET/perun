package cz.metacentrum.perun.core.impl.modules.pwdmgr;

	import com.google.common.collect.Lists;
	import cz.metacentrum.perun.core.api.Attribute;
	import cz.metacentrum.perun.core.api.ExtSource;
	import cz.metacentrum.perun.core.api.PerunSession;
	import cz.metacentrum.perun.core.api.User;
	import cz.metacentrum.perun.core.api.UserExtSource;
	import cz.metacentrum.perun.core.api.Vo;
	import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
	import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
	import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
	import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
	import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
	import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
	import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
	import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
	import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
	import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
	import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
	import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
	import cz.metacentrum.perun.core.bl.PerunBl;
	import cz.metacentrum.perun.core.bl.UsersManagerBl;
	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

/**
 * Password manager for lifescienceid-username.
 */
public class LifescienceidusernamePasswordManagerModule extends GenericPasswordManagerModule {

	private final static Logger log = LoggerFactory.getLogger(LifescienceidusernamePasswordManagerModule.class);

	private final static String VO_NAME = "lifescience_hostel";
	private final static String LS_DOMAIN = "@hostel.aai.lifescience-ri.eu";
	private final static String EXT_SOURCE_NAME = "https://hostel.aai.lifescience-ri.eu/lshostel/";
	private final static String REGISTRAR = "perunRegistrar";

	public LifescienceidusernamePasswordManagerModule() {
		// set proper namespace
		this.actualLoginNamespace = "lifescienceid-username";
	}

	@Override
	public void validatePassword(PerunSession sess, String userLogin, User user) throws InvalidLoginException {
		// This block of code is intended for manual setup of local accounts. Not for registrations.
		if (!sess.getPerunPrincipal().getActor().equals(REGISTRAR)){
			if (user == null) {
				user = ((PerunBl) sess.getPerun()).getModulesUtilsBl().getUserByLoginInNamespace(sess, userLogin, actualLoginNamespace);
			}

			if (user == null) {
				log.warn("No user was found by login '{}' in {} namespace.", userLogin, actualLoginNamespace);
				return;
			}

			try {
				ExtSource extSource = ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().getExtSourceByName(sess, EXT_SOURCE_NAME);
				UserExtSource ues;
				try {
					// get userExtSource
					ues = ((PerunBl) sess.getPerun()).getUsersManagerBl().getUserExtSourceByExtLogin(sess, extSource, userLogin + LS_DOMAIN);
				} catch (UserExtSourceNotExistsException ex) {
					// ues do not exist yet so we need to create it
					ues = new UserExtSource(extSource, userLogin + LS_DOMAIN);
					ues.setLoa(0);
					((PerunBl) sess.getPerun()).getUsersManagerBl().addUserExtSource(sess, user, ues);
				}

				// set additional identifiers
				Attribute additionalIdentifiers = ((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttribute(sess, ues, UsersManagerBl.ADDITIONAL_IDENTIFIERS_PERUN_ATTRIBUTE_NAME);
				String newIdentifier = userLogin + LS_DOMAIN;
				if (additionalIdentifiers.valueAsList() == null || additionalIdentifiers.valueAsList().isEmpty()) {
					additionalIdentifiers.setValue(Lists.newArrayList(newIdentifier));
				} else if (!additionalIdentifiers.valueContains(newIdentifier)) {
					additionalIdentifiers.setValue(additionalIdentifiers.valueAsList().add(newIdentifier));
				}
				((PerunBl) sess.getPerun()).getAttributesManagerBl().setAttribute(sess, ues, additionalIdentifiers);

				// add user to specific vo
				Vo targetVo = ((PerunBl) sess.getPerun()).getVosManagerBl().getVoByShortName(sess, VO_NAME);
				((PerunBl) sess.getPerun()).getMembersManagerBl().createMember(sess, targetVo, user);
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException | ExtSourceNotExistsException |
				WrongAttributeValueException | WrongReferenceAttributeValueException | VoNotExistsException | ExtendMembershipException | UserExtSourceExistsException ex) {
				throw new InternalErrorException(ex);
			} catch (AlreadyMemberException ignored) {
			}
		}

		// validate password
		super.validatePassword(sess, userLogin, user);
	}
}
