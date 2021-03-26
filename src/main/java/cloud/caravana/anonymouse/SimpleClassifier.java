package cloud.caravana.anonymouse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SimpleClassifier extends PIIClassifier {
    private Set<String> piiCols;

    public SimpleClassifier(){}

    public SimpleClassifier(String piiCols) {
        String[] piiColsArr = piiCols.toUpperCase().split("\\,");
        this.piiCols = new HashSet<>(Arrays.asList(piiColsArr));
    }

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

    public boolean isPIISafe(String tableName, String columnName, String columnValue){
        return PIIClass.Safe.equals(classify(tableName, columnName, columnValue));
    }

    private static boolean isCleared(String columnValue) {
        return columnValue == null || columnValue.startsWith(Anonymouse.ANON_PREFIX);
    }

}
