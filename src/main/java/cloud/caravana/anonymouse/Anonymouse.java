package cloud.caravana.anonymouse;

import cloud.caravana.anonymouse.classifier.CompositeClassifier;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class Anonymouse {
    @Inject
    Logger log;

    @Inject
    JDBCIterator dbIterator;

    public void run() {
        dbIterator.run();
    }
}
