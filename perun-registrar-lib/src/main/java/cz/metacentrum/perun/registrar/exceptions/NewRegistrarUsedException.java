package cz.metacentrum.perun.registrar.exceptions;

public class NewRegistrarUsedException extends RegistrarException {
  private static final long serialVersionUID = 1L;

  public NewRegistrarUsedException(String message) {
    super(message);
  }

  public NewRegistrarUsedException(String message, Throwable ex) {
    super(message, ex);
  }
}
