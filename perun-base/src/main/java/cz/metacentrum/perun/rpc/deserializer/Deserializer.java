package cz.metacentrum.perun.rpc.deserializer;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.RpcException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Subclasses of {@code Deserializer} class provide methods to deserialize values supplied as name/value pairs in some
 * form (ie. in JSON or as HTTP request parameters). It is presumed that the names of the values are unique - results
 * are undefined if multiple values with the same name are supplied. Implementing any of the read* methods is optional.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
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
	public abstract String readString(String name);

	/**
	 * Reads value with the specified name as {@code Boolean}.
	 *
	 * @param name name of the value to read
	 * @return the value as {@code Boolean}
	 *
	 * @throws RpcException If the specified value cannot be parsed as {@code String} or if it is not supplied
	 */
	public abstract Boolean readBoolean(String name);

	/**
	 * Reads value with the specified name as {@code int}.
	 *
	 * @param name name of the value to read
	 * @return the value as {@code int}
	 *
	 * @throws RpcException if the specified value cannot be parsed as {@code int} or if it is not supplied
	 */
	public abstract int readInt(String name);

	/**
	 * Reads LocalDate value with the specified name. Expected ISO-8601 format. (yyyy-MM-dd)
	 *
	 * @param name name of the localDate value
	 * @return parsed local date
	 */
	public LocalDate readLocalDate(String name) {
		String date = readString(name);
		return date == null ? null : LocalDate.parse(date);
	}

	public int[] readArrayOfInts(String name) {
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
	public <T> T read(String name, Class<T> valueType) {
		throw new UnsupportedOperationException("read(String name, Class<T> valueType)");
	}

	/**
	 * Reads value from root Json node.
	 *
	 * @param valueType type of the value to read
	 * @return the value as {@code valueType}
	 *
	 * @throws UnsupportedOperationException if this deserializer does not implement this method
	 * @throws RpcException if the specified value cannot be parsed as {@code valueType} or if it is not supplied
	 */
	public <T> T read(Class<T> valueType) {
		throw new UnsupportedOperationException("read(Class<T> valueType)");
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
	public <T> List<T> readList(String name, Class<T> valueType) {
		throw new UnsupportedOperationException("readList(String name, Class<T> valueType)");
	}

	/**
	 * Reads array from root Json node.
	 *
	 * @param valueType type of the value to read
	 * @return the value as {@code List<valueType>}
	 *
	 * @throws UnsupportedOperationException if this deserializer does not implement this method
	 * @throws RpcException if the specified value cannot be parsed as {@code valueType} or if it is not supplied
	 */
	public <T> List<T> readList(Class<T> valueType) {
		throw new UnsupportedOperationException("readList(Class<T> valueType)");
	}

	/**
	 * Reads object with the specified name as {@code Map<String, String>}.
	 *
	 * @param name name of the array to read
	 * @return the value as {@code Map<String, String>}
	 *
	 * @throws UnsupportedOperationException if this deserializer does not implement this method
	 * @throws RpcException if the specified value cannot be parsed as {@code Map<String, String>} or if it is not supplied
	 */
	public Map<String, String> readMap(String name) {
		throw new UnsupportedOperationException("readMap(String name)");
	}

	/**
	 * Reads value with the specified name as {@code PerunBean}.
	 *
	 * @param name name of the value to read
	 * @return the value as {@code PerunBean}
	 *
	 * @throws UnsupportedOperationException if this deserializer does not implement this method
	 * @throws RpcException if the specified value cannot be parsed as {@code perunBean} or if it is not supplied
	 */
	public PerunBean readPerunBean(String name) {
		throw new UnsupportedOperationException("readListPerunBeans(String name)");
	}

	/**
	 * Reads array with the specified name as {@code List<PerunBean>}.
	 *
	 * @param name name of the array to read
	 *
	 * @return the value as {@code List<PerunBean>}
	 * @throws UnsupportedOperationException if this deserializer does not implement this method
	 * @throws RpcException if the specified value cannot be parsed as {@code perunBean} or if it is not supplied
	 */
	public List<PerunBean> readListPerunBeans(String name) {
		throw new UnsupportedOperationException("readListPerunBeans(String name)");
	}

	/**
	 * Returns string representation of the variables stored in the deserializer.
	 *
	 * @return string containing all variables
	 * @throws RpcException
	 */
	public abstract String readAll();

	/**
	 * Return HttpServletRequest related to concrete call this deserializer is used to process.
	 *
	 * Note that this "request" is not necessarily used as source to read parameters by
	 * other methods of deserializer. It IS typically for GET requests, but NOT for POST with
	 * JSON/JSONP data format.
	 *
	 * @return HttpServletRequest related to concrete call
	 * @throws UnsupportedOperationException if this deserializer does not implement this method
	 */
	public HttpServletRequest getServletRequest() {
		throw new UnsupportedOperationException("getServletRequest()");
	}

	/**
	 * Check whether method that changes state is not GET.
	 *
	 * @throws RpcException if method is GET
	 */
	public void stateChangingCheck() {
		if (getServletRequest().getMethod().equals("GET")) {
			throw new RpcException(RpcException.Type.STATE_CHANGING_CALL, "This is a state changing operation. Please use HTTP POST request.");
		}
	}
}
