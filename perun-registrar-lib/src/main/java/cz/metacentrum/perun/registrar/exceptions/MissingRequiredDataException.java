package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;

/**
 * Exception thrown when any required application form item
 * is missing it's pre-filled value from federation, during
 * application form retrieval for user.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MissingRequiredDataException extends PerunException {

	private static final long serialVersionUID = 1L;

    private ApplicationFormItemWithPrefilledValue formItem;

	public MissingRequiredDataException(String message) {
        super(message);
    }

    public MissingRequiredDataException(String message, ApplicationFormItemWithPrefilledValue object) {
        super(message);
        this.formItem = object;
    }

    public MissingRequiredDataException(String message, Throwable ex) {
        super(message, ex);
    }

    public MissingRequiredDataException(String message, Throwable ex, ApplicationFormItemWithPrefilledValue object) {
        super(message, ex);
        this.formItem = object;
    }

    public Object getFormItem() {
        return formItem;
    }

}