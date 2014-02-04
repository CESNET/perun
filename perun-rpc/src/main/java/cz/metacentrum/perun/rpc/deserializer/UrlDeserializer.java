package cz.metacentrum.perun.rpc.deserializer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import cz.metacentrum.perun.rpc.RpcException;

/**
 * Deserializer for URL data format.
 *
 * Reads parameters only from URL of request, which is typically GET.
 * Doesn't read any parameters from request body (InputStream)!
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UrlDeserializer extends Deserializer {

    private HttpServletRequest req;

    /**
     * Create deserializer for URL data format.
     *
     * @param req HttpServletRequest this deserializer is about to process
     */
    public UrlDeserializer(HttpServletRequest req) {
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

    @Override
    public HttpServletRequest getServletRequest() {
        return this.req;
    }

}
