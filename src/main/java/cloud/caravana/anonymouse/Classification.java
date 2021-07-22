package cloud.caravana.anonymouse;

import cloud.caravana.anonymouse.classifier.Classifier;
import java.util.Optional;

public record Classification(
    PIIClass piiClass,
    Classifier classifier
) {

    public static Classification of(PIIClass piiClass, Classifier classifier) {
        return new Classification(piiClass,classifier);
    }

    public boolean isSafe() {
        return  piiClass.isSafe();
    }
}
