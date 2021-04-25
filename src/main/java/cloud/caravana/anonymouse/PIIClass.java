package cloud.caravana.anonymouse;

import cloud.caravana.anonymouse.classifier.Classifier;
import java.util.Optional;

public enum PIIClass {
    DateTime,
    Telephone,
    FullName,
    Email,
    Hashid,
    Safe,
    Erase,
    OtherPII;

    public Optional<Classification> by(Classifier cx) {
        return Optional.of(new Classification(this, cx));
    }
}
