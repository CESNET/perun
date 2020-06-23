package cz.metacentrum.perun.rpclib.api;

import java.io.IOException;

import cz.metacentrum.perun.core.api.exceptions.RpcException;

/**
 * Subclasses of {@code Serializer} class provide methods to serialize PerunAPI's datatypes (as JSON, for example).
 * Implementing any of the write* methods except {@code write(PerunException)}, {@code write(PerunRuntimeException)} is
 * optional.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @since 0.1
 */
public abstract interface Serializer {
	void write(Object object) throws IOException;
}
