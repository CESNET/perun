package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Extracts BBMRI user ID (originally Perun DB ID for user in BBMRI-ERIC AAI) from the BBMRI ExtSource
 * BBMRI ExtSource object has to be named "BBMRI id"
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_bbmriUserId
	extends UserVirtualAttributesModuleAbstract
	implements UserVirtualAttributesModuleImplApi
{

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public static final String BBMRI_ES_NAME = "BBMRI id";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
		Attribute attribute = new Attribute(attributeDefinition);

		List<UserExtSource> extSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
		for (UserExtSource ues: extSources) {
			if (ues == null) {
				log.warn("Found null UserExtSource for user '{}'", user);
				continue;
			} else if (ues.getExtSource() == null) {
				log.warn("Found UserExtSource with null ExtSource ({}) for user '{}'", ues, user);
				continue;
			}
			ExtSource es = ues.getExtSource();
			if (!ExtSourcesManager.EXTSOURCE_INTERNAL.equals(es.getType())) {
				continue;
			} else if (!BBMRI_ES_NAME.equals(es.getName())) {
				continue;
			}
			try {
				attribute.setValue(Integer.parseInt(ues.getLogin()));
			} catch (NumberFormatException ex) {
				log.error("Invalid login set for {} UserExtSource ({}) - {} could not be parsed as integer",
					BBMRI_ES_NAME, ues, ues.getLogin());
			}
			break;
		}
		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("bbmriUserId");
		attr.setDisplayName("BBMRI User ID");
		attr.setType(Integer.class.getName());
		attr.setDescription("Original (Perun DB) user ID used previously in BBMRI (read from user ext source)");
		return attr;
	}

}
