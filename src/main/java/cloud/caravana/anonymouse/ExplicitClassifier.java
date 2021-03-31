package cloud.caravana.anonymouse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.yaml.snakeyaml.Yaml;

@ApplicationScoped
public class ExplicitClassifier {
    @Inject
    Logger log;

    @Inject
    Configuration cfg;

    public static String ANON_PREFIX = "|#| ";

    protected static boolean isCleared(String columnValue) {
        return columnValue == null || columnValue.startsWith(ANON_PREFIX);
    }


    public PIIClass classify(String value, String... context){
        var cname = cname(context);
        var result = PIIClass.Safe;
        if (! isCleared(value)){
            result = cfg.getPIIClass(cname);
        }
        return result;
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



}
