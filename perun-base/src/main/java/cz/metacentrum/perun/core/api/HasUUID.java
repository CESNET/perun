package cz.metacentrum.perun.core.api;

import java.util.UUID;

/**
 * This interface represents objects, which has a UUID.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface HasUUID {

  /**
   * Get UUID.
   *
   * @return UUID of the object
   */
  UUID getUuid();
}
