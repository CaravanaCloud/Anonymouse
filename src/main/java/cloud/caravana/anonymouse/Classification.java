package cloud.caravana.anonymouse;

import cloud.caravana.anonymouse.classifier.Classifier;
import java.util.Optional;

public record Classification<T>(
    PIIClass piiClass,
    Classifier<T> classifier
) {

}
