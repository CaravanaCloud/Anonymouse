package cloud.caravana.anonymouse;

import java.util.logging.Logger;
import javax.sql.*;
import java.sql.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.sql.ResultSet.*;

@Component
public class Anonymouse {
    public static String ANON_PREFIX = "|#| ";

    private static Logger log = Logger.getLogger("anonymouse");

    @Autowired(required = false)
    private DataSource ds;

    @Autowired
    private Classifier cx;

    public Anonymouse(){}

    public Anonymouse(DataSource ds, String piiCols){
        this.ds = ds;
        this.cx = new Classifier(piiCols);
    }

    public void runTable(Connection conn, DatabaseMetaData dbmd, String tableName) throws Exception{
        String sql = "SELECT * FROM " + tableName;
        Statement statement = conn.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
        ResultSet rows = statement.executeQuery(sql);
        ResultSetMetaData rowsMD = rows.getMetaData();
        int colCnt = rowsMD.getColumnCount();
        int rowCnt = 0;
        while(rows.next()){
            rowCnt++;
            for(int col=1; col <= colCnt; col++){
                int columnType = rowsMD.getColumnType(col);
                if (isStringType(columnType)){
                    updateString(tableName, rowsMD, rows, rowCnt, col);
                }
            }
            rows.updateRow();
        }   
    }

    private void updateString(String tableName, ResultSetMetaData rowsMD, ResultSet rows, int row, int col)
        throws SQLException {
        String columnName = rowsMD.getColumnName(col);
        String columnValue = rows.getString(col);
        if ( cx.isInteresting(tableName, columnName, columnValue) ){
            String newValue = ANON_PREFIX + " " + columnName + " " + row;
            log.finer("Anonymoused ["+ tableName +"].["+ columnName +"] := ["+newValue+"]");
            rows.updateString(columnName, newValue);
        }
    }

    public static boolean isStringType(int columnType){
        return columnType == Types.VARCHAR;
    }

    public void runTables(Connection conn) throws Exception{
        DatabaseMetaData dbmd = conn.getMetaData();
        ResultSet rs = dbmd.getTables(null, null, null, null); 
        while(rs.next()) { 
            String tableCat = rs.getString("TABLE_CAT"); 
            String tableSchem = rs.getString("TABLE_SCHEM"); 
            String tableName = rs.getString("TABLE_NAME"); 
            String tableType = rs.getString("TABLE_TYPE"); 
            String typeCat = rs.getString("TYPE_CAT"); 
            String typeSchem = rs.getString("TYPE_SCHEM"); 
            String typeName = rs.getString("TYPE_NAME"); 
            String remarks = rs.getString("REMARKS"); 
            String selfRefColName = rs.getString("SELF_REFERENCING_COL_NAME");
            String refGen = rs.getString("REF_GENERATION");

            StringBuffer buff = new StringBuffer();
            buff.append(" TABLE_CAT="+tableCat);
            buff.append(" TABLE_SCHEM="+tableSchem);
            buff.append(" TABLE_NAME="+tableName);
            buff.append(" TABLE_TYPE="+tableType);
            buff.append(" TYPE_CAT="+typeCat);
            buff.append(" TYPE_SCHEM="+typeSchem);
            buff.append(" TYPE_NAME="+typeName);
            buff.append(" REMARKS="+remarks);
            buff.append(" SELF_REFERENCING_COL_NAME="+selfRefColName);
            buff.append(" REF_GENERATION="+refGen);
            log.info(buff.toString());

            boolean skip = "INFORMATION_SCHEMA".equalsIgnoreCase(tableSchem);
            skip |= "FLYWAY_SCHEMA_HISTORY".equalsIgnoreCase(tableName);
            if (! skip)
                runTable(conn, dbmd, tableName);
        }    
    }

    public void run(){
        if (ds == null) {
            log.warning("Can't connect without datasource");
        }else try (Connection conn = ds.getConnection()){
            runTables(conn);
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally{
            log.fine("Database anonymized");
        }
    }

    public boolean isAnonymized(String tableName, String colName, String colValue) {
        return ! cx.isInteresting(tableName,colName,colValue);
    }

    public static void main( String[] args ) throws Exception {
        //TODO
        new Anonymouse(null, null).run();
    }
}
