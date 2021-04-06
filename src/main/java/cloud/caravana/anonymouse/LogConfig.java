package cloud.caravana.anonymouse;

import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class LogConfig {

    @Produces
    public Logger getLogger() {
        return Logger.getLogger("anonymouse");
    }
}
