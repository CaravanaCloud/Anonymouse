package cloud.caravana.anonymouse.report;

import cloud.caravana.anonymouse.Table;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public record JDBCResult(
        Map<Table, TableResult> tableResults
) {

    public static JDBCResult of() {
        return new JDBCResult(new HashMap<>());
    }

    public void tableResult(Table table, boolean success, String errorMsg, Long rowCnt, LocalDateTime startTime, LocalDateTime endTime) {
        var tableResult = new TableResult(table,
                success,
                errorMsg,
                rowCnt,
                startTime,
                endTime);
        tableResults.put(table, tableResult);
    }
}
