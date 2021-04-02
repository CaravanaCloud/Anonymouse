package cloud.caravana.anonymouse.classifier;

import cloud.caravana.anonymouse.Classification;
import cloud.caravana.anonymouse.Configuration;
import cloud.caravana.anonymouse.PIIClass;
import cloud.caravana.anonymouse.Setting;
import java.util.Optional;
import javax.inject.Inject;

public class Classifier {
    @Inject
    Configuration cfg;

    @Inject
    @Setting("anonPrefix")
    String anonPrefix;


    String cname(String... context) {
        String cname = String.join(".", context)
                             .toUpperCase();
        return cname;
    }

    public Optional<Classification> classify(String value, String... context) {
        return PIIClass.OtherPII.by(this);
    }

    Optional<Classification> _classify(String value, PIIClass target, String[] context) {
        if ((value != null) &&
            (!isAnonymized(value))){
            var cname = cname(context);
            boolean isTargeted = cfg.isDeclared(cname, target);
            if (isTargeted)
                return target.by(this);
        }
        return Optional.empty();
    }

    public String generateString(String columnName, int rowId){
        return "";
    }

    protected boolean isAnonymized(String value) {
        boolean isPIISafe = value.startsWith(anonPrefix);
        return isPIISafe;
    }


}