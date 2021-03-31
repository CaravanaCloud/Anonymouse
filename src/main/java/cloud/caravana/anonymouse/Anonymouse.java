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
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import static java.lang.String.*;

@Component
public class Anonymouse {
    @Autowired
    private Logger log;

    @Autowired
    JDBCIterator dbIterator;

    @Autowired
    private ExplicitClassifier cx;

    public boolean isPIISafe(String value, String... context) {
        return cx.isPIISafe(value,context);
    }

    public PIIClass classify(String value, String tbl, String col) {
        return cx.classify(value, tbl, col);
    }

    public void run() {
        dbIterator.run();
    }

    public void addConfig(String url) {
        cx.addConfig(url);
    }
}
