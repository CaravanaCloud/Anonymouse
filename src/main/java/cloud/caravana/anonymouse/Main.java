package cloud.caravana.anonymouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class Main implements CommandLineRunner {
    @Autowired
    private Anonymouse anonymouse;

    @Override
    public void run(String... args) {
        anonymouse.run();
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
