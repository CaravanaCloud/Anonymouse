package cloud.caravana.anonymouse;

import cloud.caravana.anonymouse.iter.DDBIterator;
import cloud.caravana.anonymouse.iter.JDBCIterator;
import cloud.caravana.anonymouse.report.Report;

import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class Anonymouse {
    @Inject
    Logger log;

    @Inject
    Instance<JDBCIterator> jdbcIterator;

    @Inject
    Instance<DDBIterator> ddbIterator;

    @Inject
    Configuration cfg;

    public final void run() {
        var report = Report.of();
        report.startNow();
        cfg.beforeRunWait();
        if (jdbcIterator.isResolvable()){
            cfg.onJDBCReady(jdbcIterator.get(), report);
        }
        if (ddbIterator.isResolvable()){
            cfg.onDDBReady(ddbIterator.get());
        }
        report.endNow();
        log.info(report.toString());
        log.info("Elased seconds [%d]".formatted(report.elapsedSecs()));

        log.info("Over and out.");
    }
}
