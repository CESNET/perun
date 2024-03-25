package cz.metacentrum.perun.core.api.exceptions.rt;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * This exception is thrown when the loading of module configuration property is not found or it fails.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class ModulePropertyNotFoundException extends InternalErrorException {
  public ModulePropertyNotFoundException(String message) {
    super(message);
  }

  public ModulePropertyNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ModulePropertyNotFoundException(Throwable cause) {
    super(cause);
  }

  public ModulePropertyNotFoundException(String module, String property) {
    super("Failed to parse property: '" + property + "' for module: " + module);
  }
}
