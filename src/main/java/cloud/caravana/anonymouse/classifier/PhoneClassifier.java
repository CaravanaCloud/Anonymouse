package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.Telephone;

import cloud.caravana.anonymouse.Classification;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PhoneClassifier extends Classifier<String> {

    public static final String PREFIX = "5555";

    @Override
    public Optional<Classification> classify(String value,
                                             String... context) {
        return ifDeclared(value.toString(), Telephone, context);
    }

    @Override
    public String generateString(String columnValue, int index, String... context) {
        var newPhone = columnValue.toString().replaceAll(".", "5");
        return newPhone;
    }

    @Override
    protected boolean isAnonymized(String value) {
        return value.toString().startsWith(PREFIX);
    }
}
