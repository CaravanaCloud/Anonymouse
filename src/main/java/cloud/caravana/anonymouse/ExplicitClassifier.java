package cloud.caravana.anonymouse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ExplicitClassifier {
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
}
