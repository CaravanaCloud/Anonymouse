package cloud.caravana.anonymouse;

import cloud.caravana.anonymouse.classifier.Classifier;
import java.util.Optional;
import java.util.logging.Logger;

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


    public static PIIClass of(String cvalue) {
        try {
            return PIIClass.valueOf(cvalue);
        }catch (IllegalArgumentException ex){
            Logger.getLogger("PIIClass").warning("Could not find class [%s]".formatted(cvalue));
        }
        return OtherPII;
    }

    public Optional<Classification> by(Classifier cx) {
        return Optional.of(new Classification(this, cx));
    }

    public boolean isSafe() {
        return Safe.equals(this);
    }
}
