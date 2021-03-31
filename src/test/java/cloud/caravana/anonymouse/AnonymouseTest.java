package cloud.caravana.anonymouse;


import io.quarkus.test.junit.QuarkusTest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import static java.lang.String.*;
import static cloud.caravana.anonymouse.PIIClass.*;
import static org.assertj.core.api.Assertions.*;

@QuarkusTest
public class AnonymouseTest {

    private final Log logger = LogFactory.getLog(getClass());

    @Inject
    TestConfig db;

    @Inject
    DataSource datasource;


    @Inject
    Anonymouse anonymouse;

    private void loadTest(String migrationLoc) {
        db.migrate(migrationLoc);
        anonymouse.addConfig("classpath:/%s/pii_info.yaml".formatted(migrationLoc));
    }

    @Test
    public void testConnectivity() throws Exception {
        assertThat(datasource).isNotNull();
        assertThat(datasource.getConnection()).isNotNull();
    }

    @Test
    public void testInitialDataHasNames() {
        //when
        loadTest("anonName");
        //then
        assertThat(hasNamedCustomer()).isTrue();
    }

    @Test
    public void testAnonDataHasNoNames() {
        //given
        loadTest("anonName");
        //when
        anonymouse.run();
        //then
        assertThat(hasNamedCustomer()).isFalse();
    }

    @Test
    public void testInitialDataHasPhone() {
        //when
        loadTest("anonName");
        //then
        assertThat(hasPhonedCustomer()).isTrue();
    }

    @Test
    public void testAnonDataHasNoPhone() {
        //given
        loadTest("anonName");
        //when
        anonymouse.run();
        //then
        assertThat(hasPhonedCustomer()).isFalse();
    }

    private boolean hasPII(PIIClass piiClass, String tbl, String col) {
        var sql = format("SELECT %s FROM %s",col,tbl);
        var rows = queryForList(sql);
        boolean hasPhone = rows.stream().anyMatch(row -> isPII(tbl, col, row, piiClass));
        return hasPhone;
    }

    private List<String> queryForList(String sql) {
        var result = new ArrayList<String>();
        try(var conn = db.getDataSource().getConnection()){
            var rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) result.add(rs.getString(1));
        } catch (SQLException ex) {
            fail(ex.getMessage(),ex);
        }
        return result;
    }

    private boolean hasPhonedCustomer() {
        return hasPII(Telephone, "CUSTOMER","cus_phone");
    }

    private boolean hasNamedCustomer() {
        return hasPII(FullName, "CUSTOMER","cus_name");
    }

    private boolean isPIIName(String tbl, String col, String value) {
        return isPII(tbl, col, value, PIIClass.FullName);
    }

    private boolean isPII(String tbl, String col, String value, PIIClass piiClass) {
        var valueClass = anonymouse.classify(value, tbl, col);
        return valueClass.equals(piiClass);
    }
}

