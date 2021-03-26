package cloud.caravana.anonymouse;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DBConfig {
    
    @Bean
    @Primary
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(getJDBCUrl());
        dataSourceBuilder.username(getJDBCUsername());
        dataSourceBuilder.password(getJDBCPassword());
        return dataSourceBuilder.build();
    }

    private String getJDBCPassword() {
        return getEnv("JDBC_PASSWORD", "");
    }

    private String getJDBCUsername() {
        return getEnv("JDBC_USERNAME", "sa");
    }

    private String getJDBCUrl() {
        return getEnv("JDBC_URL","jdbc:h2:mem:test");
    }

    private String getEnv(String varName, String defVal) {
        return System.getenv().getOrDefault(varName,defVal);
    }
}