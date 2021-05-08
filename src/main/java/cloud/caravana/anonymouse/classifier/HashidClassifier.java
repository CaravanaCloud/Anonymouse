package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.Email;
import static cloud.caravana.anonymouse.PIIClass.Hashid;

import cloud.caravana.anonymouse.Classification;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.hashids.Hashids;

@ApplicationScoped
public class HashidClassifier extends Classifier<String>{
    @Inject
    Hashids hashids;

    @Override
    public Optional<Classification> classify(String value,
                                             String... context) {
        return ifDeclared(value, Hashid, context);
    }

    @Override
    public String generateString(String columnValue, int index, String... context) {
        var hashCode = (long) Math.abs(columnValue.hashCode());
        var encode = hashids.encode( hashCode);
        return encode;
    }

    @Override
    protected boolean isAnonymized(String value) {
        return "".equals(value.toString());
    }

}
