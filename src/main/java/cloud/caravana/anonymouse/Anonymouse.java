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
    private DataSource ds;

    @Autowired
    private ExplicitClassifier cx;

    public void runTable(String tableName) {
        try (Connection conn = ds.getConnection()) {
            var sql = "SELECT * FROM " + tableName;
            var statement = conn.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
            var rows = statement.executeQuery(sql);
            var rowsMD = rows.getMetaData();
            var colCnt = rowsMD.getColumnCount();
            var rowCnt = 0;
            while (rows.next()) {
                rowCnt++;
                for (var col = 1; col <= colCnt; col++) {
                    int columnType = rowsMD.getColumnType(col);
                    if (isStringType(columnType)) {
                        updateString(tableName, rowsMD, rows, rowCnt, col);
                    }
                }
                rows.updateRow();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            log.exiting("Anonymouse", "runTable");
        }
    }

    private void updateString(String tableName, ResultSetMetaData rowsMD,
                              ResultSet rows, int row, int col) throws SQLException {
        String columnName = rowsMD.getColumnName(col);
        String columnValue = rows.getString(col);
        PIIClass piiclass = cx.classify(columnValue, tableName, columnName);
        if (!PIIClass.Safe.equals(piiclass)) {
            String newValue = cx.generateString(row, columnName);
            log.finer("Anonymoused [" + tableName + "].[" + columnName + "] := [" + newValue + "]");
            rows.updateString(columnName, newValue);
        }
    }

    public static boolean isStringType(int columnType) {
        return columnType == Types.VARCHAR;
    }

    public void runTables() throws Exception {
        try (Connection conn = ds.getConnection()) {
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, null, null, null);
            while (rs.next()) {
                if (!isSkippedTable(rs)) {
                    String tableName = rs.getString("TABLE_NAME");
                    runTable(tableName);
                }
            }
        } finally {
            log.exiting("Anonymouse", "runTables");
        }
    }

    private boolean isSkippedTable(ResultSet rs) throws SQLException {
        var table = new Table(
            rs.getString("TABLE_CAT"),
            rs.getString("TABLE_SCHEM"),
            rs.getString("TABLE_NAME"),
            rs.getString("TABLE_TYPE"),
            rs.getString("TYPE_CAT"),
            rs.getString("TYPE_SCHEM"),
            rs.getString("TYPE_NAME"),
            rs.getString("REMARKS"),
            rs.getString("SELF_REFERENCING_COL_NAME"),
            rs.getString("REF_GENERATION")
        );

        boolean skip = "INFORMATION_SCHEMA".equalsIgnoreCase(table.tableSchem());
        skip |= "FLYWAY_SCHEMA_HISTORY".equalsIgnoreCase(table.tableName());
        log.info(format("Skip [%s] Table [%s]", skip, table));
        return skip;
    }

    public void run() {
        try (Connection conn = ds.getConnection()) {
            runTables();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            log.fine("Database anonymized");
        }
    }


    public void addConfig(String URL) {
        if (URL.startsWith("classpath:")) {
            var resource = URL.split(":")[1];
            try (
                var istream = this.getClass()
                                  .getResourceAsStream(resource)
            ) {
                if (istream != null) {
                    var yaml = new Yaml();
                    var cfgMap = (Map<String, Object>) yaml.load(istream);
                    cfgMap.forEach((key,value) -> addRoot(key,value));
                } else {
                    log.warning("Failed to load config [%s]".formatted(URL));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void addRoot(String key, Object value) {
        if (value instanceof Map){
            Map<String,Object> map = (Map<String, Object>) value;
            map.forEach((ckey,cvalue) -> addChild(key,ckey,cvalue.toString()));
            System.out.println(map.getClass());
        }
    }

    private void addChild(String key, String ckey, String cvalue) {
        PIIClass piiClass = PIIClass.valueOf(cvalue);
        String cname = key + "." + ckey;
        cx.setPIIClass(piiClass,cname);
    }

    public boolean isPIISafe(String value, String... context) {
        return cx.isPIISafe(value,context);
    }

    public PIIClass classify(String value, String tbl, String col) {
        return cx.classify(value, tbl, col);
    }
}
