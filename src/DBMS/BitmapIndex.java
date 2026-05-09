package DBMS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class BitmapIndex implements Serializable {
    private String tableName;
    private String colName;
    private HashMap<String,String> bitmap;

    public BitmapIndex(String tableName, String colName) {
        this.tableName = tableName;
        this.colName = colName;
        bitmap = new HashMap<>();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public HashMap<String, String> getBitmap() {
        return bitmap;
    }

    public void setBitmaps(HashMap<String, String> bitmap) {
        this.bitmap = bitmap;
    }
}
