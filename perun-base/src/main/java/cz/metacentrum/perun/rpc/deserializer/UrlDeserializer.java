package cz.metacentrum.perun.rpc.deserializer;

import cz.metacentrum.perun.core.api.exceptions.RpcException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

/**
 * Deserializer for URL data format.
 * <p>
 * Reads parameters only from URL of request, which is typically GET. Doesn't read any parameters from request body
 * (InputStream)!
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UrlDeserializer extends Deserializer {

  private final HttpServletRequest req;

  /**
   * Create deserializer for URL data format.
   *
   * @param request HttpServletRequest this deserializer is about to process
   */
  public UrlDeserializer(HttpServletRequest request) {
    this.req = request;
  }

  /**
   * Returns {@code true} if value with the specified name is supplied. Check ignores array suffix "[]". It means, that
   * {@code true} is returned for both "name" and "name[]" parameters.
   *
   * @param name name of the value to check
   * @return {@code true} if value with the specified name is supplied, {@code false} otherwise
   */
  @Override
  public boolean contains(String name) {
    return (req.getParameter(name) != null || req.getParameter(name + "[]") != null);
  }

  @Override
  public HttpServletRequest getServletRequest() {
    return this.req;
  }

  public String readAll() {
    StringBuilder stringParams = new StringBuilder();
    for (Enumeration<String> parameters = req.getParameterNames(); parameters.hasMoreElements(); ) {
      String paramName = parameters.nextElement();
      stringParams.append(paramName).append("=").append(req.getParameter(paramName)).append(",");
    }
    return stringParams.toString();
  }

  @Override
  public int[] readArrayOfInts(String name) {
    return readList(name, Integer.class).stream().mapToInt(i -> i).toArray();
  }

  @Override
  public Boolean readBoolean(String name) {
    if (!contains(name)) {
      throw new RpcException(RpcException.Type.MISSING_VALUE, name);
    }

    try {
      // check if parameter in URL isn't passed as number
      // if yes, conform JsonDeserializer implementation
      // => only 0 is considered FALSE, other numbers are TRUE
      int number = Integer.parseInt(req.getParameter(name));
      if (number == 0) {
        return false;
      }
      return true;
    } catch (NumberFormatException ex) {
      // parameter is passed in URL as a String
      return Boolean.parseBoolean(req.getParameter(name));
    }

  }

  @Override
  public int readInt(String name) {
    if (!contains(name)) {
      throw new RpcException(RpcException.Type.MISSING_VALUE, name);
    }

    try {
      return Integer.parseInt(req.getParameter(name));
    } catch (NumberFormatException ex) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, name + " as int", ex);
    }
  }

  @Override
  public <T> List<T> readList(String name, Class<T> valueType) {

    if (!contains(name)) {
      throw new RpcException(RpcException.Type.MISSING_VALUE, name);
    }

    List<T> list = new ArrayList<T>();

    String[] stringParams = req.getParameterValues(name + "[]");
    if (stringParams == null) {
      // submitter probably forgot to add list decoration to param name ("[]").
      stringParams = req.getParameterValues(name);
    }

    for (String param : stringParams) {
      if (valueType.isAssignableFrom(String.class)) {
        list.add(valueType.cast(param));
      } else if (valueType.isAssignableFrom(Integer.class)) {
        list.add(valueType.cast(Integer.valueOf(param)));
      } else if (valueType.isAssignableFrom(Boolean.class)) {
        list.add(valueType.cast(Boolean.valueOf(param)));
      } else if (valueType.isAssignableFrom(Float.class)) {
        list.add(valueType.cast(Float.valueOf(param)));
      }
    }

    return list;
  }

  @Override
  public String readString(String name) {
    if (!contains(name)) {
      throw new RpcException(RpcException.Type.MISSING_VALUE, name);
    }

    return req.getParameter(name);
  }

  @Override
  public UUID readUUID(String name) {
    if (!contains(name)) {
      throw new RpcException(RpcException.Type.MISSING_VALUE, name);
    }

    UUID uuid;
    try {
      uuid = UUID.fromString(req.getParameter(name));
    } catch (IllegalArgumentException e) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, name + " as UUID");
    }

    return uuid;
  }

}
