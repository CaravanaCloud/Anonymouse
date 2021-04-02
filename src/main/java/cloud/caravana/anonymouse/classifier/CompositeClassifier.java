package cloud.caravana.anonymouse.classifier;

import cloud.caravana.anonymouse.Classification;
import cloud.caravana.anonymouse.PIIClass;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CompositeClassifier extends Classifier{
    List<Classifier> classifiers = new ArrayList<>();

    @Inject
    public CompositeClassifier(
        NameClassifier names,
        PhoneClassifier phones){
        classifiers.add(names);
        classifiers.add(phones);
    }

    @Override
    public Optional<Classification> classify(String value, String... context) {
        var result =
            classifiers.stream()
                       .map(cx -> cx.classify(value,context))
                       .filter(Optional::isPresent)
                       .findFirst()
                       .orElseGet(Optional::empty);
        return result;
    }
}
