package cloud.caravana.anonymouse;


import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.ShutdownEvent;
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
    Configuration cfg;

    @Inject
    Logger log;

    @Override
    public int run(String... args) throws Exception {
        anonymouse.safeRun();
        return 0;
    }

    public static void main(String... args) {
        Quarkus.run(Main.class, args);
    }


    void onStop(@Observes ShutdownEvent ev) {
        if(cfg.isExitOnStop()){
            log.info("Anonymouse exited");
            Quarkus.waitForExit();
            System.exit(0);
        }else {
            log.info("Anonymouse stopped");
        }
    }
}
