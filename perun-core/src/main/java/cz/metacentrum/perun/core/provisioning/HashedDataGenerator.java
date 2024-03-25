package cz.metacentrum.perun.core.provisioning;


import cz.metacentrum.perun.core.api.HashedGenData;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface HashedDataGenerator {

  /**
   * Generated hashed data structure used for provisioning.
   *
   * @return hashed data structure
   */
  HashedGenData generateData();
}
