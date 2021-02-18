package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * @author vojtech sassmann <vojtech.sassmann@gmail.com>
 */
public abstract class AbstractApplicationAutoRejectMessagesModule<T extends PerunBean> extends AttributesModuleAbstract implements AttributesModuleImplApi {

	private static final Pattern IGNORED_BY_ADMIN_PATTERN = Pattern.compile("^ignoredByAdmin(-(\\w+))?$");
	private static final Pattern MAIL_VERIFICATION_PATTERN = Pattern.compile("^emailVerification(-(\\w+))?$");

	public void checkAttributeSyntax(PerunSessionImpl perunSession, T entity, Attribute attribute) throws WrongAttributeValueException {
		LinkedHashMap<String, String> values = attribute.valueAsMap();
		for (String key : values.keySet()) {
			if (isInvalidKey(key)) {
				throw new WrongAttributeValueException(attribute, "Key '" + key + "' has an invalid format.");
			}
		}
	}

	/**
	 * Returns true, if the given key has a valid format.
	 *
	 * @param key key representing rejection message type and language
	 * @return Returns true, if the given key has a valid format, false otherwise.
	 */
	private boolean isInvalidKey(String key) {
		return !IGNORED_BY_ADMIN_PATTERN.matcher(key).matches() &&
				!MAIL_VERIFICATION_PATTERN.matcher(key).matches();
	}
}
