package cloud.caravana.anonymouse.iter;

import com.google.errorprone.annotations.Var;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class DDBIterator implements Runnable {
    @Inject
    Logger log;

    @Override
    public void run() {
        try(DynamoDbClient ddb = DynamoDbClient.builder().build();){
            var resp = ddb.listTables();
            var tableNames = resp.tableNames();
            tableNames.forEach(t -> runTable(ddb,t));
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
                count++;
                System.out.println(item);
            }
            lastKeyEvaluated = result.lastEvaluatedKey();
        } while (lastKeyEvaluated != null);
        log.info("Table [%s] [%d] items scanned ".formatted(tableName,count));
    }
}
