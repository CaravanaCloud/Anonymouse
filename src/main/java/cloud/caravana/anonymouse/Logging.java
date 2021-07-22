package cloud.caravana.anonymouse;

import javax.enterprise.inject.Produces;
import java.util.logging.Logger;


public class Logging {
    @Produces
    public Logger getLogger() {
        return Logger.getLogger("anonymouse");
    }
}
