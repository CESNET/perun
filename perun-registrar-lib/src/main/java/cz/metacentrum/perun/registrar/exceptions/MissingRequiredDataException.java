package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when any required application form item is missing it's pre-filled value from federation, during
 * application form retrieval for user.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MissingRequiredDataException extends PerunException {

  private static final long serialVersionUID = 1L;

  private List<ApplicationFormItemWithPrefilledValue> formItems = new ArrayList<>();

  public MissingRequiredDataException(String message) {
    super(message);
  }

  public MissingRequiredDataException(String message, List<ApplicationFormItemWithPrefilledValue> items) {
    super(message);
    this.formItems = items;
  }

  public MissingRequiredDataException(String message, Throwable ex) {
    super(message, ex);
  }

  public MissingRequiredDataException(String message, Throwable ex, List<ApplicationFormItemWithPrefilledValue> items) {
    super(message, ex);
    this.formItems = items;
  }

  public void addFormItem(ApplicationFormItemWithPrefilledValue item) {
    if (formItems == null) {
      formItems = new ArrayList<>();
    }
    if (item != null) {
      formItems.add(item);
    }
  }

  public List<ApplicationFormItemWithPrefilledValue> getFormItems() {
    return formItems;
  }

  @Override
  public String getMessage() {
    StringBuilder problematicItemsInfoBuilder = new StringBuilder();
    if (formItems != null) {
      for (ApplicationFormItemWithPrefilledValue item : formItems) {
        problematicItemsInfoBuilder
            .append("\n")
            .append(item.getFormItem().getShortname())
            .append(" filled from ")
            .append(item.getFormItem().getFederationAttribute() != null ?
                "federation attribute " + item.getFormItem().getFederationAttribute() :
                "perun attribute " + item.getFormItem().getPerunSourceAttribute());
      }
    }
    return super.getMessage() + problematicItemsInfoBuilder;
  }
}
