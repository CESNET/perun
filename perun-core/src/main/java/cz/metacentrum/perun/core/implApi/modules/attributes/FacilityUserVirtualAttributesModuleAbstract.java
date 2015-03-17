package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for Facility User Virtual Attributes modules.
 * Implements methods for modules to perform default function.
 * In the function that the method in the module does nothing, it is not necessary to implement it, simply extend this abstract class.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 *
 */
public abstract class FacilityUserVirtualAttributesModuleAbstract extends FacilityUserAttributesModuleAbstract implements FacilityUserVirtualAttributesModuleImplApi{


	public Attribute getAttributeValue(PerunSessionImpl perunSession, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException {
		return new Attribute(attribute);
	}

	public boolean setAttributeValue(PerunSessionImpl perunSession, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		return false;
	}

	public boolean removeAttributeValue(PerunSessionImpl perunSession, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException {
		return false;
	}

	@Override
	public List<String> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, String message) throws InternalErrorException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException{
		return new ArrayList<String>();
	}
}
