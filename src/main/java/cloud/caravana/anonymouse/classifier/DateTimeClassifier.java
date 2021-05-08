package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.DateTime;

import cloud.caravana.anonymouse.Classification;

import java.time.ZoneId;
import java.util.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DateTimeClassifier extends Classifier<Date> {
    @Inject
    Logger log;
    ZoneId defaultZoneId = ZoneId.systemDefault();
    LocalDate startOfTime = LocalDate.of(1582, 10, 15);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public Optional<Classification> classify(Date value, String... context) {
        return ifDeclared(value, DateTime, context);
    }

    @Override
    public Date generateDate(Date columnValue, int index, String... context) {
        LocalDate anonDate = startOfTime.minusDays(index);
        return Date.from(anonDate.atStartOfDay(defaultZoneId).toInstant());
    }

    @Override
    protected boolean isAnonymized(Date valueObj) {
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
