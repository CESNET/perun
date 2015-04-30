package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

@SuppressWarnings("serial")
/**
 * Checked RPC exception.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @since 0.1
 */
public class RpcException extends InternalErrorException {

	/**
	 * This enum represents possible RPC error types.
	 */
	public enum Type {

		NO_PATHINFO,
			INVALID_URL,
			UNKNOWN_SERIALIZER_FORMAT,
			UNKNOWN_DESERIALIZER_FORMAT,
			NOT_AN_INTEGER,
			WRONGLY_FORMATTED_CONTENT,
			CANNOT_DESERIALIZE_VALUE,
			CANNOT_SERIALIZE_VALUE,
			MISSING_VALUE,
			WRONG_PARAMETER,
			AMBIGUOUS_CALL,
			UNKNOWN_MANAGER,
			UNKNOWN_METHOD,
			STATE_CHANGING_CALL,
			CLASS_NOT_FOUND,
			UNCATCHED_EXCEPTION,
			NO_REMOTE_USER_SPECIFIED,
			UNKNOWN_PERUN_RPC_SERVER,
			COMMUNICATION_ERROR_WITH_PERUN_RPC_SERVER,
			PERUN_RPC_SERVER_ERROR_HTTP_CODE,
			UNKNOWN_EXCEPTION;
	}
	private final Type type;
	private final String errorInfo;

	/**
	 * @param type type of the error.
	 * @param errorInfo additional error information.
	 */
	public RpcException(Type type, String errorInfo) {
		super(errorInfo == null ? type.toString() : new StringBuilder(type.toString()).append(": ").append(errorInfo).toString());
		this.type = type;
		this.errorInfo = errorInfo;
	}

	/**
	 * @param type type of the error.
	 */
	public RpcException(Type type) {
		this(type, (String) null);
	}

	/**
	 * @param type type of the error.
	 * @param errorInfo additional error information.
	 * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
	 */
	public RpcException(Type type, String errorInfo, Throwable cause) {
		super(errorInfo == null ? type.toString() : new StringBuilder(type.toString()).append(": ").append(errorInfo).toString(), cause);
		this.type = type;
		if (errorInfo == null && cause != null) {
			this.errorInfo = cause.getClass().getSimpleName();
		} else {
			this.errorInfo = errorInfo;
		}
	}

	/**
	 * @param type type of the error.
	 * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
	 */
	public RpcException(Type type, Throwable cause) {
		this(type, null, cause);
	}

	/**
	 * @param type string representation of type of the error.
	 * @param errorInfo additional error information.
	 */
	public RpcException(String type, String errorInfo) {
		this(RpcException.Type.valueOf(type), errorInfo, null);
	}

	/**
	 * @return type of the error.
	 */
	public String getType() {
		return type.name();
	}

	/**
	 * @return additional error information or <tt>null</tt> if there is none.
	 */
	public String getErrorInfo() {
		return errorInfo;
	}
}
