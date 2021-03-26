package cloud.caravana.anonymouse;

import java.util.logging.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

@Configuration
public class LogConfig {

    @Bean
    @Scope("singleton")
    @Primary
    public Logger getLogger(){
        return Logger.getLogger("anonymouse");
    }
}
