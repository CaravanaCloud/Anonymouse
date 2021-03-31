package cloud.caravana.anonymouse;

import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.yaml.snakeyaml.Yaml;

import static java.lang.String.*;

@ApplicationScoped
public class Anonymouse {
    @Inject
    Logger log;

    @Inject
    JDBCIterator dbIterator;

    @Inject
    ExplicitClassifier cx;

    public boolean isPIISafe(String value, String... context) {
        return cx.isPIISafe(value,context);
    }

    public PIIClass classify(String value, String tbl, String col) {
        return cx.classify(value, tbl, col);
    }

    public void run() {
        dbIterator.run();
    }
}
