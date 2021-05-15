package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.Erase;

import cloud.caravana.anonymouse.Classification;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EraseClassifier extends Classifier<String> {
    @Override
    public Optional<Classification> classify(String value,
                                             String... context) {
        return ifDeclared(value, Erase, context);
    }

    @Override
    public String generateString(String columnValue, long index, String... context) {
        return "";
    }

    @Override
    protected boolean isAnonymized(String value) {
        return "".equals(value.toString());
    }
}
