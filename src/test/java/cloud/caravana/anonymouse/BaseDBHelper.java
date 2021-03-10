package cloud.caravana.anonymouse;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Simple base class for test make no duplicate code for SQL executions.</p>
 *
 * This class is not usable for production test.
 *
 * @author Florian
 *
 * @version 1.0
 *
 */
public abstract class BaseDBHelper {

    @Autowired
    protected ApplicationContext context;

    protected Connection con;
    protected DataSource ds;
    
    /**
     * Open a connection to database for test execution statements
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {
        ds = (DataSource) context.getBean("dataSourceRef");
        con = ds.getConnection();
    }

    /**
     * Close the connection
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        if (con != null) {
            if (!con.isClosed()) {
                con.rollback();
                con.close();
            }
        }
        con = null;
    }

    /**
     * Simple counter query to have simple test inside test methods.
     *
     * @return number of customer in database
     * @throws Exception
     */
    public int countCustomer() throws Exception {
        int result = -1;

        try (Statement stmt = con.createStatement()) {
            String query = "select count(*) from Customer";

            try (ResultSet rs = stmt.executeQuery(query)) {
                rs.next();
                Long cnt = rs.getLong(1);
                result = cnt.intValue();
            }
        }

        return result;
    }

    protected BaseDBHelper() {
        super();
    }

}