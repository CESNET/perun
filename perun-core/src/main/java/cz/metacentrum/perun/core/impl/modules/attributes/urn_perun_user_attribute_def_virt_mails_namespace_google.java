package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks specified users mails in google namespace.
 *
 * @author Michal Stava  &lt;stavamichal@gmail.com&gt;
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_mails_namespace_google extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_mails_namespace_google.class);
	private static final String EXTSOURCE = "https://login.cesnet.cz/google-idp/";
	private static final Pattern pattern = Pattern.compile("^.+[@]google[.]extidp[.]cesnet[.]cz$");
	private static final String A_UES_MAIL = AttributesManager.NS_UES_ATTR_DEF + ":mail";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Set<String> googleMails = new HashSet<>();
		List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);

		for(UserExtSource uES: userExtSources) {
			if(uES.getExtSource() != null && EXTSOURCE.equals(uES.getExtSource().getName())) {
				String login = uES.getLogin();
				if (login != null && !login.isEmpty()) {
					Matcher matcher = pattern.matcher(login);
					if (matcher.matches()) {
						try {
							Attribute attribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, uES, A_UES_MAIL);
							if (attribute.getValue() != null) {
								googleMails.add((String) attribute.getValue());
							}
						} catch (AttributeNotExistsException ex) {
							//This should not happen, but if yes, skip this value
							log.error("Attribute {} not exists in Perun so value {} google mails can't be get correctly.", A_UES_MAIL, user);
						} catch (WrongAttributeAssignmentException ex) {
							throw new InternalErrorException(ex);
						}
					}
				}
			}
		}

		Attribute attribute = new Attribute(attributeDefinition);
		attribute.setValue(new ArrayList<>(googleMails));
		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		attr.setFriendlyName("mails-namespace:google");
		attr.setDisplayName("Emails in namespace:google");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("Emails in google namespace");
		return attr;
	}
}
