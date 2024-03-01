package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Attribute module for isCesnetEligibleLastSeen, value is String representing timestamp.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class urn_perun_user_attribute_def_def_isCesnetEligibleLastSeen extends UserAttributesModuleAbstract {

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, User user, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateFormat.setLenient(false);
    try {
      dateFormat.parse(attribute.valueAsString());
    } catch (ParseException ex) {
      throw new WrongAttributeValueException(attribute,
          "Format of timestamp is not correct, it should be 'yyyy-MM-dd HH:mm:ss'", ex);
    }
  }
}
