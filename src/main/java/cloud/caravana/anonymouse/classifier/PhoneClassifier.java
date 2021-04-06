package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.Telephone;

import cloud.caravana.anonymouse.Classification;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PhoneClassifier extends Classifier {
    @Override
    public Optional<Classification> classify(String value,
                                             String... context) {
        return ifDeclared(value, Telephone, context);
    }

    @Override
    public String generateString(int index, String... context) {
        return "555" + index;
    }

    @Override
    protected boolean isAnonymized(String value) {
        return value.startsWith("555");
    }
}
