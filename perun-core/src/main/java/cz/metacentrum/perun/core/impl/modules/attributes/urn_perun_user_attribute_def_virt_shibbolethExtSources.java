package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pairs of IdP identificator and user's EPPN.
 *
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_shibbolethExtSources extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
		Map<String, String> idpLogins = new LinkedHashMap<>();
		List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);

		int i=1;
		for(UserExtSource uES: userExtSources) {
			if(uES.getExtSource() != null) {
				String login = uES.getLogin();
				String type = uES.getExtSource().getType();
				String idpIdentifier = uES.getExtSource().getName();

				if(type != null && login != null) {
					if(type.equals(ExtSourcesManager.EXTSOURCE_IDP)) {
						idpLogins.put(i + ":" + idpIdentifier, login);
						i++;
					}
				}
			}
		}

		Attribute attribute = new Attribute(attributeDefinition);
		attribute.setValue(idpLogins);
		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("shibbolethExtSources");
		attr.setDisplayName("Shibboleth external sources");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Pairs of IdP identificator and user's EPPN.");
		return attr;
	}
}
