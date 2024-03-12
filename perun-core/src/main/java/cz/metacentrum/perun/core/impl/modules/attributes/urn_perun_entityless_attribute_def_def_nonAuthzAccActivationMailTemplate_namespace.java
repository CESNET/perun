package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;
import java.util.regex.Pattern;

/**
 * Module for non authorized account activation mail template attribute.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class urn_perun_entityless_attribute_def_def_nonAuthzAccActivationMailTemplate_namespace
    extends EntitylessAttributesModuleAbstract {

  // matches tag {link-*}
  private static final Pattern languageLinkTagPattern = Pattern.compile("\\{link-[^}]+}");

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, String key, Attribute attribute)
      throws WrongAttributeValueException {
    String value = attribute.valueAsString();
    if (value == null) {
      return;
    }

    if (!value.contains("{link}") && !languageLinkTagPattern.matcher(value).find()) {
      throw new WrongAttributeValueException(attribute, key,
          "Value must contain tag {link} or {link-*} where * represents language, e.g. {link-en}.");
    }
  }
}
