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

	private final static int targetVoId = 3346;
	private final static String LS_DOMAIN = "@hostel.aai.lifescience-ri.eu";
	private final static String EXT_SOURCE_NAME = "https://hostel.aai.lifescience-ri.eu/lshostel/";

	public LifescienceidusernamePasswordManagerModule() {
		// set proper namespace
		this.actualLoginNamespace = "lifescienceid-username";
	}

	@Override
	public void validatePassword(PerunSession sess, String userLogin, User user) throws InvalidLoginException {
		// FIXME Needs to be commented because it is triggered through the registrations that are not intended for the hostel users
		/*
		if (user == null) {
			user = ((PerunBl) sess.getPerun()).getModulesUtilsBl().getUserByLoginInNamespace(sess, userLogin, actualLoginNamespace);
		}

		if (user == null) {
			log.warn("No user was found by login '{}' in {} namespace.", userLogin, actualLoginNamespace);
			return;
		}

		try {
			// set userExtSource
			ExtSource extSource = ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().getExtSourceByName(sess, EXT_SOURCE_NAME);
			UserExtSource ues = new UserExtSource(extSource, userLogin + LS_DOMAIN);
			ues.setLoa(0);
			try {
				((PerunBl) sess.getPerun()).getUsersManagerBl().addUserExtSource(sess, user, ues);
			} catch (UserExtSourceExistsException ex) {
				//this is OK
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
			Vo targetVo = ((PerunBl) sess.getPerun()).getVosManagerBl().getVoById(sess, targetVoId);
			((PerunBl) sess.getPerun()).getMembersManagerBl().createMember(sess, targetVo, user);
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException | ExtSourceNotExistsException |
				 WrongAttributeValueException | WrongReferenceAttributeValueException | VoNotExistsException | ExtendMembershipException ex) {
			throw new InternalErrorException(ex);
		} catch (AlreadyMemberException ignored) {
		}
		 */

		// validate password
		super.validatePassword(sess, userLogin, user);
	}
}
