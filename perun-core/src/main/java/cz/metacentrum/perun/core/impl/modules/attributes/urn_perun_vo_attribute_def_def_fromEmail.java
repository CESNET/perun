package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jiří Mauritz
 * @edited Peter Balcirak <peter.balcirak@gmail.com>
 */
public class urn_perun_vo_attribute_def_def_fromEmail extends VoAttributesModuleAbstract
    implements VoAttributesModuleImplApi {

  private static final Pattern pattern = Pattern.compile("^\".+\" <.+>$");

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, Vo vo, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    // null attribute
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, "Vo fromEmail cannot be null.");
    }
  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, Vo vo, Attribute attribute)
      throws WrongAttributeValueException {
    // null attribute is ok for syntax check
    if (attribute.getValue() == null) {
      return;
    }

    String fromEmail = attribute.valueAsString();

    if (!(sess.getPerunBl().getModulesUtilsBl().isNameOfEmailValid(sess, fromEmail))) {

      Matcher match = pattern.matcher(fromEmail);

      if (!match.matches()) {
        throw new WrongAttributeValueException(attribute, "Vo : " + vo.getName() + " has fromEmail " + fromEmail +
                                                          " which is not valid. It has to be in form \"header\" " +
                                                          "<correct email> or just correct email.");
      } else {

        String[] emailParts = fromEmail.split("[<>]+");

        if (!(sess.getPerunBl().getModulesUtilsBl().isNameOfEmailValid(sess, emailParts[1]))) {
          throw new WrongAttributeValueException(attribute,
              "Vo : " + vo.getName() + " has email in <> " + emailParts[1] + " which is not valid.");
        }
      }
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
    attr.setFriendlyName("fromEmail");
    attr.setDisplayName("\"From\" email address");
    attr.setType(String.class.getName());
    attr.setDescription("Email address used as \"from\" in mail notifications");
    return attr;
  }
}
