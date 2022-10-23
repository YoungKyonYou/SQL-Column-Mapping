public class ColumnEntity {
    private String ToBeLogicalColName;
    private String ToBePhysicalColName;
    private String AsIsLogicalColName;
    private String AsIsPhysicalColName;

    public String getToBeLogicalColName() {
        return ToBeLogicalColName;
    }

    public void setToBeLogicalColName(String toBeLogicalColName) {
        ToBeLogicalColName = toBeLogicalColName;
    }

    public String getToBePhysicalColName() {
        return ToBePhysicalColName;
    }

    public void setToBePhysicalColName(String toBePhysicalColName) {
        ToBePhysicalColName = toBePhysicalColName;
    }

    public String getAsIsLogicalColName() {
        return AsIsLogicalColName;
    }

    public void setAsIsLogicalColName(String asIsLogicalColName) {
        AsIsLogicalColName = asIsLogicalColName;
    }

    public String getAsIsPhysicalColName() {
        return AsIsPhysicalColName;
    }

    public void setAsIsPhysicalColName(String asIsPhysicalColName) {
        AsIsPhysicalColName = asIsPhysicalColName;
    }
}
