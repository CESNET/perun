package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown during application form retrieval for user when any required application form item has no pre-filled
 * value from federation and currently this item is hidden or disabled.
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class NoPrefilledUneditableRequiredDataException extends PerunException {

  private static final long serialVersionUID = 1L;

  private List<ApplicationFormItemWithPrefilledValue> formItems = new ArrayList<>();

  public NoPrefilledUneditableRequiredDataException(String message) {
    super(message);
  }

  public NoPrefilledUneditableRequiredDataException(String message, List<ApplicationFormItemWithPrefilledValue> items) {
    super(message);
    this.formItems = items;
  }

  public List<ApplicationFormItemWithPrefilledValue> getFormItems() {
    return formItems;
  }

  @Override
  public String getMessage() {
    List<String> itemNames = new ArrayList<>();
    if (formItems != null) {
      itemNames = formItems.stream().map(item -> item.getFormItem().getShortname()).toList();
    }
    return super.getMessage() + "\n Item names: " + String.join(", ", itemNames);
  }
}
