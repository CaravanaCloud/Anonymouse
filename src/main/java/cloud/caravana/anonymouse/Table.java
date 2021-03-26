package cloud.caravana.anonymouse;

public record Table(
    String tableCat,
    String tableSchem,
    String tableName,
    String tableType,
    String typeCat,
    String typeSchem,
    String typeName,
    String remarks,
    String selfRefColName,
    String refGen) { }
