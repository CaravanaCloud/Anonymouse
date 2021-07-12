package cloud.caravana.anonymouse;


import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import io.quarkus.runtime.configuration.ProfileManager;

@SuppressWarnings("CdiUnproxyableBeanTypesInspection")
@QuarkusMain
public class Main implements QuarkusApplication {
    @Inject
    Anonymouse anonymouse;

    @Inject
    Configuration cfg;

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
        var profile = ProfileManager.getActiveProfile();
        var userDir = System.getProperty("user.dir");
        log.info("Anonymouse starting...");
        log.info("... profile [%s]".formatted(profile));
        log.info("... userDir [%s]".formatted(userDir));
        log.info("... dryRun [%s]".formatted(cfg.isDryRun()));
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("The application is stopping...");
        //System.exit(0);
    }
}
