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

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;
import org.hibernate.JDBCException;

@SuppressWarnings("CdiUnproxyableBeanTypesInspection")
@ApplicationScoped
public class JDBCIterator implements Runnable {

    @Inject
    Configuration cfg;

    @Inject
    Logger log;

    @Inject
    Provider<DataSource> dsProvider;

    @Inject
    Classifiers classifiers;

    @Inject
    ExecutorService executor;

    public void runTable(Table table) {
        var rowCnt = 0;
        try (var conn = getConnection()) {
            conn.setAutoCommit(true);
            var tableCat = table.tableCat();
            var tableName = table.tableName();
            var tableFQN = tableName;
            log.info("Anonymizing [%s]".formatted(table.toString()));
            var sql = "SELECT * FROM " + tableFQN;
            try(var statement = conn
                .createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);){
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
                    if (rowCnt % cfg.getLogEach() == 0){
                        log.info("Row [%s] Table [%s] ".formatted(rowCnt, table));
                    }
                    for (var col = 1; col <= colCnt; col++) {
                        int columnType = rowsMD.getColumnType(col);
                        var columnName = rowsMD.getColumnName(col);
                        try {
                            visitColumn(rowCnt, tableName, rows, col, columnType, columnName);
                        } catch (Exception e){
                            e.printStackTrace();
                            log.warning("Fail to visit cell ");
                        }
                    }
                    try {
                        rows.updateRow();
                    } catch (SQLException ex){
                        ex.printStackTrace();
                        log.warning("Fail to update row  ");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            log.warning("Failed to anonymize %s".formatted(table));
            log.throwing("JDBCIterator","runTable", ex);
        }
        log.info("Anonymized [%s]rows of [%s]".formatted(rowCnt,table.toString()));
    }

    private Connection getConnection() throws SQLException {
        if (cfg.isJDBCReady()) {
            DataSource ds = dsProvider.get();
            return ds.getConnection();
        }else return null;
    }

    private void visitColumn(int rowCnt, String tableName, ResultSet rows, int col, int columnType, String columnName) {
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
                             int row,
                             ResultSet rows) throws SQLException{
        var value = rows.getDate(columnName);
        if(value != null){
            var classification = classifiers.classify(value, tableName, columnName)
                                   .map(Classification::classifier);
            if (classification.isPresent()){
                Classifier<Date> classifier = classification.get();
                var utilDate = classifier.generateDate(value, row, columnName);
                var sqlDate = new Date(utilDate.getTime());
                rows.updateDate(columnName, sqlDate);
            }
        }
    }

    private void visitString(String tableName,
                             String columnName,
                             int row,
                             ResultSet rows) throws SQLException{
            var columnValue = rows.getString(columnName);
            if (columnValue != null){
                var classification = classifiers.classify(columnValue, tableName, columnName)
                  .map(Classification::classifier);
                if (classification.isPresent()){
                    Classifier<String> classifier = classification.get();
                    var newValue = classifier.generateString(columnValue, row, columnName);
                    if (! columnValue.equals(newValue )){
                        rows.updateString(columnName, newValue);
                    }
                }
            }
    }

    public void runTables() throws Exception {
        try (var conn = getConnection()) {
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, null, null, null);
            while (rs.next()) {
                var table = Table.of(rs);
                if (isTruncate(table)){
                    truncate(table);
                }else if (!isSkippedTable(table)) {
                    executor.submit( () -> runTable(table));
                } else {
                    log.fine("Skipping %s".formatted(table));
                }
            }
        } finally {
            log.exiting("Anonymouse", "runTables");
        }
    }

    private void truncate(Table table) {
        var sql = "TRUNCATE TABLE %s".formatted(table.tableName());
        try (var conn = getConnection();
             var stmt = conn.createStatement()) {
            var upCount = stmt.executeUpdate(sql);
            log.info("Truncated [%d] %s".formatted(upCount, table));
        }catch (SQLException ex){
            log.warning("Failed to truncate %s".formatted(table));
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
        var executorMonitor =
            Executors.newSingleThreadScheduledExecutor();
        Runnable periodicTask = () -> {
            log.info("JDBC ExecutorService Shutdown[%b] Terminated[%b]".formatted(
                executor.isShutdown(),
                executor.isTerminated()
            ));
        };
        executorMonitor.scheduleAtFixedRate(periodicTask, 10, 30, TimeUnit.SECONDS);
        try {
            ping();
            runTables();
            log.fine("Database iterated. Shutting executors.");
            try {
                executor.shutdown();
                Integer timeout = cfg.getTimeoutMinutes();
                if (!executor.awaitTermination(timeout, TimeUnit.MINUTES)) {
                    log.warning("Executor shutdown timed out");
                }
            } catch (InterruptedException ie) {
                log.warning("Executor shutdown interrupted");
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }finally {
                log.info("Shutting down NOW");
                executor.shutdownNow();
                executorMonitor.shutdownNow();
            }
            log.fine("Executed.");
        } catch (Exception e) {
            log.info("Failed to anonymize");
            log.throwing("Anonymouse", "run", e);
            throw new RuntimeException(e);
        } finally {
            var t1 = System.currentTimeMillis();
            var t = (t1 - t0) / 1000.0D;
            log.info("Database anonymized. Took [%1$,.4f]s".formatted(t));
        }
    }

    private void ping() throws SQLException {
        try (var conn = getConnection();
            var stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1+1");
        }
    }
}
