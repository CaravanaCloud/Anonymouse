package cloud.caravana.anonymouse.iter;

import static java.lang.String.format;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static java.sql.Types.*;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import cloud.caravana.anonymouse.Classification;
import cloud.caravana.anonymouse.Configuration;
import cloud.caravana.anonymouse.PIIClass;
import cloud.caravana.anonymouse.Table;
import cloud.caravana.anonymouse.classifier.Classifier;
import cloud.caravana.anonymouse.classifier.Classifiers;
import cloud.caravana.anonymouse.report.Report;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.Transactional;

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
            var sql = writeSelectFrom(table);
            try (var statement = conn
                    .createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);) {
                if(cfg.getMaxRows() != null){
                    statement.setMaxRows(cfg.getMaxRows());
                }
                log.info("Executing query [%s]".formatted(sql));

                try(var rows = statement.executeQuery(sql)) {
                    int concurrency = rows.getConcurrency();
                    if (concurrency != ResultSet.CONCUR_UPDATABLE) {
                        log.warning("Skipping not updatable table %s".formatted(table));
                        return;
                    }
                    var rowsMD = rows.getMetaData();
                    var colCnt = rowsMD.getColumnCount();
                    while (rows.next()) {
                        rowCnt++;
                        if (visitRow(conn, table, rowCnt, rows, rowsMD, colCnt)) break;
                    }
                }catch (SQLException ex){
                    log.warning("Failed to iterate %s".formatted(table));
                }
            }
        } catch (SQLException ex) {
            success = false;
            errorMsg = ex.getMessage();
            ex.printStackTrace();
            log.warning("Failed to connect %s".formatted(table));
            log.throwing("JDBCIterator", "runTable", ex);
        }
        var endTime = LocalDateTime.now();
        report.jdbcResult().tableResult(
            table,
            success, false,
            errorMsg,
            rowCnt,
            startTime,
            endTime
        );
        log.info("Anonymized [%s]rows of [%s]".formatted(rowCnt, table.toString()));
    }

    private String writeSelectFrom(Table table) {
        var dbKind = cfg.getDbKind();
        var sql = (String) null;
        var tableName = table.tableName();
        if ("h2".equals(dbKind)){
            sql = "SELECT * FROM " + tableName;
        }else{
            var tableCat = table.tableCat();
            var tableFQN = "%s.%s".formatted(tableCat, tableName);
            sql = "SELECT * FROM " + tableFQN;
        }
        return sql;
    }

    private boolean visitRow(Connection conn, Table table,
                             long rowCnt,
                             ResultSet rows,
                             ResultSetMetaData rowsMD,
                             int colCnt) throws SQLException {
        if (cfg.isAboveMaxRows(rowCnt)){
            log.info("Reached max rows [%d] Table [%s]".formatted(rowCnt, table));
            return true;
        }
        if (rowCnt % cfg.getLogEach() == 0) {
            log.info("Row [%s] Table [%s]- ".formatted(rowCnt, table));
        }
        for (var col = 1; col <= colCnt; col++) {
            int columnType = rowsMD.getColumnType(col);
            var columnName = rowsMD.getColumnName(col);
            try {
                visitCell(rowCnt, table.tableName(), rows, col, columnType, columnName);
            } catch (Exception e) {
                e.printStackTrace();
                log.warning("Fail to visit cell ");
            }
        }
        if (!cfg.isDryRun()) {
            try {
                rows.updateRow();
            } catch (SQLException ex) {
                log.warning("Failed to update row [%d] of [%s]: %s".formatted(rowCnt, table.toString(),ex.getMessage()));
            }
        }
        return false;
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
            conn.setAutoCommit(true);
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
                case TIMESTAMP, DATE -> visitDate(tableName, columnName, rowCnt, rows);
                default -> log.finest("Cant handle type for column %s.%s [%d]".formatted(
                        tableName,
                        columnName,
                        columnType));
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
            var classificationOpt = classifiers
                    .classify(value, tableName, columnName);
            if (classificationOpt.isPresent()) {
                var classification = classificationOpt.get();
                if (! classification.isSafe()){
                    Classifier<Date> classifier = classification.classifier();
                    var utilDate = classifier.generateDate(value, row, columnName);
                    var sqlDate = new Date(utilDate.getTime());
                    rows.updateDate(columnName, sqlDate);
                }
            }
        }
    }

    private void visitString(String tableName,
                             String columnName,
                             Long row,
                             ResultSet rows) throws SQLException {
        var columnValue = rows.getString(columnName);
        if (columnValue != null) {
            var classificationOpt = classifiers.classify(columnValue, tableName, columnName);
            if (classificationOpt.isPresent()) {
                Classification classification = classificationOpt.get();
                if (! classification.isSafe()){
                    Classifier<String> classifier = classification.classifier();
                    var newValue = classifier.generateString(columnValue, row, columnName);
                    if (!columnValue.equals(newValue)) {
                        rows.updateString(columnName, newValue);
                    }
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
            log.warning("Failed to anonymize JDBC " + e.getMessage());
            e.printStackTrace();
            log.throwing("Anonymouse", "run", e);
        } finally {
            log.fine("JDBC Iterator finished");
        }
    }

    private boolean ping() {
        try (var conn = getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1+1");
            log.info("JDBC ping success");
            return true;
        } catch (SQLException ex) {
            log.info("JDBC ping failed");
            ex.printStackTrace();
            return false;
        }
    }

    public void setReport(Report report) {
        this.report = report;
    }
}
