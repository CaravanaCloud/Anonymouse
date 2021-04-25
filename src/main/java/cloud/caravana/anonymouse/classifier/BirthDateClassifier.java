package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.DateTime;

import cloud.caravana.anonymouse.Classification;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BirthDateClassifier extends Classifier<Date> {


    LocalDate startOfTime = LocalDate.of(1582, 10, 15);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public Optional<Classification> classify(Object value, String... context) {
        return ifDeclared(value.toString(), DateTime, context);
    }

    @Override
    public Date generate(Object columnValue, int index, String... context) {
        LocalDate anonDate = startOfTime.minusDays(index);
        return Date.valueOf(anonDate);
    }

    @Override
    protected boolean isAnonymized(Object valueObj) {
        String value = valueObj.toString();
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
