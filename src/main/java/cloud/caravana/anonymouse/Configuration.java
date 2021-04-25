package cloud.caravana.anonymouse;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hashids.Hashids;
import org.yaml.snakeyaml.Yaml;

@ApplicationScoped
@Default
public class Configuration {

    @Inject
    Logger log;

    @ConfigProperty(name = "jdbc.timeout_minutes", defaultValue= "5")
    Integer timeoutMinutes;

    @ConfigProperty(name = "jdbc.log_each", defaultValue= "10000")
    Integer logEach;

    @ConfigProperty(name = "hashids.salt", defaultValue= "Anonymouse")
    String hashSalt;

    @Produces
    public Hashids produceHashid(){
        return new Hashids(hashSalt);
    }



    Map<String, PIIClass> piiClasses = new HashMap<>();

    @PostConstruct
    public void loadFromEnv(){
        optConfigFilePath().ifPresent(this::addFromURL);
    }

    public Integer getTimeoutMinutes(){
        return timeoutMinutes;
    }

    public Integer getLogEach() {
        return logEach;
    }

    Optional<String> optConfigFilePath() {
        return optEnv("ANONYM_CONFIG");
    }

    @SuppressWarnings("all")
    private static Optional<String> optEnv(String varName) {
        Map<String, String> env = System.getenv();
        return Optional.ofNullable(env.get(varName));
    }

    @Produces
    @Setting("anonPrefix")
    public final String getAnonPrefix() {
        return "|#|";
    }

    public final void add(final String url) {
        if (url == null) {
            log.warning("Cannot load null configuration URL");
            return;
        }
        addFromURL(url);
    }

    @SuppressWarnings("all")
    private void addFromURL(final String url) {
        log.info("Loading configuration from [%s]".formatted(url));
        String[] split = url.split(":");
        var protocol = split[0];
        switch (protocol){
            case "classpath" -> addFromResource(url);
            default -> addFromStream(url);
        }
    }

    private void addFromStream(String addr) {
        try {
            URL url = new URL(addr);
            try (InputStream is = url.openStream()){
                addFromInput(is);
            }catch (IOException ex){
                log.warning("Failed to read from url "+addr);
            }
        }catch (MalformedURLException ex){
            log.warning("Failed to parse url "+addr);
        }
    }

    private void addFromResource(String url) {
        String[] split = url.split(":");
        var resource = split[1];
        try (var input = getClass().getResourceAsStream(resource)
        ) {
            addFromInput(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void addFromInput(InputStream istream) {
        if (istream != null) {
            var yaml = new Yaml();
            @SuppressWarnings("unchecked")
            var cfgMap = (Map<String, Object>) yaml.load(istream);
            cfgMap.forEach(this::addRoot);
        } else {
            log.warning("Failed to load config");
        }
    }

    private void addRoot(final String key, final Object value) {
        if (value instanceof String) {
            PIIClass piiClass = PIIClass.valueOf((String) value);
            setPIIClass(piiClass, key);
        }
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            map.forEach((ckey, cvalue) ->
                addChild(key, ckey, cvalue.toString()));
        }
    }

    private void addChild(final String key,
                          final String ckey,
                          final  String cvalue) {
        PIIClass piiClass = PIIClass.valueOf(cvalue);
        String cname = cname(key,ckey);
        setPIIClass(piiClass, cname);
    }

    private void setPIIClass(final PIIClass piiClass,
                             final String cname) {
        piiClasses.put(cname.toUpperCase(), piiClass);
    }

    public final PIIClass getPIIClass(String... context) {
        var cname = cname(context);
        PIIClass piiClass = piiClasses.get(cname.toUpperCase());
        return piiClass;
    }

    private String cname(String... context) {
        return String.join(".",context);
    }

    public boolean isDeclared(String cname, PIIClass piiClass) {
        var declaredClass = getPIIClass(cname);
        boolean isDeclared = declaredClass != null && declaredClass.equals(piiClass);
        return isDeclared;
    }


}
