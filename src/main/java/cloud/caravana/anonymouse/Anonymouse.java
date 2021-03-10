package cloud.caravana.anonymouse;

import java.util.logging.Logger;
import javax.sql.*;
import java.sql.*;
import java.util.*;
import static java.sql.ResultSet.*;

public class Anonymouse {
    private static Logger log = Logger.getLogger("anonymouse");

    Classifier cx = new Classifier();
    Generator gn = new Generator();

    public void runTable(Connection conn, DatabaseMetaData dbmd, String tableName) throws Exception{
        ResultSet columns = dbmd.getColumns(null,null, tableName, null);
        List<String> xcolumns = new ArrayList<>();
        while(columns.next())
        {
            String columnName = columns.getString("COLUMN_NAME");
            String datatype = columns.getString("DATA_TYPE");
            String columnsize = columns.getString("COLUMN_SIZE");
            String decimaldigits = columns.getString("DECIMAL_DIGITS");
            String isNullable = columns.getString("IS_NULLABLE");
            String is_autoIncrment = columns.getString("IS_AUTOINCREMENT");
            Boolean interesting = cx.isInteresting(tableName, columnName);

            StringBuffer buff = new StringBuffer();
            buff.append("INTERESTING="+interesting);
            buff.append(" COLUMN_NAME="+columnName);
            buff.append(" DATA_TYPE="+datatype);
            buff.append(" COLUMN_SIZE="+columnsize);
            buff.append(" DECIMAL_DIGITS="+decimaldigits);
            buff.append(" IS_NULLABLE="+isNullable);
            buff.append(" IS_AUTOINCREMENT="+is_autoIncrment);
            log.info(buff.toString());

            if(interesting){
                xcolumns.add(columnName);
            }
        }

        String sql = "SELECT * FROM " + tableName;
        Statement statement = conn.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
        ResultSet rows = statement.executeQuery(sql);
        while(rows.next()){
            for(String columnName: xcolumns){
                String newValue = gn.anonName();
                log.finer("Anonymoused ["+tableName+"].["+columnName+"] := ["+newValue+"]");
                rows.updateString(columnName, newValue);
            }
            rows.updateRow();
        }   
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
            Boolean isInteresting = cx.isInteresting(tableName);

            StringBuffer buff = new StringBuffer();
            buff.append("INTERESTING="+isInteresting);
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

            if (isInteresting){
                runTable(conn, dbmd, tableName);
            }
        }    
    }

    public void run(DataSource ds) throws Exception{
        try(Connection conn = ds.getConnection()){
            runTables(conn);
        } finally{
            log.fine("Database anonymized");
        }
    }
}
