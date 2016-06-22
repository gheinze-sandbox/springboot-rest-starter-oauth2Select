package bike.asi.configmgmt.config;

import bike.asi.configmgmt.Application;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * Override Spring Boots default usage of spring.jpa.* properties from the application.properties
 * file in order to wire in multi-tenancy support. Once this is used, it seems no spring.jpa properties
 * will be picked up.
 *
 * Currently tests run using embedded hsql, so we do not wish to use this configuration for testing.
 * The embedded db allows the tests to run without any database dependencies. Most services should
 * still be testable this way.  When there is a divergence for Postgresql or a need to test multi-
 * tenancy, then the environment variable DATABASE_URL will need to be configured to point to a test
 * Postgresql database service and the @Profile("!test") annotation can be removed.
 *
 * @author gheinze
 */
@Profile("!test")
@Configuration
public class HibernateConfig {

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }


    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(dataSource);
        emfBean.setPackagesToScan(Application.class.getPackage().getName());
        emfBean.setJpaVendorAdapter(jpaVendorAdapter());

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        // Improved naming strategy deprecated as of hibernate 5.0
        // jpaProperties.put("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
        jpaProperties.put("hibernate.implicit_naming_strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyHbmImpl");
        jpaProperties.put("hibernate.physical_naming_strategy", "bike.asi.configmgmt.config.HibernateLegacyImprovedNamingStrategy");

        jpaProperties.put("hibernate.show_sql", "true");
        jpaProperties.put("hibernate.format_sql", "true");
        emfBean.setJpaPropertyMap(jpaProperties);
        return emfBean;
    }

}
