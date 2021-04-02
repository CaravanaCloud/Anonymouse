package cloud.caravana.anonymouse;

import cloud.caravana.anonymouse.classifier.Classifier;

public record Classification(
    PIIClass piiClass,
    Classifier classifier
) {
}
