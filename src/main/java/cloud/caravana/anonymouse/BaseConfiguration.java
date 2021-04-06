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
    public final void add(final String url) {
        if (url == null) {
            log.warning("Cannot load null configuration URL");
            return;
        }
        if (url.startsWith("classpath:")) {
            addFromURL(url);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void addFromURL(final String url) {
        var resource = url.split(":")[1];
        try (
            var istream = getClass().getResourceAsStream(resource)
        ) {
            if (istream != null) {
                var yaml = new Yaml();
                @SuppressWarnings("unchecked")
                var cfgMap = (Map<String, Object>) yaml.load(istream);
                cfgMap.forEach(this::addRoot);
            } else {
                log.warning("Failed to load config [%s]".formatted(url));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addRoot(final String key, final Object value) {
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
        String cname = key + "." + ckey;
        setPIIClass(piiClass, cname);
    }

    private void setPIIClass(final PIIClass piiClass,
                             final String cname) {
        piiClasses.put(cname.toUpperCase(), piiClass);
    }

    @Override
    public final PIIClass getPIIClass(final String cname) {
        return piiClasses.getOrDefault(cname, PIIClass.Safe);
    }
}
