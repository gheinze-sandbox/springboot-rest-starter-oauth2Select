package bike.asi.configmgmt.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.filter.CompositeFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.util.WebUtils;

/**
 *
 * @author gheinze
 */
@EnableWebSecurity
@EnableOAuth2Client
@Configuration
public class HttpSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired private OAuth2ClientContext oauth2ClientContext;

    @Value("${google.drive.callBackPort}") private int googleDriveCallbackPort;


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.antMatcher("/**")
                .authorizeRequests()
                .antMatchers("/", "/index.html", "/js/**", "/img/**", "/login**").permitAll()
                .anyRequest().authenticated()
                .and().logout().logoutSuccessUrl("/").permitAll()
                .and().csrf().csrfTokenRepository(csrfTokenRepository())
                .and().addFilterAfter(csrfHeaderFilter(), CsrfFilter.class)
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);

    }



    // SSO Filters

    class ClientResources {

        private OAuth2ProtectedResourceDetails client = new AuthorizationCodeResourceDetails();
        private ResourceServerProperties resource = new ResourceServerProperties();

        public OAuth2ProtectedResourceDetails getClient() {
            return client;
        }

        public ResourceServerProperties getResource() {
            return resource;
        }

    }


    @Bean
    @ConfigurationProperties("oauth2.google")
    ClientResources google() {
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("oauth2.facebook")
    ClientResources facebook() {
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("oauth2.github")
    ClientResources github() {
        return new ClientResources();
    }


    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }


    private Filter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(ssoFilter(google(), "/login/google"));
        filters.add(ssoFilter(facebook(), "/login/facebook"));
        filters.add(ssoFilter(github(), "/login/github"));
        filter.setFilters(filters);
        return filter;
    }


    private Filter ssoFilter(ClientResources client, String path) {

        OAuth2ClientAuthenticationProcessingFilter oAuth2ClientAuthenticationFilter
                = new OAuth2ClientAuthenticationProcessingFilter(path);

        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);

        oAuth2ClientAuthenticationFilter.setRestTemplate(oAuth2RestTemplate);

        UserInfoTokenServices tokenServices = new UserInfoTokenServices(
                client.getResource().getUserInfoUri(), client.getClient().getClientId());

        tokenServices.setRestTemplate(oAuth2RestTemplate);

        oAuth2ClientAuthenticationFilter.setTokenServices(tokenServices);

        return oAuth2ClientAuthenticationFilter;
    }





    // CSRF

    private static Filter csrfHeaderFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {
                CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                if (csrf != null) {
                    Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
                    String token = csrf.getToken();
                    if (cookie == null || token != null && !token.equals(cookie.getValue())) {
                        cookie = new Cookie("XSRF-TOKEN", token);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    private static CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }


    // File Upload

    public class SecurityApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
        @Override
        protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
            insertFilters(servletContext, new MultipartFilter());
        }
    }


    @Bean
    public Drive googleDrive() throws GeneralSecurityException, IOException {

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                google().getClient().getClientId(),
                google().getClient().getClientSecret(),
                Collections.singleton(DriveScopes.DRIVE_FILE)
        )
                .setAccessType("online")
                .setApprovalPrompt("auto")
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(System.getProperty("user.home"), ".store/drive_sample")))
                .build()
                ;

        LocalServerReceiver localServerReceiver = new LocalServerReceiver.Builder()
                .setPort(googleDriveCallbackPort)
                .build()
                ;

        Credential credential = new AuthorizationCodeInstalledApp(flow, localServerReceiver).authorize("102687441626965982711"); // client id for user?

        return new Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("ASI Configuration Management")
                .build();

    }


}
