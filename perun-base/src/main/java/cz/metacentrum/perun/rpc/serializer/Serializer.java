package cz.metacentrum.perun.rpc.serializer;

import java.io.IOException;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
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

	/**
	 * @return Content-type of this serializer's output format and its encoding.
	 */
	String getContentType();

	void write(Object object) throws IOException;

	/**
	 * Serializes {@code PerunException}.
	 *
	 * @param pex {@code PerunException} to serialize
	 * @throws IOException If an IO error occurs
	 */
	void writePerunException(PerunException pex) throws IOException;

	/**
	 * Serializes {@code PerunRuntimeException}.
	 *
	 * @param prex {@code PerunRuntimeException} to serialize
	 * @throws IOException If an IO error occurs
	 */
	void writePerunRuntimeException(PerunRuntimeException prex) throws IOException;
}
