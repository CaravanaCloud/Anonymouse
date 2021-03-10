package cloud.caravana.anonymouse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.sql.*;
import java.sql.*;
import java.util.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = {"/context/simple_applicationContext.xml"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    FlywayTestExecutionListener.class})
@FlywayTest
public class AnonymouseTest extends BaseDBHelper {
    private final Log logger = LogFactory.getLog(getClass());

    /**
     * Made a clean init migrate usage before execution of test method.
     * SQL statements will be loaded from the default location.
     */
    @Test
    @FlywayTest(locationsForMigrate = {"anonName"})
    public void dummyTestMethodLoad() throws Exception {
        Anonymouse anon = new Anonymouse();
        assertTrue(hasNamedCustomer());
        anon.run(ds);
        assertFalse(hasNamedCustomer());
    }

    public Boolean hasNamedCustomer() throws Exception{
        try (Statement stmt = con.createStatement()) {
            String query = "select * from CUSTOMER";

            try (ResultSet rs = stmt.executeQuery(query)) {
                while(rs.next()){
                    String name = rs.getString("cus_name");
                    System.out.println("---- "+ name);
                    if (! name.startsWith(Anonymouse.ANON_PREFIX)){
                        return true;
                    }    
                }
            }
        }

        return false;
    }



}

