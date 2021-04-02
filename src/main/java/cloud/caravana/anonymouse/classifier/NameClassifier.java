package cloud.caravana.anonymouse.classifier;

import cloud.caravana.anonymouse.Classification;
import cloud.caravana.anonymouse.Configuration;
import static cloud.caravana.anonymouse.PIIClass.FullName;
import cloud.caravana.anonymouse.Setting;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class NameClassifier extends Classifier{
    @Override
    public Optional<Classification> classify(String value, String... context){
        return _classify(value, FullName, context);
    }

    @Override
    public String generateString(String columnName, int rowId) {
        return anonPrefix+ " name "+ columnName + "_" + rowId;
    }
}
