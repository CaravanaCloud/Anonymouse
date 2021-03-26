package cloud.caravana.anonymouse;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class TestDBConfig {
    public static Flyway getFlyway(String... locations) {
        Flyway flyway = Flyway.configure()
                              .dataSource(getTestDataSource())
                              .locations(locations)
                              .load();
        return flyway;
    }

    @Bean
    @Qualifier("test")
    public static DataSource getTestDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(getJDBCUrl());
        dataSourceBuilder.username(getJDBCUsername());
        dataSourceBuilder.password(getJDBCPassword());
        return dataSourceBuilder.build();
    }

    private static  String getJDBCPassword() {
        return "";
    }

    private static  String getJDBCUsername() {
        return "sa";
    }

    private static  String getJDBCUrl() {
        return "jdbc:h2:mem:test";
    }

    public Flyway migrate(String... ls) {
        Flyway flyway = getFlyway(ls);
        flyway.clean();
        flyway.migrate();
        return flyway;
    }
}