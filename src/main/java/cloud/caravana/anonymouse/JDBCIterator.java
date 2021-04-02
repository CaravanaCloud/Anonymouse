package cloud.caravana.anonymouse;

import static java.lang.String.format;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;

import cloud.caravana.anonymouse.classifier.Classifier;
import cloud.caravana.anonymouse.classifier.CompositeClassifier;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

@ApplicationScoped
public class JDBCIterator {
    @Inject
    Logger log;

    @Inject
    DataSource ds;

    @Inject
    CompositeClassifier cx;

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
                              ResultSet rows, int row, int col) throws SQLException{
            var columnName = rowsMD.getColumnName(col);
            var columnValue = rows.getString(col);
            var piiClassO = cx.classify(columnValue, tableName, columnName);
            var piiClfnO = piiClassO.stream();
            piiClfnO.forEach(piiClfn -> {
                Classifier piiCx = piiClfn.classifier();
                String newValue = piiCx.generateString(columnName, row);
                log.finer("Updating string [" + tableName + "].[" + columnName + "] := [" + newValue + "]");
                try {
                    rows.updateString(columnName, newValue);
                } catch (SQLException ex) {
                    log.warning("Failed to update [" + tableName + "].[" + col + "] ");
                    log.throwing("JDBIterator","updateString",ex);
                }
            });
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
        try {
            ping();
            runTables();
        } catch (Exception e) {
            log.info("Failed to anonymize");
            log.throwing("Anonymouse","run",e);
            throw new RuntimeException(e);
        } finally {
            log.fine("Database anonymized");
        }
    }

    private void ping() throws SQLException {
        try(var conn = ds.getConnection();
            var stmt = conn.createStatement()){
            stmt.executeQuery("SELECT 1+1");
        }
    }
}
