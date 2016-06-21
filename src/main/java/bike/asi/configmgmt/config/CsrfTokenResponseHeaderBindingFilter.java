package bike.asi.configmgmt.config;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Taken from:  https://github.com/aditzel/spring-security-csrf-filter/blob/master/src/main/java/com/allanditzel/springframework/security/web/csrf/CsrfTokenResponseHeaderBindingFilter.java
 *
 * Binds a {@link org.springframework.security.web.csrf.CsrfToken} to the {@link HttpServletResponse}
 * headers if the Spring {@link org.springframework.security.web.csrf.CsrfFilter} has placed one in the {@link HttpServletRequest}.
 *
 * Based on the work found in: <a href="http://stackoverflow.com/questions/20862299/with-spring-security-3-2-0-release-how-can-i-get-the-csrf-token-in-a-page-that">Stack Overflow</a>
 *
 * @author Allan Ditzel
 * @since 1.0
 */
public class CsrfTokenResponseHeaderBindingFilter extends OncePerRequestFilter {

    protected static final String REQUEST_ATTRIBUTE_NAME = "_csrf";
    protected static final String RESPONSE_HEADER_NAME = "X-CSRF-HEADER";
    protected static final String RESPONSE_PARAM_NAME = "X-CSRF-PARAM";
    protected static final String RESPONSE_TOKEN_NAME = "X-CSRF-TOKEN";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, javax.servlet.FilterChain filterChain) throws ServletException, IOException {

        CsrfToken token = (CsrfToken) request.getAttribute(REQUEST_ATTRIBUTE_NAME);

        if (token != null) {
            //setHeaders(response, token);
            setCookie(response, token);
        }

        filterChain.doFilter(request, response);
    }


    private void setHeaders(HttpServletResponse response, CsrfToken token) {
            response.setHeader(RESPONSE_HEADER_NAME, token.getHeaderName());
            response.setHeader(RESPONSE_PARAM_NAME, token.getParameterName());
            response.setHeader(RESPONSE_TOKEN_NAME , token.getToken());
    }



    public static final String XSRF_TOKEN_COOKIE_NAME = "XSRF-TOKEN";

    private void setCookie(HttpServletResponse response, CsrfToken token) {
        Cookie cookie = new Cookie(XSRF_TOKEN_COOKIE_NAME, token.getToken());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
