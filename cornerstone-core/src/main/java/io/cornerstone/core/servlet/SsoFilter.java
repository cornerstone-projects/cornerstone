package io.cornerstone.core.servlet;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import io.cornerstone.core.util.RequestUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsoFilter implements Filter {

	public static final String DEFAULT_ENCODING = "UTF-8";

	public static final String COOKIE_NAME_TOKEN = "T";

	public static final String COOKIE_NAME_SESSION = "s";

	private final RestTemplate restTemplate;

	private final UserDetailsService userDetailsService;

	private final MessageSource messageSource;

	@Value("${portal.baseUrl}")
	private String portalBaseUrl;

	@Value("${portal.api.user.self.url:/api/user/@self}")
	private String portalApiUserSelfUrl;

	@Value("${portal.login.url:/login}")
	private String portalLoginUrl;

	@Value("${ssoHandler.excludePattern:}")
	protected String excludePattern;

	@Value("${ssoHandler.strictAccess:false}")
	protected boolean strictAccess;

	private AntPathMatcher antPathMatcher = new AntPathMatcher();

	public SsoFilter(RestTemplateBuilder builder, UserDetailsService userDetailsService, MessageSource messageSource) {
		this.restTemplate = builder.build();
		this.userDetailsService = userDetailsService;
		this.messageSource = messageSource;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		if (!isSameOrigin(request.getRequestURL().toString(), portalBaseUrl)) {
			if (strictAccess)
				throw new AccessDeniedException("Please access via domain name");
			chain.doFilter(req, resp);
			return;
		}
		String uri = request.getRequestURI();
		uri = uri.substring(request.getContextPath().length());
		if (!isProtected(uri)) {
			chain.doFilter(req, resp);
			return;
		}
		SecurityContext sc = SecurityContextHolder.getContext();
		Authentication auth = sc.getAuthentication();
		if (auth != null && auth.isAuthenticated()) {
			chain.doFilter(req, resp);
			return;
		}
		// check cors
		String origin = request.getHeader("Origin");
		if (origin != null) {
			if (!("Upgrade".equalsIgnoreCase(request.getHeader("Connection"))
					&& "WebSocket".equalsIgnoreCase(request.getHeader("Upgrade")))) {
				String url = request.getRequestURL().toString();
				if (!url.startsWith(origin)) {
					response.setHeader("Access-Control-Allow-Origin", origin);
					response.setHeader("Access-Control-Allow-Credentials", "true");
					String requestMethod = request.getHeader("Access-Control-Request-Method");
					String requestHeaders = request.getHeader("Access-Control-Request-Headers");
					String method = request.getMethod();
					if (method.equalsIgnoreCase("OPTIONS") && (requestMethod != null || requestHeaders != null)) {
						// preflighted request
						if (requestMethod != null)
							response.setHeader("Access-Control-Allow-Methods", requestMethod);
						if (requestHeaders != null)
							response.setHeader("Access-Control-Allow-Headers", requestHeaders);
						response.setHeader("Access-Control-Max-Age", "36000");
						return;
					}
				}
			}
		}

		// check same origin
		if (!isSameOrigin(request.getRequestURL().toString(), portalBaseUrl)) {
			chain.doFilter(request, response);
			return;
		}

		String token = getCookieValue(request, COOKIE_NAME_TOKEN);
		if (token == null) {
			redirect(request, response);
			return;
		} else {
			URI apiUri;
			try {
				apiUri = new URI(portalApiUserSelfUrl.indexOf("://") > 0 ? portalApiUserSelfUrl
						: portalBaseUrl + portalApiUserSelfUrl);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return;
			}
			StringBuilder cookie = new StringBuilder();
			cookie.append(COOKIE_NAME_TOKEN).append("=").append(URLEncoder.encode(token, DEFAULT_ENCODING));
			String session = getCookieValue(request, COOKIE_NAME_SESSION);
			if (session != null)
				cookie.append("; ").append(COOKIE_NAME_SESSION).append("=")
						.append(URLEncoder.encode(session, DEFAULT_ENCODING));
			RequestEntity<?> requestEntity = RequestEntity.get(apiUri).header("Cookie", cookie.toString())
					.header("X-Real-IP", request.getRemoteAddr()).build();
			try {
				ResponseEntity<SimpleUser> responseEntity = restTemplate.exchange(requestEntity, SimpleUser.class);
				UserDetails ud = map(responseEntity.getBody());
				auth = new UsernamePasswordAuthenticationToken(ud, ud.getPassword(), ud.getAuthorities());
				sc.setAuthentication(auth);
				request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
				MDC.put("username", auth.getName());
			} catch (HttpClientErrorException e) {
				if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
					redirect(request, response);
					return;
				}
				throw e;
			}
		}
		chain.doFilter(request, response);
	}

	protected boolean isProtected(String uri) {
		if (uri.startsWith("/assets/"))
			return false;
		for (String s : excludePattern.split("\\s*,\\s*")) {
			if (antPathMatcher.match(s, uri)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected UserDetails map(SimpleUser userFromApi) {
		if (userFromApi == null)
			throw new AccessDeniedException("user not found");
		try {
			UserDetails user = userDetailsService.loadUserByUsername(userFromApi.getUsername());
			// reset passwordModifyDate to avoid CredentialsExpiredException
			BeanWrapperImpl bw = new BeanWrapperImpl(user);
			if (bw.isWritableProperty("passwordModifyDate"))
				bw.setPropertyValue("passwordModifyDate", null);
			Collection authorities = user.getAuthorities();
			try {
				Set<String> roles = userFromApi.getRoles();
				if (!CollectionUtils.isEmpty(roles)) {
					List<GrantedAuthority> list = AuthorityUtils
							.createAuthorityList(roles.toArray(new String[roles.size()]));
					for (GrantedAuthority ga : list) {
						if (!authorities.contains(ga))
							authorities.add(ga);
					}
				}
			} catch (UnsupportedOperationException e) {
				log.warn("Can not copy roles from portal server because collection is unmodifiable");
			}
			return user;
		} catch (UsernameNotFoundException e) {
			throw new AccessDeniedException(e.getMessage());
		}
	}

	protected void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (RequestUtils.isRequestedFromApi(request)) {
			// see WebSecurityConfiguration::authenticationFailureHandler
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, messageSource.getMessage(
					"ExceptionTranslationFilter.insufficientAuthentication", null, LocaleContextHolder.getLocale()));
			return;
		}
		StringBuffer sb = request.getRequestURL();
		String queryString = request.getQueryString();
		if (queryString != null)
			sb.append("?").append(queryString);
		String targetUrl = sb.toString();
		StringBuilder redirectUrl = new StringBuilder(portalLoginUrl.indexOf("://") > 0 ? "" : portalBaseUrl);
		redirectUrl.append(portalLoginUrl).append("?targetUrl=").append(URLEncoder.encode(targetUrl, "UTF-8"));
		response.sendRedirect(redirectUrl.toString());
	}

	protected static String getCookieValue(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (Cookie cookie : cookies)
			if (cookieName.equalsIgnoreCase(cookie.getName()))
				try {
					return URLDecoder.decode(cookie.getValue(), DEFAULT_ENCODING);
				} catch (UnsupportedEncodingException e) {
					return cookie.getValue();
				}
		return null;
	}

	protected static boolean isSameOrigin(String a, String b) {
		if (b.indexOf("://") < 0 || a.indexOf("://") < 0)
			return true;
		try {
			String host1 = new URL(a).getHost();
			if (host1 == null)
				host1 = "localhost";
			String host2 = new URL(b).getHost();
			if (host2 == null)
				host2 = "localhost";
			if (host1.equalsIgnoreCase(host2))
				return true;
			String[] arr1 = host1.split("\\.");
			String[] arr2 = host2.split("\\.");
			return arr1.length >= 2 && arr2.length >= 2 && arr1[arr1.length - 1].equals(arr2[arr2.length - 1])
					&& arr1[arr1.length - 2].equals(arr2[arr2.length - 2]);
		} catch (MalformedURLException e) {
			return false;
		}
	}

	static class SimpleUser implements Serializable {

		private static final long serialVersionUID = 2064378429236105592L;

		private String username;

		private String name;

		private Set<String> roles = new LinkedHashSet<>(0);

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Set<String> getRoles() {
			return roles;
		}

		public void setRoles(Set<String> roles) {
			this.roles = roles;
		}

	}

}
