package cloud.caravana.anonymouse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flywaydb.core.Flyway;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@Import(TestDBConfig.class)
public class AnonymouseTest {

    private final Log logger = LogFactory.getLog(getClass());
    Anonymouse anonymouse;

    @Autowired
    @Qualifier("test")
    private DataSource datasource;

    @Autowired
    private TestDBConfig db;

    @Autowired
    private JdbcTemplate jdbc;

    @Before
    public void beforeTest() {
        anonymouse = new Anonymouse(datasource, "customer.cus_name");
    }

    @Test
    public void testConnectivity() throws Exception {
        //anonymizer.run();
        //assertFalse(hasNamedCustomer());
        assertNotNull(datasource);
        assertNotNull(datasource.getConnection());
    }

    @Test
    public void testInitialDataHasNames() {
        //given
        //when
        db.migrate("db/migration", "anonName");
        //then
        assertTrue(hasNamedCustomer());
    }

    @Test
    public void testAnonDataHasNoNames() {
        //given
        db.migrate("db/migration", "anonName");
        //when
        anonymouse.run();
        //then
        assertFalse(hasNamedCustomer());
    }

    public Boolean hasNamedCustomer() {
        String sql = "SELECT * FROM CUSTOMER";
        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        for (Map row : rows) {
            String cusName = (String) row.get("cus_name");
            Boolean isName = !anonymouse.isAnonymized("CUSTOMER", "cus_name", cusName);
            if (isName) return true;
        }
        return false;
    }
}

