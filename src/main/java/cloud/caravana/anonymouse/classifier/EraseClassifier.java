package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.Erase;
import static cloud.caravana.anonymouse.PIIClass.Telephone;

import cloud.caravana.anonymouse.Classification;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EraseClassifier extends Classifier<String> {
    @Override
    public Optional<Classification> classify(Object value,
                                             String... context) {
        return ifDeclared(value, Erase, context);
    }

    @Override
    public String generate(Object columnValue, int index, String... context) {
        return "";
    }

    @Override
    protected boolean isAnonymized(Object value) {
        return "".equals(value.toString());
    }
}