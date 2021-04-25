package cloud.caravana.anonymouse.classifier;

import cloud.caravana.anonymouse.Classification;
import cloud.caravana.anonymouse.Configuration;
import cloud.caravana.anonymouse.PIIClass;
import cloud.caravana.anonymouse.Setting;
import java.util.Optional;
import java.util.logging.Logger;
import javax.inject.Inject;

@SuppressWarnings("CdiUnproxyableBeanTypesInspection")
public abstract class Classifier<T> {
    @Inject
    Logger log;

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

    public abstract Optional<Classification> classify(T value, String... context);

    public abstract T generate(T value, int index, String... context) ;

    Optional<Classification> ifDeclared(T value,
                                        PIIClass target,
                                        String[] context) {
        if ((value != null)
            && (!isAnonymized(value))) {
            var cname = cname(context);
            boolean isTargeted = cfg.isDeclared(cname, target);
            if (isTargeted) {
                return target.by(this);
            }
        }
        return Optional.empty();
    }

    protected boolean isAnonymized(T value) {
        return value == null;
    }

    public Classification<T> of(PIIClass piiClass){
        return new Classification<>(piiClass,this);
    }
}
