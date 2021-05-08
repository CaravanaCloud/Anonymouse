package cloud.caravana.anonymouse;

import io.quarkus.test.Mock;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

@ApplicationScoped
public class Migrations {
    @Inject
    DataSource defaultDS;

    public Flyway getFlyway(String... locations) {
        return Flyway.configure()
                     .dataSource(defaultDS)
                     .locations(locations)
                     .load();
    }

    public void migrate(String... ls) {
        Flyway flyway = getFlyway(ls);
        flyway.clean();
        flyway.migrate();
    }
}
