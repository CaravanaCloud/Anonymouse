package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.FullName;

import cloud.caravana.anonymouse.Classification;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NameClassifier extends Classifier<String> {
    public static final String PREFIX = "|#| ";

    @Override
    public Optional<Classification> classify(String value,
                                             String... context) {
        return ifDeclared(value.toString(), FullName, context);
    }

    @Override
    public String generateString(String columnValue, long index, String... context) {
        String newName = PREFIX
                + String.join("_", context)
                + "_"
                + index;
        return newName;
    }

    @Override
    protected boolean isAnonymized(String value) {
        return value.startsWith(PREFIX);
    }
}
