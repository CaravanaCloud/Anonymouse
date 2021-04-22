package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.Erase;
import static cloud.caravana.anonymouse.PIIClass.Telephone;

import cloud.caravana.anonymouse.Classification;
import java.util.Optional;

public class EraseClassifier extends Classifier {
    @Override
    public Optional<Classification> classify(String value,
                                             String... context) {
        return ifDeclared(value, Erase, context);
    }

    @Override
    public String generateString(String columnValue, int index, String... context) {
        return "";
    }

    @Override
    protected boolean isAnonymized(String value) {
        return "".equals(value);
    }
}
