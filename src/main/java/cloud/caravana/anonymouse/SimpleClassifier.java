package cloud.caravana.anonymouse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SimpleClassifier extends PIIClassifier {
    private Set<String> piiCols;

    public PIIClass classify(String tableName, String columnName, String columnValue){
        if (isCleared(columnValue))
            return PIIClass.Safe;
        if (piiCols.contains(tableName.toUpperCase()))
            return PIIClass.OtherPII;
        String piiCol = (tableName + "." + columnName).toUpperCase();
        if (piiCols.contains(piiCol))
            return PIIClass.OtherPII;
        return PIIClass.Safe;
    }

    public void setPIIColumns(String piiCols) {
        String[] piiColsArr = piiCols.toUpperCase().split("\\,");
        this.piiCols = new HashSet<>(Arrays.asList(piiColsArr));
    }
}
