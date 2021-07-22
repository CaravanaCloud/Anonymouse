package cloud.caravana.anonymouse.report;

import cloud.caravana.anonymouse.Configuration;
import cloud.caravana.anonymouse.util.Utils;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class QuteReporter {
    @Inject
    Logger log;

    @Inject
    Engine engine;

    @Location("index.html")
    Template index;

    @Inject
    Configuration cfg;

    public void emmit(Report report){
        var rendered = index.render();
        var reportOutDir = cfg.getReportOutDir();
        var canWrite = reportOutDir != null
                && reportOutDir.isDirectory()
                && reportOutDir.canWrite();
        if (canWrite) {
            Utils.writeFile(rendered, "index.html");
        }else {
            log.warning("Can't write report");
        }
    }
}
