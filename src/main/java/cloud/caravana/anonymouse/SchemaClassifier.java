package cloud.caravana.anonymouse;

public class SchemaClassifier extends PIIClassifier {

    @Override
    public PIIClass classify(String tableName, String columnName, String columnValue) {
        return super.classify(tableName, columnName, columnValue);
    }
}
