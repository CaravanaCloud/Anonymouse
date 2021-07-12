package cloud.caravana.anonymouse.iter;

import static java.lang.String.format;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static java.sql.Types.DATE;
import static java.sql.Types.VARCHAR;

import cloud.caravana.anonymouse.Classification;
import cloud.caravana.anonymouse.Configuration;
import cloud.caravana.anonymouse.PIIClass;
import cloud.caravana.anonymouse.Table;
import cloud.caravana.anonymouse.classifier.Classifier;
import cloud.caravana.anonymouse.classifier.Classifiers;
import cloud.caravana.anonymouse.report.Report;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.sql.DataSource;

@SuppressWarnings("CdiUnproxyableBeanTypesInspection")
@ApplicationScoped
public class JDBCIterator implements Runnable {

    @Inject
    Configuration cfg;

    @Inject
    Logger log;

    @Inject
    Instance<DataSource> dsProvider;

    @Inject
    Classifiers classifiers;

    @Inject
    ExecutorService executor;

    private Report report;

    public void runTable(Table table) {
        var rowCnt = 0L;
        var startTime = LocalDateTime.now();
        var success = true;
        var errorMsg = "";
        try (var conn = getConnection()) {
            conn.setAutoCommit(true);
            var tableCat = table.tableCat();
            var tableName = table.tableName();
            var tableFQN = "%s.%s".formatted(tableCat, tableName);
            log.info("Anonymizing [%s]".formatted(table.toString()));
            var sql = "SELECT * FROM " + tableFQN;
            log.info(sql);
            try (var statement = conn
                    .createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);) {
                var rows = statement.executeQuery(sql);
                int concurrency = rows.getConcurrency();
                if (concurrency != ResultSet.CONCUR_UPDATABLE) {
                    log.warning("Skipping not updatable table %s".formatted(table));
                    return;
                }
                var rowsMD = rows.getMetaData();
                var colCnt = rowsMD.getColumnCount();
                while (rows.next()) {
                    rowCnt++;
                    if (rowCnt % cfg.getLogEach() == 0) {
                        log.info("Row [%s] Table [%s] ".formatted(rowCnt, table));
                    }
                    for (var col = 1; col <= colCnt; col++) {
                        int columnType = rowsMD.getColumnType(col);
                        var columnName = rowsMD.getColumnName(col);
                        try {
                            visitCell(rowCnt, tableName, rows, col, columnType, columnName);
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.warning("Fail to visit cell ");
                        }
                    }
                    if (!cfg.isDryRun()) {
                        try {
                            rows.updateRow();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            log.warning("Fail to update row  ");
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            success = false;
            errorMsg = ex.getMessage();
            ex.printStackTrace();
            log.warning("Failed to anonymize %s".formatted(table));
            log.throwing("JDBCIterator", "runTable", ex);
        }
        var endTime = LocalDateTime.now();
        report.jdbcResult().tableResult(
            table,
            success,
            errorMsg,
            rowCnt,
            startTime,
            endTime
        );
        log.info("Anonymized [%s]rows of [%s]".formatted(rowCnt, table.toString()));
    }

    private Connection tryInjected(){
        try {
            if (cfg.isJDBCReady() && dsProvider.isResolvable()) {
                var ds = dsProvider.get();
                var conn = ds.getConnection();
                return conn;
            }
        }catch (SQLException e){
            return null;
        }
        return null;
    }


    private Connection getConnection() throws SQLException {
        Connection conn = tryInjected();
        if (conn != null) {
            return conn;
        } else {
            log.warning("Can't resolve database to connect.");
            throw new IllegalStateException("Can't resolve datasource to connect.");
        }
    }

    private void visitCell(Long rowCnt, String tableName, ResultSet rows, int col, int columnType, String columnName) {
        try {
            switch (columnType) {
                case VARCHAR -> visitString(tableName, columnName, rowCnt, rows);
                case DATE -> visitDate(tableName, columnName, rowCnt, rows);
                default -> log.finest("Cant handle type for column %s.%s".formatted(
                        tableName,
                        columnName));
            }
        } catch (SQLException ex) {
            log.warning("Failed to update [" + tableName + "].[" + columnName + "] ");
            log.throwing("JDBIterator", "updateString", ex);
        }
    }

    private void visitDate(String tableName,
                           String columnName,
                           Long row,
                           ResultSet rows) throws SQLException {
        var value = rows.getDate(columnName);
        if (value != null) {
            var classification = classifiers.classify(value, tableName, columnName)
                    .map(Classification::classifier);
            if (classification.isPresent()) {
                Classifier<Date> classifier = classification.get();
                var utilDate = classifier.generateDate(value, row, columnName);
                var sqlDate = new Date(utilDate.getTime());
                rows.updateDate(columnName, sqlDate);
            }
        }
    }

    private void visitString(String tableName,
                             String columnName,
                             Long row,
                             ResultSet rows) throws SQLException {
        var columnValue = rows.getString(columnName);
        if (columnValue != null) {
            var classification = classifiers.classify(columnValue, tableName, columnName)
                    .map(Classification::classifier);
            if (classification.isPresent()) {
                Classifier<String> classifier = classification.get();
                var newValue = classifier.generateString(columnValue, row, columnName);
                if (!columnValue.equals(newValue)) {
                    rows.updateString(columnName, newValue);
                }
            }
        }
    }

    public void runTables() throws Exception {
        try (var conn = getConnection()) {
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, null, null, new String[] {"TABLE"});
            while (rs.next()) {
                var table = Table.of(rs);
                if (isTruncate(table)) {
                    truncate(table);
                } else if (!isSkippedTable(table)) {
                    runTable(table);
                } else {
                    log.fine("Skipping %s".formatted(table));
                }
            }
        } finally {
            log.exiting("Anonymouse", "runTables");
        }
    }

    private void truncate(Table table) {
        if (!cfg.isDryRun()) {
            var sql = "TRUNCATE TABLE %s".formatted(table.tableName());
            try (var conn = getConnection();
                 var stmt = conn.createStatement()) {
                var upCount = stmt.executeUpdate(sql);
                log.info("Truncated [%d] %s".formatted(upCount, table));
            } catch (SQLException ex) {
                log.warning("Failed to truncate %s".formatted(table));
            }
        }
    }

    private boolean isTruncate(Table table) {
        String tableName = table.tableName();
        boolean truncate = cfg.isDeclared(tableName, PIIClass.Truncate);
        return truncate;
    }

    private boolean isSkippedTable(Table table)
            throws SQLException {
        if ("SYSTEM TABLE".equalsIgnoreCase(table.tableType())) return true;
        if ("INFORMATION_SCHEMA".equalsIgnoreCase(table.tableSchem())) return true;
        if ("INFORMATION_SCHEMA".equalsIgnoreCase(table.tableCat())) return true;
        if ("SYS".equalsIgnoreCase(table.tableCat())) return true;
        String tableName = table.tableName();
        if ("FLYWAY_SCHEMA_HISTORY".equalsIgnoreCase(tableName)) return true;
        boolean skip = cfg.isDeclared(tableName, PIIClass.Safe);
        log.info(format("Skip [%s] Table [%s]", skip, table));
        return skip;
    }

    @Override
    public void run() {
        var t0 = System.currentTimeMillis();
        try {
            if (ping()) {
                runTables();
                var t1 = System.currentTimeMillis();
                var t = (t1 - t0) / 1000.0D;
                log.info("JDBC iteration took [%1$,.4f]s".formatted(t));
            } else {
                log.warning("Could not connect to JDBC Datasource");
            }
        } catch (Exception e) {
            log.warning("Failed to anonymize JDBC");
            log.throwing("Anonymouse", "run", e);
        } finally {
            log.fine("JDBC Iterator finished");
        }
    }

    private boolean ping() {
        try (var conn = getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1+1");
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public void setReport(Report report) {
        this.report = report;
    }
}
