package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.List;

/**
 * This interface serves as a template for virtual attributes.
 *
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public interface UserVirtualAttributesModuleImplApi extends UserAttributesModuleImplApi, VirtualAttributesModuleImplApi {

	/**
	 * This method will return computed value.
	 *
	 * @param perunSession perun session
	 * @param user user which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	Attribute getAttributeValue(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Method sets attributes' values which are dependent on this virtual attribute.
	 *
	 * @param perunSession
	 * @param user user which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return true if attribute was really changed
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	boolean setAttributeValue(PerunSessionImpl perunSession, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException;

	/**
	 * Currently do nothing.
	 *
	 * @param perunSession
	 * @param user user which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAttributeValue(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Method searches all users, who have attribute with the given value.
	 * The rules of the match are dependent on the type of the attribute:
	 * <table>
	 * <tr><td><em>String</em></td> <td>match in substring</td></tr>
	 * <tr><td><em>List</em></td> <td>match in one item</td></tr>
	 * <tr><td><em>Map</em></td> <td>match in key or value</td></tr>
	 * </table>
	 *
	 * @param perunSession perun session
	 * @param attribute attribute with value, which all returned users have to have
	 * @return all users, who have attribute with given value
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	List<User> searchInAttributesValues(PerunSessionImpl perunSession, String attribute) throws InternalErrorException;
}
