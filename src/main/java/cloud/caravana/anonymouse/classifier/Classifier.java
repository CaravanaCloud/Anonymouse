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

    public Optional<Classification> classify(Object value, String... context) {
        return PIIClass.OtherPII.by(this);
    }

    public abstract Object generate(Object value, int index, String... context) ;

    Optional<Classification> ifDeclared(Object value,
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

    protected boolean isAnonymized(Object value) {
        return value == null;
    }
}
