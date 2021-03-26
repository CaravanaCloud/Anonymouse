package cloud.caravana.anonymouse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class Classifier {
    private Set<String> piiCols;

    public Classifier(){}

    public Classifier(String piiCols) {
        String[] piiColsArr = piiCols.toUpperCase().split("\\,");
        this.piiCols = new HashSet<>(Arrays.asList(piiColsArr));
    }

    public boolean isInteresting(String tableName, String columnName, String columnValue){
        if (isCleared(columnValue))
            return false;
        if (piiCols.contains(tableName.toUpperCase()))
            return true;
        String piiCol = (tableName + "." + columnName).toUpperCase();
        if (piiCols.contains(piiCol))
            return true;
        return false;
    }

    private boolean isCleared(String columnValue) {
        return columnValue == null || columnValue.startsWith(Anonymouse.ANON_PREFIX);
    }

}
