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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User edu Person principal Names (eppn)
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_eduPersonPrincipalNames extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("[^@]+@[^@]+");

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
		List<String> idpLogins = new ArrayList<>();
		List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);

		for(UserExtSource uES: userExtSources) {
			if(uES.getExtSource() != null) {
				String login = uES.getLogin();
				String type = uES.getExtSource().getType();

				if(type != null && login != null) {
					// insert only EPPN formatted data
					Matcher matcher = pattern.matcher(login);
					if(type.equals(ExtSourcesManager.EXTSOURCE_IDP) && matcher.matches()) {
						idpLogins.add(login);
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
		attr.setFriendlyName("eduPersonPrincipalNames");
		attr.setDisplayName("EPPN");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("Extsource logins from IDP.");
		return attr;
	}

	@Override
	public List<User> searchInAttributesValues(PerunSessionImpl perunSession, String login) {
		if (login == null) return null;
		return perunSession.getPerunBl().getUsersManagerBl().getUsersByExtSourceTypeAndLogin(perunSession, ExtSourcesManager.EXTSOURCE_IDP, login);
	}
}
