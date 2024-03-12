package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import java.util.List;

/**
 * Thrown when initial application for the user already exists.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class DuplicateRegistrationAttemptException extends PerunException {

  private final Application application;
  private final List<ApplicationFormItemData> applicationData;

  public DuplicateRegistrationAttemptException(String message, Application application,
                                               List<ApplicationFormItemData> data) {
    super(message);
    this.application = application;
    this.applicationData = data;
  }

  public Application getApplication() {
    return application;
  }

  public List<ApplicationFormItemData> getApplicationData() {
    return applicationData;
  }

}
