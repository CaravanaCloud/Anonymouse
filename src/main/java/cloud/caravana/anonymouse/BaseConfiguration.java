package cloud.caravana.anonymouse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.yaml.snakeyaml.Yaml;

@ApplicationScoped
@Default
public class BaseConfiguration extends Configuration {

    @Inject
    Logger log;

    Map<String, PIIClass> piiClasses = new HashMap<>();

    @Produces
    @Setting("anonPrefix")
    public final String getAnonPrefix() {
        return "|#|";
    }

    @Override
    public final void add(final String URL) {
        if (URL == null) {
            log.warning("Cannot load null configuration URL");
            return;
        }
        if (URL.startsWith("classpath:")) {
            addFromURL(URL);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void addFromURL(final String URL) {
        var resource = URL.split(":")[1];
        try (
            var istream = getClass().getResourceAsStream(resource)
        ) {
            if (istream != null) {
                var yaml = new Yaml();
                var cfgMap = (Map<String, Object>) yaml.load(istream);
                cfgMap.forEach(this::addRoot);
            } else {
                log.warning("Failed to load config [%s]".formatted(URL));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addRoot(final String key, final Object value) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            map.forEach((ckey, cvalue) ->
                addChild(key, ckey, cvalue.toString()));
        }
    }

    private void addChild(final String key,final  String ckey,final  String cvalue) {
        PIIClass piiClass = PIIClass.valueOf(cvalue);
        String cname = key + "." + ckey;
        setPIIClass(piiClass, cname);
    }

    public void setPIIClass(final PIIClass piiClass,final String cname) {
        piiClasses.put(cname.toUpperCase(), piiClass);
    }

    @Override
    public PIIClass getPIIClass(String cname) {
        return piiClasses.getOrDefault(cname, PIIClass.Safe);
    }
}
