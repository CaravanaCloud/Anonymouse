package cloud.caravana.anonymouse.classifier;

import static cloud.caravana.anonymouse.PIIClass.BirthDate;
import static cloud.caravana.anonymouse.PIIClass.Telephone;

import cloud.caravana.anonymouse.Classification;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

//TODO: Support multiple date patterns
@ApplicationScoped
public class BirthDateClassifier extends Classifier{
    Calendar anonCal;
    String ANON_DATE = "01/01/0001";

    public BirthDateClassifier(){
        anonCal = Calendar.getInstance();
        anonCal.setTimeInMillis(0L);
    }

    @Override
    public Optional<Classification> classify(String value, String... context){
        return ifDeclared(value, BirthDate, context);
    }

    @Override
    public String generateString(int index, String... context) {
       var newVal = (Calendar) anonCal.clone();
       newVal.add(Calendar.DAY_OF_YEAR, -1 * index);
       var formatter = new SimpleDateFormat("dd/MM/yyyy");
       var formattedDate = formatter.format(newVal.getTime());
       return formattedDate;
    }

    @Override
    protected boolean isAnonymized(String value) {
        var parser = new SimpleDateFormat("dd/MM/yyyy");
        try {
            var date = parser.parse(value);
            var val = Calendar.getInstance();
            val.setTime(date);
            var isBefore = val.before(anonCal);
            return isBefore;
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
    }
}
