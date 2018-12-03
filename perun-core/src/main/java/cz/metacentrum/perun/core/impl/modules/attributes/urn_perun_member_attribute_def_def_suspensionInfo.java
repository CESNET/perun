package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Message with information about suspension of a member.
 *
 * @author Metodej Klang <metodej.klang@gmail.com>
 */
public class urn_perun_member_attribute_def_def_suspensionInfo extends MemberAttributesModuleAbstract {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_member_attribute_def_def_suspensionInfo.class);

	private final Pattern statusChange = Pattern.compile("Member:\\[id=<([0-9]+)>.*] ([a-z]+).", Pattern.DOTALL);

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
	public List<String> resolveVirtualAttributeValueChange(PerunSessionImpl session, String message) throws WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {

		Matcher statusChangeMatcher = statusChange.matcher(message);

		if (statusChangeMatcher.find() && (statusChangeMatcher.group(2).equals("expired") || statusChangeMatcher.group(2).equals("disabled") || statusChangeMatcher.group(2).equals("validated"))) {
			try {
				int memberId = Integer.valueOf(statusChangeMatcher.group(1));
				Member member = session.getPerunBl().getMembersManagerBl().getMemberById(session, memberId);
				Attribute attribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, member, AttributesManager.NS_MEMBER_ATTR_DEF + ":suspensionInfo");
				attribute.setValue(new LinkedHashMap<String, String>());
				session.getPerunBl().getAttributesManagerBl().setAttribute(session, member, attribute);
			} catch (AttributeNotExistsException e) {
				//suspensionInfo is an optional attribute and it doesn't have to be set.
			} catch (MemberNotExistsException | WrongAttributeValueException | InternalErrorException e) {
				log.error("Can't resolve auditer's message for " + this.getClass().getSimpleName() + " module because of exception.", e);
			}
		}

		return new ArrayList<>();
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
		attr.setFriendlyName("suspensionInfo");
		attr.setDisplayName("Suspension Info");
		attr.setType(String.class.getName());
		attr.setDescription("Suspension info of a member.");
		return attr;
	}
}
