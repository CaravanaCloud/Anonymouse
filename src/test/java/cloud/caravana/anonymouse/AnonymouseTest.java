package cloud.caravana.anonymouse;


import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import cloud.caravana.anonymouse.classifier.Classifiers;
import cloud.caravana.anonymouse.classifier.NameClassifier;
import cloud.caravana.anonymouse.classifier.PhoneClassifier;

import java.sql.SQLException;
import javax.inject.Inject;
import javax.sql.DataSource;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AnonymouseTest {

    private final Log logger = LogFactory.getLog(getClass());

    @Inject
    DataSource dataSource;

    @Inject
    Configuration config;

    @Inject
    Migrations migrations;

    @Inject
    DataSource datasource;

    @Inject
    Anonymouse anonymouse;

    @Inject
    Classifiers cx;

    private void loadTest(String migrationLoc) {
        migrations.migrate(migrationLoc);
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



    private boolean hasPhonedCustomer() {
        var sql = """
        SELECT * FROM CUSTOMER
        WHERE cus_phone NOT LIKE '%s%%' 
        """.formatted(PhoneClassifier.PREFIX);
        return ! isEmpty(sql);
    }

    private boolean hasBDayCustomer() {
        var sql = """
        SELECT * FROM CUSTOMER
        WHERE cus_bday > '1900-01-01' 
        """.formatted(PhoneClassifier.PREFIX);
        return ! isEmpty(sql);
    }

    private boolean hasNamedCustomer() {
        var sql = """
        SELECT * FROM CUSTOMER
        WHERE cus_name NOT LIKE '%s%%' 
        """.formatted(NameClassifier.PREFIX);
        return ! isEmpty(sql);
    }

    private boolean isEmpty(String sql) {
        try(var conn = datasource.getConnection();
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(sql) ){
            if (rs.next()){
                var name = rs.getString("cus_name");
                var phone = rs.getString("cus_phone");
                logger.info("Matched "+name+" "+phone);
                return false;
            }
        } catch (SQLException ex) {
            fail(ex.getMessage(),ex);
        }
        return true;
    }




}

