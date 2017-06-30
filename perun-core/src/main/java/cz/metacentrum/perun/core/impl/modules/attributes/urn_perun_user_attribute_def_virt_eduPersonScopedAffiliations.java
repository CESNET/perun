package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * All affiliations collected from UserExtSources attributes.
 * Afilliation is a multi-valued attribute, but Apache joins multiple values using ';',
 * thus we must split it up here.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations.class);

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {

		Attribute attribute = new Attribute(attributeDefinition);
		List<String> values = new ArrayList<>();

		List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
		AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();
		for (UserExtSource userExtSource : userExtSources) {
			try {
				Attribute a = am.getAttribute(sess, userExtSource, "urn:perun:ues:attribute-def:def:affiliation");
				Object value = a.getValue();
				if(value!=null && value instanceof String) {
					String affiliation = (String) value;
					if(affiliation.contains(";")) {
						//multiple values
						values.addAll(Arrays.asList(affiliation.split(";")));
					} else {
						//just one value
						values.add(affiliation);
					}
				}
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
				log.error("cannot read affiliation from userExtSource "+userExtSource.getId()+" of user "+user.getId(),e);
			}
		}
		attribute.setValue(values);
		return attribute;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("eduPersonScopedAffiliations");
		attr.setDisplayName("eduPersonScopedAffiliations");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("All affiliations of a user");
		return attr;
	}
}
