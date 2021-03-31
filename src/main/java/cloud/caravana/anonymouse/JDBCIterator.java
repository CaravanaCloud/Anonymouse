package cloud.caravana.anonymouse;

import static java.lang.String.format;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;

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
    ExplicitClassifier cx;


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
}
