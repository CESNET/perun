package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberDisabled;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberExpired;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberValidated;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleAbstract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Message with information about suspension of a member.
 *
 * @author Metodej Klang <metodej.klang@gmail.com>
 */
public class urn_perun_member_attribute_def_def_suspensionInfo extends MemberAttributesModuleAbstract {

	private final static String A_M_D_suspensionInfo = AttributesManager.NS_MEMBER_ATTR_DEF + ":suspensionInfo";
	private final static Logger log = LoggerFactory.getLogger(urn_perun_member_attribute_def_def_suspensionInfo.class);

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Member member, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		if (attribute.getValue() == null) {
			return;
		}

		Map<String, String> valueOfAttribute = attribute.valueAsMap();

		if (valueOfAttribute.isEmpty()) {
			return;
		}

		if (!valueOfAttribute.containsKey("userId") || !valueOfAttribute.containsKey("timestamp")) {
			int id = session.getPerunPrincipal().getUser().getId();
			valueOfAttribute.put("userId", String.valueOf(id));
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			valueOfAttribute.put("timestamp", timestamp.toString());
			try {
				session.getPerunBl().getAttributesManagerBl().setAttribute(session, member, attribute);
			} catch (WrongAttributeValueException | WrongAttributeAssignmentException e) {
				throw new InternalErrorException(e);
			}
		}
	}

	@Override
	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl session, AuditEvent message) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

		Member member = null;

		if (message instanceof MemberValidated) {
			member = ((MemberValidated) message).getMember();
		} else if (message instanceof MemberDisabled) {
			member = ((MemberDisabled) message).getMember();
		} else if (message instanceof MemberExpired) {
			member = ((MemberExpired) message).getMember();
		}

		clearSuspensionInfo(session, member);

		return new ArrayList<>();
	}

	private void clearSuspensionInfo(PerunSessionImpl session, Member member) throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		if (member != null) {
			try {
				Attribute attribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, member, A_M_D_suspensionInfo);
				attribute.setValue(new LinkedHashMap<String, String>());
				session.getPerunBl().getAttributesManagerBl().setAttribute(session, member, attribute);
			} catch (AttributeNotExistsException e) {
				//suspensionInfo is an optional attribute and it doesn't have to be set.
			} catch (WrongAttributeValueException | InternalErrorException e) {
				log.error("Can't resolve auditer's message for " + this.getClass().getSimpleName() + " module because of exception.", e);
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
		attr.setFriendlyName("suspensionInfo");
		attr.setDisplayName("Suspension Info");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Suspension info of a member.");
		return attr;
	}
}
