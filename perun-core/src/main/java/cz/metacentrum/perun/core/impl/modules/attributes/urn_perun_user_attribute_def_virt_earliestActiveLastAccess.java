package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class urn_perun_user_attribute_def_virt_earliestActiveLastAccess extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {
	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
		Attribute attribute = new Attribute(attributeDefinition);
		List<UserExtSource> ueses = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
		if (ueses == null || ueses.isEmpty()) {
			return attribute;
		}

		int validity = BeansUtils.getCoreConfig().getIdpLoginValidity();
		Optional<UserExtSource> value = ueses.stream()
			.filter(u -> ExtSourcesManager.EXTSOURCE_IDP.equals(u.getExtSource().getType()))
			.filter(u -> LocalDateTime.parse(u.getLastAccess(), Utils.lastAccessFormatter)
				.isAfter(LocalDateTime.now().minusMonths(validity)))
			.min(Comparator.comparing(UserExtSource::getLastAccess));

		attribute.setValue(value.isPresent() ? value.get().getLastAccess() : null);
		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("earliestActiveLastAccess");
		attr.setDisplayName("Earliest active last access");
		attr.setType(String.class.getName());
		attr.setDescription("Timestamp of the earliest active IdP extSource's lastAccess.");
		return attr;
	}
}
