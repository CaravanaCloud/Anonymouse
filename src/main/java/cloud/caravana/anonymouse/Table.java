package cloud.caravana.anonymouse;

import java.sql.ResultSet;
import java.sql.SQLException;

public record Table (
    String tableCat,
    String tableSchem,
    String tableName,
    String tableType,
    String typeCat,
    String typeSchem,
    String typeName,
    String remarks,
    String selfRefColName,
    String refGen) {

    public static Table of(ResultSet rs)
        throws SQLException {
        Table table = new Table(
            rs.getString("TABLE_CAT"),
            rs.getString("TABLE_SCHEM"),
            rs.getString("TABLE_NAME"),
            rs.getString("TABLE_TYPE"),
            rs.getString("TYPE_CAT"),
            rs.getString("TYPE_SCHEM"),
            rs.getString("TYPE_NAME"),
            rs.getString("REMARKS"),
            rs.getString("SELF_REFERENCING_COL_NAME"),
            rs.getString("REF_GENERATION"));
        return table;
    }
}
