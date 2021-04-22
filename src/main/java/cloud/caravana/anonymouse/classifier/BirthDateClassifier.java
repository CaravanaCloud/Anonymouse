package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.BirthDate;

import cloud.caravana.anonymouse.Classification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class BirthDateClassifier extends Classifier {


    LocalDate startOfTime = LocalDate.of(1582, 10, 15);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public Optional<Classification> classify(String value, String... context) {
        return ifDeclared(value, BirthDate, context);
    }

    @Override
    public String generateString(String columnValue, int index, String... context) {
        LocalDate anonDate = startOfTime.minusDays(index);
        var formattedDate = formatter.format(anonDate);
        return formattedDate;
    }

    @Override
    protected boolean isAnonymized(String value) {
        try {
            var dateVal = LocalDate.parse(value,formatter);
            var isAnonymized = dateVal.isBefore(startOfTime);
            return isAnonymized;
        } catch (DateTimeParseException e) {
            log.throwing("BirthDateClassifer",
                "isAnonymized",
                e);
            return true;
        }
    }
}
