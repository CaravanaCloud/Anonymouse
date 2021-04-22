package cloud.caravana.anonymouse;

import static java.lang.String.format;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static java.sql.Types.VARCHAR;

import cloud.caravana.anonymouse.classifier.Classifier;
import cloud.caravana.anonymouse.classifier.CompositeClassifier;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

@SuppressWarnings("CdiUnproxyableBeanTypesInspection")
@ApplicationScoped
public class JDBCIterator {

    @Inject
    Configuration cfg;

    @Inject
    Logger log;

    @Inject
    DataSource ds;

    @Inject
    CompositeClassifier cx;

    ExecutorService executor;

    public void runTable(Table table) {
        var rowCnt = 0;
        try (var conn = ds.getConnection()) {
            var tableCat = table.tableCat();
            var tableName = table.tableName();
            var tableFQN = tableCat +"."+tableName;
            log.info("Anonymizing [%s]".formatted(table.toString()));
            var sql = "SELECT * FROM " + tableFQN;
            try(var statement = conn
                .createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);){
                var rows = statement.executeQuery(sql);
                int concurrency = rows.getConcurrency();
                if (concurrency != ResultSet.CONCUR_UPDATABLE) {
                    log.warning("Skipping not updatable table %s".formatted(table));
                }
                var rowsMD = rows.getMetaData();
                var colCnt = rowsMD.getColumnCount();
                while (rows.next()) {
                    rowCnt++;
                    if (rowCnt % cfg.getLogEach() == 0){
                        System.out.println("Row [%s] Table [%s] ".formatted(rowCnt, table));
                    }
                    for (var col = 1; col <= colCnt; col++) {
                        int columnType = rowsMD.getColumnType(col);
                        if (isStringType(columnType)) {
                            updateString(tableName, rowsMD, rows, rowCnt, col);
                        }
                    }
                    rows.updateRow();
                }
            }
        } catch (Exception ex) {
            log.warning("Failed to anonymize %s".formatted(table));
            log.throwing("JDBCIterator","runTable", ex);
        } finally {
            log.warning("Exiting %s".formatted(table));
            log.exiting("JDBCIterator", "runTable");
        }
        log.info("Anonymized [%s]rows of [%s]".formatted(rowCnt,table.toString()));
    }

    private void updateString(String tableName,
                              ResultSetMetaData rowsMD,
                              ResultSet rows,
                              int row,
                              int col) throws SQLException {
            var columnName = rowsMD.getColumnName(col);
            var columnType = rowsMD.getColumnType(col);
            if (isStringType(columnType)) {
                var columnValue = rows.getString(col);
                var piiClassO = cx.classify(columnValue, tableName, columnName);
                var piiClfnO = piiClassO.stream();
                piiClfnO.forEach(piiClfn ->
                    updateCell(tableName, rows, row, col, columnName, columnValue, piiClfn));
            }else{
                log.warning("Can't update column type: "+columnType);
            }
    }

    private void updateCell(String tableName,
                            ResultSet rows,
                            int row,
                            int col,
                            String columnName,
                            String columnValue,
                            Classification piiClfn) {
        Classifier piiCx = piiClfn.classifier();
        String newValue = piiCx.generateString(columnValue, row, columnName);
        log.finer("Updating string ["
            + tableName + "].["
            + columnName + "] := ["
            + newValue + "]");
        try {
            rows.updateString(columnName, newValue);
        } catch (SQLException ex) {
            log.warning("Failed to update [" + tableName + "].[" + col + "] ");
            log.throwing("JDBIterator", "updateString", ex);
        }
    }

    public static boolean isStringType(int columnType) {
        boolean isString = columnType == VARCHAR;
        return isString;
    }

    public void runTables() throws Exception {
        try (var conn = ds.getConnection()) {
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, null, null, null);
            while (rs.next()) {
                var table = Table.of(rs);
                if (!isSkippedTable(table))
                    executor.submit( () -> runTable(table) );
            }
        } finally {
            log.exiting("Anonymouse", "runTables");
        }
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

    public void run() {
        var t0 = System.currentTimeMillis();
        executor = Executors.newWorkStealingPool();
        var executorMonitor =
            Executors.newSingleThreadScheduledExecutor();
        Runnable periodicTask = () -> {
            log.info("Executor is terminated? "+executor.isTerminated());
            log.info("         is shut? "+executor.isShutdown());
        };
        executorMonitor.scheduleAtFixedRate(periodicTask, 10, 30, TimeUnit.SECONDS);
        try {
            ping();
            runTables();
            log.fine("Iterated. Shutting executors.");
            try {
                executor.shutdown();
                Integer timeout = cfg.getTimeoutMinutes();
                if (!executor.awaitTermination(timeout, TimeUnit.MINUTES)) {
                    log.warning("Timed out");
                }
            } catch (InterruptedException ie) {
                log.warning("Interrupted out");
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }finally {
                log.warning("Shutting down");
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
            log.fine("Done. [%1$,.4f]s".formatted(t));
        }
    }

    private void ping() throws SQLException {
        try (var conn = ds.getConnection();
            var stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1+1");
        }
    }
}
