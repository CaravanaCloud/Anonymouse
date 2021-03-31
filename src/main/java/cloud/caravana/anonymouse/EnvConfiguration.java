package cloud.caravana.anonymouse;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class EnvConfiguration extends BaseConfiguration{
    public DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getJDBCUrl());
        config.setUsername(getJDBCUsername());
        config.setPassword(getJDBCPassword());
        return new HikariDataSource(config);
    }

    String getJDBCPassword() {
        return getEnv("JDBC_PASSWORD", "");
    }

    String getJDBCUsername() {
        return getEnv("JDBC_USERNAME", "sa");
    }

    String getJDBCUrl() {
        return getEnv("JDBC_URL", "jdbc:h2:mem:test");
    }

    private static String getEnv(String varName, String defVal) {
        return System.getenv()
                     .getOrDefault(varName, defVal);
    }
}
