package cz.metacentrum.perun.rpc.csrf;

/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.metacentrum.perun.core.api.BeansUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

/**
 * This is an implementation of CSRF protection based on Spring framework solution.
 *
 * We create Cookie with CsrfToken, which is valid during whole HTTP session.
 * Client JS app or CLI can read the Cookie value and provide it back within HTTP Header.
 * CsrfToken value is also stored within HTTP session.
 *
 * On each request, where Cookie is not present, Token is generated and set to the Session.
 * On any state changing request, CsrfToken value must match between Cookie, Header and Session.
 * Check for "GET", "HEAD", "TRACE", "OPTIONS" HTTP methods is disabled.
 *
 * @see CsrfToken
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public final class CsrfFilter implements Filter {

	private final Logger log = LoggerFactory.getLogger(CsrfFilter.class);
	private final HashSet<String> allowedMethods = new HashSet<>(Arrays.asList("GET", "HEAD", "TRACE", "OPTIONS"));

	private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
	private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
	private static final String CSRF_REQUEST_ATTR_NAME = CsrfToken.class.getName();

	public CsrfFilter() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

		if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {

			log.trace("Processing CSRF filter.");

			HttpServletRequest request = (HttpServletRequest)servletRequest;
			HttpServletResponse response = (HttpServletResponse)servletResponse;

			// get all instances of tokens
			CsrfToken httpSessionToken = (CsrfToken)request.getSession(true).getAttribute(CSRF_REQUEST_ATTR_NAME);
			CsrfToken cookieCsrfToken = loadToken(request);
			String actualToken = request.getHeader(CSRF_HEADER_NAME);

			final boolean missingToken = (cookieCsrfToken == null || httpSessionToken == null);
			if (missingToken) {
				log.debug("Missing CSRF token - generating new.");
				cookieCsrfToken = generateToken();
				saveToken(cookieCsrfToken, request, response);
			}

			// Skip this filter for unprotected methods GET | HEAD | TRACE | OPTIONS
			if (this.allowedMethods.contains(request.getMethod())) {
				log.trace("Skip CSRF check on GET | HEAD | TRACE | OPTIONS method.");
				filterChain.doFilter(request, response);
				return;
			}
			// Is CSRF protection enabled ?
			if (!BeansUtils.getCoreConfig().isCsrfEnabled()) {
				filterChain.doFilter(servletRequest, servletResponse);
				return;
			}

			log.trace("Perform CSRF check.");

			// check if sent Cookie and Header are the same
			if (!Objects.equals(cookieCsrfToken.getValue(), actualToken)) {

				log.trace("Invalid CSRF token found for {} COOKIE: {} | HEADER: {}.", request.getRequestURI(), cookieCsrfToken.getValue(), actualToken);

				if (missingToken) {
					// missing token
					response.setStatus(HttpStatus.SC_FORBIDDEN);
				} else {
					// invalid token
					response.setStatus(HttpStatus.SC_FORBIDDEN);
				}
				return;
			}

			// since caller might forge both Cookie and Header
			// check if sent Header and stored HttpSession token are the same
			httpSessionToken = (CsrfToken)request.getSession(true).getAttribute(CSRF_REQUEST_ATTR_NAME);
			if (!Objects.equals(httpSessionToken.getValue(), actualToken)) {

				log.trace("Invalid CSRF token found for {} SESSION: {} | HEADER: {}.", request.getRequestURI(), httpSessionToken.getValue(), actualToken);

				if (missingToken) {
					// missing token
					response.setStatus(HttpStatus.SC_FORBIDDEN);
				} else {
					// invalid token
					response.setStatus(HttpStatus.SC_FORBIDDEN);
				}
				return;
			}

			log.trace("Tokens match SESSION: {} | COOKIE: {} | HEADER: {}.", httpSessionToken.getValue(), cookieCsrfToken.getValue(), actualToken);

			// continue to the next filter
			filterChain.doFilter(request, response);

		} else {

			log.trace("Processing CSRF filter skipped.");

			// this filter was not applied - continue to the next filter
			filterChain.doFilter(servletRequest, servletResponse);

		}

	}

	@Override
	public void destroy() {

	}

	/**
	 * Load CsrfToken from Cookie associated with HttpServletRequest.
	 * If cookie is not present or is empty, NULL is returned.
	 *
	 * @param request HttpServletRequest to read Cookie from
	 * @return CsrfToken from Cookie
	 */
	private CsrfToken loadToken(HttpServletRequest request) {

		// if cookie was not sent in request
		Cookie cookie = WebUtils.getCookie(request, CSRF_COOKIE_NAME);
		if (cookie == null) {
			return null;
		}

		// if cookie is empty
		String tokenValue = cookie.getValue();
		if (!StringUtils.hasLength(tokenValue)) {
			return null;
		}

		// return cookie version of the token
		return new CsrfToken(tokenValue);

	}

	/**
	 * Generate new CsrfToken
	 *
	 * @return new CsrfToken
	 */
	private CsrfToken generateToken() {
		return new CsrfToken();
	}

	/**
	 * Save generated CsrfToken as a Cookie in HttpServletResponse and original object in HttpServletRequest session.
	 *
	 * @param token CsrfToken
	 * @param request Request
	 * @param response Response
	 */
	private void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
		String tokenValue = token == null ? "" : token.getValue();
		Cookie cookie = new Cookie(CSRF_COOKIE_NAME, tokenValue);
		cookie.setSecure(request.isSecure());
		cookie.setPath("/");
		if (token == null) {
			cookie.setMaxAge(0);
		} else {
			cookie.setMaxAge(-1);
		}
		// allow our GUI (JS apps to read Cookie content)
		cookie.setHttpOnly(false);
		// save cookie and token
		response.addCookie(cookie);
		request.getSession(true).setAttribute(CSRF_REQUEST_ATTR_NAME, token);
	}

}

