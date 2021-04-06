package cloud.caravana.anonymouse;

import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class Anonymouse {
    @Inject
    Logger log;

    @Inject
    JDBCIterator dbIterator;

    public final void run() {
        dbIterator.run();
    }
}
