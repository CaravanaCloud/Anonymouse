package cloud.caravana.anonymouse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings({"unchecked","preview"})
@ApplicationScoped
public class BaseConfiguration implements Configuration{
    @Inject
    Logger log;

    Map<String,PIIClass> piiClasses = new HashMap<String,PIIClass>();

    @Override
    public void add(String URL) {
        if (URL == null){
            log.warning("Cannot load null configuration URL");
            return;
        }
        if (URL.startsWith("classpath:")) {
            var resource = URL.split(":")[1];
            try (
                var istream = this.getClass()
                                  .getResourceAsStream(resource)
            ) {
                if (istream != null) {
                    var yaml = new Yaml();
                    var cfgMap = (Map<String, Object>) yaml.load(istream);
                    cfgMap.forEach((key,value) -> addRoot(key,value));
                } else {
                    log.warning("Failed to load config [%s]".formatted(URL));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void addRoot(String key, Object value) {
        if (value instanceof Map){
            Map<String,Object> map = (Map<String, Object>) value;
            map.forEach((ckey,cvalue) -> addChild(key,ckey,cvalue.toString()));
        }
    }

    private void addChild(String key, String ckey, String cvalue) {
        PIIClass piiClass = PIIClass.valueOf(cvalue);
        String cname = key + "." + ckey;
        setPIIClass(piiClass,cname);
    }

    public void setPIIClass(PIIClass piiClass, String cname) {
        piiClasses.put(cname.toUpperCase(), piiClass);
    }

    @Override
    public PIIClass getPIIClass(String cname) {
        PIIClass piiClass = piiClasses.getOrDefault(cname, PIIClass.Safe);
        return piiClass;
    }
}
