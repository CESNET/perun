package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.List;

/**
 * This interface serves as a template for virtual attributes.
 *
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Michal Stava <stavamichal@gmail.com>
 */
public interface VirtualAttributesModuleImplApi extends AttributesModuleImplApi {

    /**
     * Get message from auditer, parse it and resolve if is needed to add another messages to DB about virtualAttribute changes.
     *
     * @param perunSession
     * @param message
     * @return list of additional messages for auditer to log it
     * @throws InternalErrorException
     * @throws AttributeNotExistsException
     * @throws WrongReferenceAttributeValueException
     */
    List<String> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, String message) throws InternalErrorException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException;
}
