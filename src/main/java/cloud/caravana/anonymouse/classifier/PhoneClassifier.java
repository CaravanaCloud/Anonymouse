package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.FullName;
import static cloud.caravana.anonymouse.PIIClass.Telephone;

import cloud.caravana.anonymouse.Classification;
import cloud.caravana.anonymouse.Configuration;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PhoneClassifier extends Classifier {
    @Override
    public Optional<Classification> classify(String value, String... context){
        return _classify(value, Telephone, context);
    }

    @Override
    public String generateString(String columnName, int rowId) {
        return "555" + rowId;
    }

    @Override
    protected boolean isAnonymized(String value) {
        return value.startsWith("555");
    }
}
