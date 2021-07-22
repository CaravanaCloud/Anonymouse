package cloud.caravana.anonymouse.report;

import cloud.caravana.anonymouse.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TableResult(
    Table table,
    Boolean success,
    Boolean truncated,
    String errorMessage,
    Long rowsRead,
    LocalDateTime readStartTime,
    LocalDateTime readEndTime
) {

}
