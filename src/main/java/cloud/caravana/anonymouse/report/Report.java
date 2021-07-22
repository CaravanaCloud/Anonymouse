package cloud.caravana.anonymouse.report;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Report{
    LocalDateTime creationTime;
    LocalDateTime startTime;
    LocalDateTime endTime;
    JDBCResult jdbcResult;

    public JDBCResult jdbcResult(){
        return jdbcResult;
    }

    public static final Report of(){
        var report = new Report();
        report.creationTime = LocalDateTime.now();
        report.jdbcResult = JDBCResult.of();
        return report;
    }

    public void startNow() {
        this.startTime = LocalDateTime.now();
    }

    public void endNow() {
        this.endTime = LocalDateTime.now();
    }

    public Long elapsedSecs(){
        return startTime.until( endTime, ChronoUnit.SECONDS );
    }

}
