package bike.asi.configmgmt.config;

import java.util.Locale;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * The "ImprovedNamingStrategy" for converting Java Entity names into Database Table names
 * (e.g. MyTable -> my_table) has been deprecated with Hibernate 5.0. Instead, the naming
 * strategy has been split into a "physical" naming strategy and an "implicit" naming strategy.
 * See: https://hibernate.atlassian.net/browse/HHH-9417
 *
 * <pre>
 *   The logical name is the name used to register tables/columns for lookup.
 *   The implicit name is the name determined when one is not explicitly specified.
 *   The physical name is the name we ultimately use with the database.
 *   Implicit and physical naming should be controllable via pluggable "naming strategies".
 *   Logical name probably should not be pluggable as it is an internal implementation detail
 *   (more or less a Map key) and things will break down if this is not done properly and consistently on both sides.
 * </pre>
 *
 * This class attempts to create a physical naming strategy with behaviour similar to the legacy
 * ImprovedNamingStrategy until something better comes along.
 *
 * See:
 * https://github.com/spring-projects/spring-boot/issues/2763
 * http://stackoverflow.com/questions/32437202/hibernate-naming-strategy-not-working
 *
 * @author gheinze
 */
public class HibernateLegacyImprovedNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    @Override
    public Identifier toPhysicalTableName(Identifier idntfr, JdbcEnvironment je) {
        return new Identifier(addUnderscores(idntfr.getText()), idntfr.isQuoted());
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier idntfr, JdbcEnvironment je) {
        return new Identifier(addUnderscores(idntfr.getText()), idntfr.isQuoted());
    }


    private static String addUnderscores(String name) {
        final StringBuilder buf = new StringBuilder(name.replace('.', '_'));
        for (int i = 1; i < buf.length() - 1; i++) {
            if (Character.isLowerCase(buf.charAt(i - 1))
                    && Character.isUpperCase(buf.charAt(i))
                    && Character.isLowerCase(buf.charAt(i + 1))) {
                buf.insert(i++, '_');
            }
        }
        return buf.toString().toLowerCase(Locale.ROOT);
    }

}
