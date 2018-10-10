package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attribute value depends on login-namespace:mu attribute
 *
 * @author Simona Kruppova, Michal Stava
 */
public class urn_perun_user_attribute_def_virt_optionalLogin_namespace_mu extends UserVirtualAttributesModuleAbstract {

	private final String EXTSOURCE_MUNI_IDP2 = "https://idp2.ics.muni.cz/idp/shibboleth";
	private static final Pattern loginMUPattern = Pattern.compile("^([0-9]+)[@]muni[.]cz$");

	private static final String A_U_D_loginNamespace_mu = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:mu";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);

		try {
			Attribute loginInMU = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_D_loginNamespace_mu);
			attribute = Utils.copyAttributeToVirtualAttributeWithValue(loginInMU, attribute);
		} catch (AttributeNotExistsException ex) {
			//That means that mu login attribute not exists at all
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		//if attribute is still null (empty login in mu or not existing attribute), try to find uco in user ext sources
		if(attribute.getValue() == null) {
			List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
			for(UserExtSource userExtSource : userExtSources) {
				ExtSource extSource = userExtSource.getExtSource();

				//Skip if extSource is not the one we are looking for
				if(userExtSource.getLogin() == null || extSource == null) continue;
				if(!ExtSourcesManager.EXTSOURCE_IDP.equals(extSource.getType())) continue;
				if(!EXTSOURCE_MUNI_IDP2.equals(extSource.getName())) continue;

				//Get login from this extSource and get only UCO from it
				String login = userExtSource.getLogin();
				Matcher loginMUMatcher = loginMUPattern.matcher(login);
				//This user has login in mu, but in weird format so skip this one
				if(!loginMUMatcher.find()) continue;
				//It is ok, take UCO from login and set it to attribute value
				String UCO = loginMUMatcher.group(1);
				attribute.setValue(UCO);
				break;
			}
		}

		return attribute;
	}

	@Override
	public List<String> getStrongDependencies() {
		return Collections.singletonList(A_U_D_loginNamespace_mu);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("optionalLogin-namespace:mu");
		attr.setDisplayName("MU login (if available)");
		attr.setType(String.class.getName());
		attr.setDescription("Masaryk University login (if available)");
		return attr;
	}
}
