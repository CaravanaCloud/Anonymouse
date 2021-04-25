package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.Telephone;

import cloud.caravana.anonymouse.Classification;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PhoneClassifier extends Classifier<String> {
    @Override
    public Optional<Classification> classify(String value,
                                             String... context) {
        return ifDeclared(value.toString(), Telephone, context);
    }

    @Override
    public String generate(String columnValue, int index, String... context) {
        return columnValue.toString().replaceAll("[0-9]", "5");
    }

    @Override
    protected boolean isAnonymized(String value) {
        return value.toString().startsWith("5555");
    }
}
