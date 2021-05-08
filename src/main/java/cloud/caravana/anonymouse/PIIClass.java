package cloud.caravana.anonymouse;

import cloud.caravana.anonymouse.classifier.Classifier;
import java.util.Optional;

public enum PIIClass {
    DateTime,
    Telephone,
    FullName,
    Email,
    CPF,
    Hashid,
    Safe,
    Erase,
    Truncate,
    OtherPII;


    public Optional<Classification> by(Classifier cx) {
        return Optional.of(new Classification(this, cx));
    }
}
