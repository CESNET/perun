package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;

/**
 * All alternative logins of user collected from UserExtSources attributes
 * as list of schacHomeOrganization:altLogin
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_alternativeLoginNames extends UserVirtualAttributeCollectedFromUserExtSource {

	@Override
	public String getSourceAttributeFriendlyName() {
		return "alternativeLoginName";
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "alternativeLoginNames";
	}

	@Override
	public String getDestinationAttributeDisplayName() {
		return "Alternative login names";
	}

	@Override
	public String getDestinationAttributeDescription() {
		return "List of all alternative logins of user in organizations represented as tuples of entityId:alternativeLogin";
	}

	@Override
	public String modifyValue(PerunSession session, ModifyValueContext ctx, UserExtSource ues, String value) {

		if (ues != null && ExtSourcesManager.EXTSOURCE_IDP.equals(ues.getExtSource().getType())) {
			try {
				Attribute schacAttribute = ((PerunSessionImpl)session).getPerunBl().getAttributesManagerBl().getAttribute(session, ues, AttributesManager.NS_UES_ATTR_DEF + ":schacHomeOrganization");
				if (schacAttribute.getValue() != null) {
					return schacAttribute.getValue() + ":" + value;
				}
			} catch (InternalErrorException |WrongAttributeAssignmentException | AttributeNotExistsException e) {
				return null;
			}
		}

		return null;

	}

}
