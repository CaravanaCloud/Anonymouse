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

    @Test
    public void testConnectivity() throws Exception {
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
        anonymouse.setPIIColumns("customer.cus_name");
        anonymouse.run();
        //then
        assertFalse(hasNamedCustomer());
    }

    private boolean hasNamedCustomer() {
        var col = "cus_name";
        var tbl = "CUSTOMER";
        var sql = format("SELECT %s FROM %s",col,tbl);
        var rows = jdbc.queryForList(sql).stream();
        return rows.anyMatch(row -> isPIIName(tbl,col,row));
    }

    private boolean isPIIName(String tbl, String col, Map<String, Object> row) {
        return ! anonymouse.isPIISafe(tbl, col, row.get(col).toString());
    }
}

