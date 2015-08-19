package cz.metacentrum.perun.rpclib.api;

import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.RpcException;

/**
 * Subclasses of {@code Deserializer} class provide methods to deserialize values supplied as name/value pairs in some
 * form (ie. in JSON or as HTTP request parameters). It is presumed that the names of the values are unique - results
 * are undefined if multiple values with the same name are supplied. Implementing any of the read* methods is optional.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @since 0.1
 */
public abstract class Deserializer {

	/**
	 * Returns {@code true} if value with the specified name is supplied.
	 *
	 * @param name name of the value to check
	 * @return {@code true} if value with the specified name is supplied, {@code false} otherwise
	 */
	public abstract boolean contains(String name);

	/**
	 * Reads value with the specified name as {@code String}.
	 *
	 * @param name name of the value to read
	 * @return the value as {@code String}
	 *
	 * @throws RpcException If the specified value cannot be parsed as {@code String} or if it is not supplied
	 */
	public abstract String readString(String name) throws RpcException;

	/**
	 * Reads value as {@code String}.
	 *
	 * @return the value as {@code String}
	 *
	 * @throws RpcException If the specified value cannot be parsed as {@code String} or if it is not supplied
	 */
	public abstract String readString() throws RpcException;


	/**
	 * Reads value with the specified name as {@code int}.
	 *
	 * @param name name of the value to read
	 * @return the value as {@code int}
	 *
	 * @throws RpcException if the specified value cannot be parsed as {@code int} or if it is not supplied
	 */
	public abstract int readInt(String name) throws RpcException;

	/**
	 * Reads value as {@code int}.
	 *
	 * @return the value as {@code int}
	 *
	 * @throws RpcException if the specified value cannot be parsed as {@code int} or if it is not supplied
	 */
	public abstract int readInt() throws RpcException;

	public int[] readArrayOfInts(String name) throws RpcException {
		throw new UnsupportedOperationException("readArrayOfInts(String name)");
	}

	public int[] readArrayOfInts() throws RpcException {
		throw new UnsupportedOperationException("readArrayOfInts(String name)");
	}

	/**
	 * Reads value with the specified name as {@code valueType}.
	 *
	 * @param name name of the value to read
	 * @param valueType type of the value to read
	 * @return the value as {@code valueType}
	 *
	 * @throws UnsupportedOperationException if this deserializer does not implement this method
	 * @throws RpcException if the specified value cannot be parsed as {@code valueType} or if it is not supplied
	 */
	public <T> T read(String name, Class<T> valueType) throws RpcException {
		throw new UnsupportedOperationException("read(String name, Class<T> valueType)");
	}

	/**
	 * Reads value as {@code valueType}.
	 *
	 * @param valueType type of the value to read
	 * @return the value as {@code valueType}
	 *
	 * @throws UnsupportedOperationException if this deserializer does not implement this method
	 * @throws RpcException if the specified value cannot be parsed as {@code valueType} or if it is not supplied
	 */
	public <T> T read(Class<T> valueType) throws RpcException {
		throw new UnsupportedOperationException("read(String name, Class<T> valueType)");
	}

	/**
	 * Reads array with the specified name as {@code List<valueType>}.
	 *
	 * @param name name of the array to read
	 * @param valueType type of the value to read
	 * @return the value as {@code List<valueType>}
	 *
	 * @throws UnsupportedOperationException if this deserializer does not implement this method
	 * @throws RpcException if the specified value cannot be parsed as {@code valueType} or if it is not supplied
	 */
	public <T> List<T> readList(String name, Class<T> valueType) throws RpcException {
		throw new UnsupportedOperationException("readList(String name, Class<T> valueType)");
	}

	/**
	 * Reads an array {@code List<valueType>}.
	 *
	 * @param valueType type of the value to read
	 * @return the value as {@code List<valueType>}
	 *
	 * @throws UnsupportedOperationException if this deserializer does not implement this method
	 * @throws RpcException if the specified value cannot be parsed as {@code valueType} or if it is not supplied
	 */
	public <T> List<T> readList(Class<T> valueType) throws RpcException {
		throw new UnsupportedOperationException("readList(String name, Class<T> valueType)");
	}
}
