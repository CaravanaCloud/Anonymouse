package cloud.caravana.anonymouse;

import javax.sql.*;
import java.sql.*;
import java.util.*;

public class Anonymouse {
    public static String ANON_PREFIX = "Anonymoused ";

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
            //Printing results
            System.out.println(columnName + "---" + datatype + "---" + columnsize + "---" + decimaldigits + "---" + isNullable + "---" + is_autoIncrment);
            if(isInteresting(tableName,columnName)){
                xcolumns.add(columnName);
            }
        }

        String sql = "SELECT * FROM " + tableName;
 
        Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        
        
        ResultSet rows = statement.executeQuery(sql);
        while(rows.next()){
            for(String columnName: xcolumns){
                System.out.println("! UPDATING DATA !");
                rows.updateString(columnName, anonName());
            }
            rows.updateRow();
        }   
    }

    public String anonName(){
        return ANON_PREFIX + UUID.randomUUID().toString();
    }
    
    //TODO: Read from cfg file?
    public boolean isInteresting(String tableName, String columnName){
        return "CUSTOMER".equalsIgnoreCase(tableName) && "cus_name".equalsIgnoreCase(columnName);
    }

    public boolean isInteresting(String tableName){
        return "CUSTOMER".equalsIgnoreCase(tableName); 
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
            String tableStr = String.format("tableCat=%s tableSchem=%s tableName=%s tableType=%s ", tableCat, tableSchem, tableName, tableType);
            //TODO: Log decente com todos dados
            System.out.println(tableStr);
            if (isInteresting(tableName)){
                runTable(conn, dbmd, tableName);
            }
            System.out.println("+-----------+");
        }    
    }

    public void run(DataSource ds) throws Exception{
        try(Connection conn = ds.getConnection()){
            runTables(conn);

            try(Statement stmt = conn.createStatement()){
                stmt.executeQuery("SELECT 1+2");
            }finally{
                System.out.println("Statement executed");
            }
            
        // Listar tables
          // varrer os registros
            // limpar as colunas necessarias
        } finally{
            System.out.println("Done");
            System.out.println("%%%%%%%");
        }
    }
}
