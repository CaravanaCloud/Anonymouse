package cloud.caravana.anonymouse;

public class Classifier {
    public static String ANON_PREFIX = "Anonymoused ";

    //TODO: Read from cfg file?
    public boolean isInteresting(String tableName, String columnName){
        return "CUSTOMER".equalsIgnoreCase(tableName) && "cus_name".equalsIgnoreCase(columnName);
    }

    public boolean isInteresting(String tableName){
        return "CUSTOMER".equalsIgnoreCase(tableName);
    }

    public boolean isAnonymized(String name) {
        return name.startsWith(ANON_PREFIX);
    }
}
