package cloud.caravana.anonymouse.classifier;

import cloud.caravana.anonymouse.Classification;
import cloud.caravana.anonymouse.Configuration;
import cloud.caravana.anonymouse.PIIClass;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class Classifiers {
    @Inject
    NameListClassifier names;
    @Inject
    PhoneClassifier phones;
    @Inject
    DateTimeClassifier bdates;
    @Inject
    EraseClassifier erasure;
    @Inject
    HashidClassifier hashes;
    @Inject
    EmailClassifier emails;
    @Inject
    CPFClassifier cpf;
    
    @Inject
    Configuration cfg;
    

    public <T> Optional<Classification> classify(T value, String tableName, String columnName) {
        PIIClass piiClass = cfg.getPIIClass(tableName, columnName);
        if (piiClass != null){
            Classifier classifier = classifierOf(piiClass);
            if (classifier != null){
                try {
                    Classification classification = Classification.of(piiClass, classifier);
                    return Optional.of(classification);
                }catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        }
        return Optional.empty();
    }

    private Classifier classifierOf(PIIClass piiClass) {
        switch (piiClass){
            case DateTime: return bdates;
            case Email: return emails;
            case Erase: return erasure;
            case Hashid: return hashes;
            case FullName: return names;
            case Telephone: return phones;
            case CPF: return cpf;
            case OtherPII: return null;
            case Safe: return null;
            default: throw new IllegalArgumentException();
        }
    }
}
