package cloud.caravana.anonymouse;


import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.inject.Inject;


@SuppressWarnings("CdiUnproxyableBeanTypesInspection")
@QuarkusMain
public class Main implements QuarkusApplication {
    @Inject
    Anonymouse anonymouse;

    @Inject
    Logger log;

    @Override
    public int run(String... args) throws Exception {
        anonymouse.run();
        return 0;
    }

    public static void main(String... args) {
        Quarkus.run(Main.class, args);
    }

    void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting...");
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("The application is stopping...");
        System.exit(0);
    }
}
