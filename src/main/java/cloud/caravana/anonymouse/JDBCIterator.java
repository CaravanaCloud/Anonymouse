package cloud.caravana.anonymouse;

import static java.lang.String.format;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static java.sql.Types.DATE;
import static java.sql.Types.VARCHAR;

import cloud.caravana.anonymouse.classifier.Classifiers;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.hibernate.JDBCException;

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
    Classifiers classifiers;

    ExecutorService executor;

    public void runTable(Table table) {
        var rowCnt = 0;
        try (var conn = ds.getConnection()) {
            conn.setAutoCommit(true);
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
                    rows.updateRow();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            log.warning("Failed to anonymize %s".formatted(table));
            log.throwing("JDBCIterator","runTable", ex);
        }
        log.info("Anonymized [%s]rows of [%s]".formatted(rowCnt,table.toString()));
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
                var classifier = classification.get();
                var newValue = classifier.generate(value, row, columnName);
                var newString = newValue.toString();
                rows.updateString(columnName, newString);
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
                    var classifier = classification.get();
                    var newValue = classifier.generate(columnValue, row, columnName);
                    var newString = newValue.toString();
                    rows.updateString(columnName, newString);
                }
            }
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
