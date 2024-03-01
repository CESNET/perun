package cz.metacentrum.perun.core.api;

/**
 * Switch to choose between sync or async or none member validation in all member creation-like methods.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public enum Validation {
  NONE,
  SYNC,
  ASYNC
}
