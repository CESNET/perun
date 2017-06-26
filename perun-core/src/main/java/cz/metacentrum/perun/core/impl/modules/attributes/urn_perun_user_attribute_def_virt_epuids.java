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
import java.util.List;

/**
 * All eduPersonUniqIds collected form UserExtSources attributes.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class urn_perun_user_attribute_def_virt_epuids extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_epuids.class);

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {

		Attribute attribute = new Attribute(attributeDefinition);
		List<String> epuids = new ArrayList<>();

		List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
		AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();
		for (UserExtSource userExtSource : userExtSources) {
			try {
				Attribute a = am.getAttribute(sess, userExtSource, "urn:perun:ues:attribute-def:def:epuid");
				Object value = a.getValue();
				if(value!=null && value instanceof String) {
					epuids.add(String.valueOf(value));
				}
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
				log.error("cannot read epuid from userExtSource "+userExtSource.getId()+" of user "+user.getId(),e);
			}
		}
		attribute.setValue(epuids);
		return attribute;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("epuids");
		attr.setDisplayName("eduPersonUniqueIds");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("All eduPersonUniqueIds of a user");
		return attr;
	}
}
