package cloud.caravana.anonymouse.iter;

import cloud.caravana.anonymouse.Classification;
import cloud.caravana.anonymouse.classifier.Classifier;
import cloud.caravana.anonymouse.classifier.Classifiers;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class DDBIterator implements Runnable {
    @Inject
    Logger log;

    @Inject
    Classifiers classifiers;

    @Override
    public void run() {
        try(var ddb = DynamoDbClient.builder().build();){
            var resp = ddb.listTables();
            var tableNames = resp.tableNames();
            tableNames.forEach(t -> runTable(ddb,t));
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private void runTable(DynamoDbClient ddb, String tableName) {
        Map<String, AttributeValue> lastKeyEvaluated = null;
        Long count = 0L;
        do {
            var req =  ScanRequest.builder()
                    .tableName(tableName)
                    .exclusiveStartKey(lastKeyEvaluated)
                    .build();
            var result = ddb.scan(req);
            for (Map<String, AttributeValue> item : result.items()){
                runItem(ddb, tableName, item, count++);
            }
            lastKeyEvaluated = result.lastEvaluatedKey();
        } while (lastKeyEvaluated != null);
        log.info("Table [%s] [%d] items scanned ".formatted(tableName,count));
    }

    private void runItem(DynamoDbClient ddb,
                         String tableName,
                         Map<String, AttributeValue> item,
                         Long count) {
        for (Map.Entry<String, AttributeValue> entry : item.entrySet()) {
            String key = entry.getKey();
            AttributeValue value = entry.getValue();
            if (value != null)
                runAttribute(ddb, tableName, item, key, value, count);
        }
        var request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();
        ddb.putItem(request);
    }

    private void runAttribute(DynamoDbClient ddb,
                              String tableName,
                              Map<String, AttributeValue> item,
                              String key,
                              AttributeValue value,
                              Long count) {
        if( value.hasSs() ){
            var valS = value.s();
            var classification = classifiers
                    .classify(valS, tableName, key)
                    .map(Classification::classifier);
            if (classification.isPresent()){
                Classifier<String> classifier = classification.get();
                var newValue = classifier.generateString(valS, count, key);
                if (! valS.equals(newValue)){
                    var newValS = AttributeValue.builder().s(newValue).build();
                    item.put(key,newValS);
                }
            }
        }

    }
}
