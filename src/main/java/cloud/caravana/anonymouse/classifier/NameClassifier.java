package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.FullName;

import cloud.caravana.anonymouse.Classification;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NameClassifier extends Classifier<String> {
    @Override
    public Optional<Classification> classify(Object value,
                                             String... context) {
        return ifDeclared(value.toString(), FullName, context);
    }

    @Override
    public String generate(Object columnValue, int index, String... context) {
        return anonPrefix
            + String.join("_", context)
            + "_"
            + index;
    }
}
