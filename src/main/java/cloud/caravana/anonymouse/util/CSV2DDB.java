package cloud.caravana.anonymouse.util;

import cloud.caravana.anonymouse.Configuration;
import cloud.caravana.anonymouse.Main;
import com.opencsv.CSVIterator;
import com.opencsv.CSVReader;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import picocli.CommandLine;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

@CommandLine.Command
public class CSV2DDB  implements Runnable, QuarkusApplication {

    @ConfigProperty(name = "csv2ddb.input", defaultValue = "")
    String input;

    @ConfigProperty(name = "csv2ddb.table", defaultValue = "")
    String tableName;

    @Override
    public void run() {
        System.out.println("Uploading CSV to DynamoDB "+input);
        var ddb = DynamoDbClient.builder().build();
        var inputFile = new File(input);
        if(! inputFile.exists()) {
            System.out.println("File [%s] not exist".formatted(input));
            return;
        }
        try(
            var reader = new CSVReader(new FileReader(inputFile));
        )   {
            List<String[]> entries = reader.readAll();
            if (entries.size() > 1) {
                String[] headers = entries.get(0);
                for (int i = 1; i < entries.size() ; i++) {
                    var entry = entries.get(i);
                    var itemValues = new HashMap<String, AttributeValue>();
                    for (int j = 0; j < headers.length; j++) {
                        String header = headers[j];
                        String value = entry[j];
                        itemValues.put(header, AttributeValue.builder().s(value).build());
                        var request = PutItemRequest.builder()
                                .tableName(tableName)
                                .item(itemValues)
                                .build();
                        try {
                            ddb.putItem(request);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(-1);
                        }
                    }
                }
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }

    }

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(this).execute(args);
    }

    public static void main(String[] args) {
        Quarkus.run(CSV2DDB.class, args);
    }
}
