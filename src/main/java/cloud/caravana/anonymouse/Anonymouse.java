package cloud.caravana.anonymouse;

import cloud.caravana.anonymouse.iter.DDBIterator;
import cloud.caravana.anonymouse.iter.JDBCIterator;

import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class Anonymouse {
    @Inject
    Logger log;

    @Inject
    JDBCIterator jdbcIterator;

    @Inject
    DDBIterator ddbIterator;

    @Inject
    Configuration cfg;

    public final void run() {
        cfg.onJDBCReady(jdbcIterator);
        cfg.onDDBReady(ddbIterator);
    }
}
