package cloud.caravana.anonymouse;

// Object classification by personally identifiable information (PII) is based on recognizing any personally identifiable artifacts based on industry standards such as NIST-80-122 and FIPS 199. Macie Classic can recognize the following PII artifacts:
public class PIIClassifier {
    public PIIClass classify(String tableName, String columnName, String columnValue){
        return PIIClass.Safe;
    }
}
