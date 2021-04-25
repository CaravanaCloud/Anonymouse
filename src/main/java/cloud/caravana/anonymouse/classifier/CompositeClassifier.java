package cloud.caravana.anonymouse.classifier;

import cloud.caravana.anonymouse.Classification;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CompositeClassifier extends Classifier {
    List<Classifier> classifiers = new ArrayList<>();

    @Inject
    public CompositeClassifier(
        NameClassifier names,
        PhoneClassifier phones,
        BirthDateClassifier bdates,
        EraseClassifier erasure,
        EmailClassifier emails,
        HashidClassifier hashes) {
        classifiers.addAll(List.of(names,
            phones,
            bdates,
            erasure,
            emails,
            hashes));
    }

    @Override
    public Optional<Classification> classify(Object value, String... context) {
        var result =
            classifiers.stream()
                       .map(cx -> cx.classify(value, context))
                       .filter(Optional::isPresent)
                       .findFirst()
                       .orElseGet(Optional::empty);
        return result;
    }

    @Override
    public Object generate(Object value, int index, String... context) {
        throw new UnsupportedOperationException();
    }
}
