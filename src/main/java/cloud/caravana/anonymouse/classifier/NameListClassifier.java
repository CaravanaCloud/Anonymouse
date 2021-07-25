package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.FullName;

import cloud.caravana.anonymouse.Classification;
import cloud.caravana.anonymouse.util.Utils;
import cloud.caravana.anonymouse.util.Wordlists;

import java.io.IOException;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NameListClassifier extends Classifier<String> {
    public static final String PREFIX = "|#| ";

    @Override
    public Optional<Classification> classify(String value,
                                             String... context) {
        return ifDeclared(value.toString(), FullName, context);
    }

    @Override
    public String generateString(String columnValue, long index, String... context) {
        try {
            return Wordlists.randomAnonymFullName();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected boolean isAnonymized(String value) {
        try {
            return Wordlists.isAnonymFullName(value);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
