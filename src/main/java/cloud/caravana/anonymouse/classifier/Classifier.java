package cloud.caravana.anonymouse.classifier;

import cloud.caravana.anonymouse.Classification;
import cloud.caravana.anonymouse.Configuration;
import cloud.caravana.anonymouse.PIIClass;

import java.util.Date;
import java.util.Optional;
import javax.inject.Inject;

@SuppressWarnings("CdiUnproxyableBeanTypesInspection")
public abstract class Classifier<T> {


    @Inject
    Configuration cfg;

    String cname(String... context) {
        String cname = String.join(".", context)
                             .toUpperCase();
        return cname;
    }

    public abstract Optional<Classification> classify(T value, String... context);

    public String generateString(String value, long index, String... context){
        return null;
    }

    public Date generateDate(Date value, long index, String... context){
        return null;
    }

    public Optional<Classification> ifDeclared(T value,
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

}
