import java.util.HashMap;

public class TableEntity {

    private String ToBeTableName;
    private String AsIsTableName;

    //key로 As-Is 컬럼 명 value로 As-Is 컬럼의 논리명과 물리명 그리고 To-Be 컬럼의 논리명과 물리명
    private HashMap<String, ColumnEntity> columnMappingMap=new HashMap<>();

    public HashMap<String, ColumnEntity> getColumnMappingMap() {
        return columnMappingMap;
    }

    public void insertMap(String key, ColumnEntity value){
        columnMappingMap.put(key, value);
    }

    public String getToBeTableName() {
        return ToBeTableName;
    }

    public void setToBeTableName(String toBeTableName) {
        ToBeTableName = toBeTableName;
    }

    public String getAsIsTableName() {
        return AsIsTableName;
    }

    public void setAsIsTableName(String asIsTableName) {
        AsIsTableName = asIsTableName;
    }



}
