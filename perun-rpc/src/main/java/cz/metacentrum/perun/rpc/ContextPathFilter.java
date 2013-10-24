package cz.metacentrum.perun.rpc;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * Filter for allowing multiple context paths for one webapp.
 * <p>
 * It is not possible to have one instance of a deployed webapp in Tomcat assigned to multiple context paths.
 * This filter allows it by receiving the original context path from Apache through AJP protocol.
 * </p><p>
 * Requires APACHE_REQUEST_URI to be set in Apache using
 * </p>
 * <pre>
 *  RewriteEngine On
 *  RewriteRule .* - [E=AJP_APACHE_REQUEST_URI:%{REQUEST_URI}]
 *  ProxyPass /aaa ajp://localhost:8009/multi
 *  ProxyPass /bbb ajp://localhost:8009/multi
 *  ProxyPass /ccc ajp://localhost:8009/multi
 * </pre>
 *
 * For sessions to work on alternative mappings, it is necessary to set in META-INF/context.xml
 *
 * <pre>
 *    &lt;Context sessionCookieName="PERUNSESSION" sessionCookiePath="/">
 * </pre>
 * (see <a href="http://tomcat.apache.org/migration-7.html#Session_cookie_configuration">Session cookie configuration</a> for details)
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class ContextPathFilter implements Filter {

    /**
     * Simple HttpServletRequestWrapper that replaces the normal context path with a specified one.
     */
    static public class ContextPathRequestWrapper extends HttpServletRequestWrapper {

        private String contextPath;

        public ContextPathRequestWrapper(HttpServletRequest request, String contextPath) {
            super(request);
            this.contextPath = contextPath;
        }

        @Override
        public String getContextPath() {
            return contextPath;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String request_uri = (String) servletRequest.getAttribute("APACHE_REQUEST_URI");
        if (request_uri != null) {
            String ctxPath = request_uri.substring(0, request_uri.indexOf('/', 1));
            filterChain.doFilter(new ContextPathRequestWrapper((HttpServletRequest) servletRequest, ctxPath), servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
