package cloud.caravana.anonymouse;

import java.util.UUID;

public class Generator {
    public static String ANON_PREFIX = "Anonymoused ";

    public String anonName(){
        return ANON_PREFIX + UUID.randomUUID().toString();
    }

}
