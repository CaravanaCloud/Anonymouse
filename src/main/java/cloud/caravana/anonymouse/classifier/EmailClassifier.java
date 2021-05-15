package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.Email;

import cloud.caravana.anonymouse.Classification;

import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.hashids.Hashids;

@ApplicationScoped
public class EmailClassifier extends Classifier<String>{
    @Inject
    Hashids hashids;

    @Override
    public Optional<Classification> classify(String value,
                                             String... context) {
        return ifDeclared(value, Email, context);
    }

    @Override
    public String generateString(String columnValue, long index, String... context) {
        var value = columnValue.toString();
        var split = value.split("@");
        if(split.length > 1) {
            var user = split[0];
            long userHash = (long) Math.abs(user.hashCode());
            var hash = hashids.encode(userHash);
            var domain = split[1];
            var gen =  hash + "@" + domain;
            return gen;
        }
        return UUID.randomUUID() + "@anonymouse.id42.cc";
    }

    @Override
    protected boolean isAnonymized(String value) {
        return "".equals(value.toString());
    }

}
