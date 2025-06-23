package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when any required application form item is missing it's pre-filled value from certificate, during
 * application form retrieval for user.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class MissingRequiredDataCertException extends PerunException {

  private static final long serialVersionUID = 1L;

  private List<ApplicationFormItemWithPrefilledValue> formItems = new ArrayList<>();

  public MissingRequiredDataCertException(String message) {
    super(message);
  }

  public MissingRequiredDataCertException(String message, List<ApplicationFormItemWithPrefilledValue> items) {
    super(message);
    this.formItems = items;
  }

  public MissingRequiredDataCertException(String message, Throwable ex) {
    super(message, ex);
  }

  public MissingRequiredDataCertException(String message, Throwable ex,
                                          List<ApplicationFormItemWithPrefilledValue> items) {
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
                "certificate attribute " + item.getFormItem().getFederationAttribute() :
                "perun attribute " + item.getFormItem().getPerunSourceAttribute());
      }
    }
    return super.getMessage() + problematicItemsInfoBuilder;
  }
}
