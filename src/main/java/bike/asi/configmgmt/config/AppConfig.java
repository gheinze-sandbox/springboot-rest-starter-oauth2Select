package bike.asi.configmgmt.config;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gheinze
 */
@Configuration
public class AppConfig {

    /**
     * Extract the db connect information exposed by the environment variable DATABASE_URL and create a DataSource
     * object. In particular, Heroku exposes the environment variable DATABASE_URL in the form of:
     *
     *      postgres://\<username\>:\<password\>@\<host\>/\<dbname\>
     *
     * Note: postgres, not postgresql.
     * Note also: remote connections to Heroku require ssl support, which adds the following URI query:
     *     ?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory
     *
     * @return
     * @throws java.net.URISyntaxException
     */
    @Bean
    public BasicDataSource dataSource() throws URISyntaxException {

        URI dbUri = new URI(System.getenv("DATABASE_URL"));
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];

        StringBuilder dbUrl = new StringBuilder(128);
        dbUrl.append("jdbc:postgresql://")
                .append(dbUri.getHost()).append(":")
                .append(dbUri.getPort())
                .append(dbUri.getPath());

        String query = dbUri.getQuery();
        if (null != query && !query.isEmpty()) {
            dbUrl.append("?").append(query);
        }

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(dbUrl.toString());
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);

        return basicDataSource;
    }



}
