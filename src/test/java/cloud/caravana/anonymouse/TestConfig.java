package cloud.caravana.anonymouse;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.quarkus.test.Mock;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Qualifier;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

@Mock
public class TestConfig implements Configuration{

    @Produces
    public DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getTestJDBCUrl());
        config.setUsername(getTestJDBCUsername());
        config.setPassword(getTestJDBCPassword());
        return new HikariDataSource(config);
    }

    String getTestJDBCPassword() {
        return "";
    }


    String getTestJDBCUsername() {
        return "sa";
    }


    String getTestJDBCUrl() {
        return "jdbc:h2:mem:test";
    }

    public Flyway getFlyway(String... locations) {
        Flyway flyway = Flyway.configure()
                              .dataSource(getDataSource())
                              .locations(locations)
                              .load();
        return flyway;
    }

    public Flyway migrate(String... ls) {
        Flyway flyway = getFlyway(ls);
        flyway.clean();
        flyway.migrate();
        return flyway;
    }
}