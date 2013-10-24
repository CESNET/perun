package cz.metacentrum.perun.rpc.deserializer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletRequest;

import cz.metacentrum.perun.rpc.RpcException;

/**
 * Deserializer that reads values as parameters from {@code ServletRequest}. Only the basic read methods (for
 * {@code int} and {@code String}) are implemented.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @version $Id$
 * @since 0.1
 */
public class UrlDeserializer extends Deserializer {
  private ServletRequest req;

  /**
   * @param req {@code ServletRequest} to read values from
   */
  public UrlDeserializer(ServletRequest req) {
    this.req = req;
  }

  @Override
  public boolean contains(String name) {
    return (req.getParameter(name) != null);
  }

  @Override
  public String readString(String name) throws RpcException {
    if (!contains(name)) throw new RpcException(RpcException.Type.MISSING_VALUE, name);

    return req.getParameter(name);
  }

  @Override
  public int readInt(String name) throws RpcException {
    if (!contains(name)) throw new RpcException(RpcException.Type.MISSING_VALUE, name);

    try {
      return Integer.parseInt(req.getParameter(name));
    } catch (NumberFormatException ex) {
      throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, name + " as int", ex);
    }
  }
  
  @Override 
  public <T> List<T> readList(String name, Class<T> valueType) throws RpcException {
    if (!contains(name + "[]")) throw new RpcException(RpcException.Type.MISSING_VALUE, name);
    
    List<T> list = new ArrayList<T>();
    
    String[] stringParams = req.getParameterValues(name + "[]");

    for (String param: stringParams) {
      if (valueType.isAssignableFrom(String.class)) {
        list.add(valueType.cast(param));
      } else if (valueType.isAssignableFrom(Integer.class)) {
        list.add(valueType.cast(Integer.valueOf(param)));
      } else if (valueType.isAssignableFrom(Float.class)) {
        list.add(valueType.cast(Float.valueOf(param)));
      }
    }
    
    return list;
  }

  public String readAll() throws RpcException {
    StringBuffer stringParams = new StringBuffer();
    for (Enumeration<String> parameters = req.getParameterNames(); parameters.hasMoreElements() ;) {
      String paramName = (String) parameters.nextElement();
      stringParams.append(paramName + "=" + req.getParameter(paramName) + " ");
    }
    return stringParams.toString();
  }
}
