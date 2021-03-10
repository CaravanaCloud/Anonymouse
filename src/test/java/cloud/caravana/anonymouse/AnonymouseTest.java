package cloud.caravana.anonymouse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

// essentials
// database
import java.sql.*;
// testing libs
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = {"/context/simple_applicationContext.xml"})
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    FlywayTestExecutionListener.class})
@FlywayTest
public class AnonymouseTest extends BaseDBHelper {
    private final Log logger = LogFactory.getLog(getClass());
    Anonymouse anonymizer;

    @Before
    public void initAnonymouse(){
        anonymizer = new Anonymouse(datasource,"customer.cus_name");
    }

    @Test
    @FlywayTest(locationsForMigrate = {"anonName"})
    public void testAnonymizeNames() throws Exception {
        assertTrue(hasNamedCustomer());
        anonymizer.run();
        assertFalse(hasNamedCustomer());
    }

    public Boolean hasNamedCustomer() throws Exception{
        try (Statement stmt = con.createStatement()) {
            String tableName = "CUSTOMER";
            String query = "select * from " + tableName;

            try (ResultSet rs = stmt.executeQuery(query)) {
                while(rs.next()){
                    String colName = "cus_name";
                    String colValue = rs.getString(colName);
                    Boolean isName = ! anonymizer.isAnonymized(tableName,colName,colValue);
                    System.out.println(colValue + " ? " + isName);
                    if (isName){
                        return true;
                    }    
                }
            }
        }
        return false;
    }
}

