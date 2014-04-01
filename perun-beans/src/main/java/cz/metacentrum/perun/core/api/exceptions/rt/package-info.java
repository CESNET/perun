/**
 * Runtime exceptions. The Grouper transaction callbacks do not allow new types of checked exceptions.
 * That is why only runtime exceptions can be throws inside the callbacks.
 * Thus this package has runtime versions of Perun exceptions. On the topmost level
 * they should be converted to checked exceptions.
 *
 * The base exception is {@link cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException}.
 *
 *
 */
package cz.metacentrum.perun.core.api.exceptions.rt;
