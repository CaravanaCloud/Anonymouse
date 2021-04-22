package cloud.caravana.anonymouse;


import static cloud.caravana.anonymouse.PIIClass.BirthDate;
import static cloud.caravana.anonymouse.PIIClass.FullName;
import static cloud.caravana.anonymouse.PIIClass.Telephone;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import cloud.caravana.anonymouse.classifier.CompositeClassifier;
import io.quarkus.test.junit.QuarkusTest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AnonymouseTest {

    private final Log logger = LogFactory.getLog(getClass());

    @Inject
    DataSource dataSource;

    @Inject
    TestConfig config;

    @Inject
    DataSource datasource;

    @Inject
    Anonymouse anonymouse;

    @Inject
    CompositeClassifier cx;

    private void loadTest(String migrationLoc) {
        config.migrate(migrationLoc);
        config.add("classpath:/%s/pii_info.yaml".formatted(migrationLoc));
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

    @Test
    public void testInitialDataHasBDay() {
        //when
        loadTest("anonName");
        //then
        assertThat(hasBDayCustomer()).isTrue();
    }

    @Test
    public void testAnonDataHasNoBDay() {
        //given
        loadTest("anonName");
        //when
        anonymouse.run();
        //then
        assertThat(hasBDayCustomer()).isFalse();
    }

    private boolean hasPII(PIIClass piiClass, String tbl, String col) {
        var sql = format("SELECT %s FROM %s", col, tbl);
        var rows = queryForList(sql);
        boolean hasPII = rows.stream()
                             .anyMatch(row -> isPII(tbl, col, row, piiClass));
        return hasPII;
    }

    private List<String> queryForList(String sql) {
        var result = new ArrayList<String>();
        try (var conn = datasource
                              .getConnection()) {
            var rs = conn.createStatement()
                         .executeQuery(sql);
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            fail(ex.getMessage(), ex);
        }
        logger.info(result.toString());
        return result;
    }

    private boolean hasPhonedCustomer() {
        return hasPII(Telephone, "CUSTOMER", "cus_phone");
    }

    private boolean hasBDayCustomer() {
        return hasPII(BirthDate, "CUSTOMER", "cus_bday");
    }

    private boolean hasNamedCustomer() {
        return hasPII(FullName, "CUSTOMER", "cus_name");
    }

    private boolean isPIIName(String tbl, String col, String value) {
        return isPII(tbl, col, value, PIIClass.FullName);
    }

    private boolean isPII(String tbl,
                          String col,
                          String value,
                          PIIClass piiClass) {
        var cn = cx.classify(value, tbl, col);
        boolean cnMatch = cn.isPresent() && cn.get()
                                              .piiClass()
                                              .equals(piiClass);
        return cnMatch;
    }
}

