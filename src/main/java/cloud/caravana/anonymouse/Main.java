package cloud.caravana.anonymouse;


import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import javax.inject.Inject;

@QuarkusMain
public class Main implements QuarkusApplication {
    @Inject
    Anonymouse anonymouse;

    @Override
    public int run(String... args) throws Exception {
        anonymouse.run();
        return 0;
    }

    public static void main(String ...args) {
        Quarkus.run(Main.class,args);
    }
}

