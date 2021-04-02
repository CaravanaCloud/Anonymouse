package cloud.caravana.anonymouse.classifier;

import cloud.caravana.anonymouse.Classification;

import static cloud.caravana.anonymouse.PIIClass.FullName;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NameClassifier extends Classifier{
    @Override
    public Optional<Classification> classify(String value, String... context){
        return ifDeclared(value, FullName, context);
    }

    @Override
    public String generateString(int index, String... context) {
        return anonPrefix + String.join("_",context) + "_" + index;
    }
}
