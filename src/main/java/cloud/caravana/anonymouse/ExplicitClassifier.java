package cloud.caravana.anonymouse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
@Scope("singleton")
public class ExplicitClassifier {
    @Autowired
    private Logger log;

    public static String ANON_PREFIX = "|#| ";
    Map<String,PIIClass> piiClasses = new HashMap<String,PIIClass>();

    protected static boolean isCleared(String columnValue) {
        return columnValue == null || columnValue.startsWith(ANON_PREFIX);
    }

    public void setPIIClass(PIIClass piiClass, String cname) {
        piiClasses.put(cname.toUpperCase(), piiClass);
    }

    public PIIClass classify(String value, String... context){
        var cname = cname(context);
        if (isCleared(value))
            return PIIClass.Safe;
        return piiClasses.getOrDefault(cname, PIIClass.Safe);
    }

    public boolean isPIISafe(String value, String... context) {
        PIIClass piiClass = classify(value, context);
        return PIIClass.Safe.equals(piiClass);
    }

    private String cname(String... context) {
        String cname = String.join(".", context).toUpperCase();
        return cname;
    }

    public String generateString(Integer rowNum,String columnName) {
        return ANON_PREFIX + " " + columnName + " " + rowNum;
    }

    public void addConfig(String URL) {
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

}
