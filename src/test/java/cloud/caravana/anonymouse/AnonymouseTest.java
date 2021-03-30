package cloud.caravana.anonymouse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static java.lang.String.*;
import static cloud.caravana.anonymouse.PIIClass.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(TestDBConfig.class)
public class AnonymouseTest {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    @Qualifier("test")
    private DataSource datasource;

    @Autowired
    private TestDBConfig db;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private Anonymouse anonymouse;

    private void loadTest(String migrationLoc) {
        db.migrate( migrationLoc);
        anonymouse.addConfig("classpath:/%s/pii_info.yaml".formatted(migrationLoc));
    }

    @Test
    public void testConnectivity() throws Exception {
        assertNotNull(datasource);
        assertNotNull(datasource.getConnection());
    }

    @Test
    public void testInitialDataHasNames() {
        //when
        loadTest("anonName");
        //then
        assertTrue(hasNamedCustomer());
    }

    @Test
    public void testAnonDataHasNoNames() {
        //given
        loadTest("anonName");
        //when
        anonymouse.run();
        //then
        assertFalse(hasNamedCustomer());
    }

    @Test
    public void testInitialDataHasPhone() {
        //when
        loadTest("anonName");
        //then
        assertTrue(hasPhonedCustomer());
    }

    @Test
    public void testAnonDataHasNoPhone() {
        //given
        loadTest("anonName");
        //when
        anonymouse.run();
        //then
        assertFalse(hasPhonedCustomer());
    }

    private boolean hasPII(PIIClass piiClass, String tbl, String col) {
        var sql = format("SELECT %s FROM %s",col,tbl);
        var rows = jdbc.queryForList(sql);
        boolean hasPhone = rows.stream().anyMatch(row -> isPII(tbl, col, row, piiClass));
        return hasPhone;
    }

    private boolean hasPhonedCustomer() {
        return hasPII(Telephone, "CUSTOMER","cus_phone");
    }

    private boolean hasNamedCustomer() {
        return hasPII(FullName, "CUSTOMER","cus_name");
    }

    private boolean isPIIName(String tbl, String col, Map<String, Object> row) {
        return isPII(tbl, col, row, PIIClass.FullName);
    }

    private boolean isPII(String tbl, String col, Map<String, Object> row, PIIClass piiClass) {
        var value = row.get(col).toString();
        var valueClass = anonymouse.classify(value, tbl, col);
        return valueClass.equals(piiClass);
    }
}

