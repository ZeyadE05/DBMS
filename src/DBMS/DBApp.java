package DBMS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DBApp
{
	static int dataPageSize = 2;


	public static void createTable(String tableName, String[] columnsNames)
	{
		Table t = new Table(tableName, columnsNames);
		FileManager.storeTable(tableName, t);
	}

	public static void insert(String tableName, String[] record)
	{
		Table t = FileManager.loadTable(tableName);
		t.insert(record);
		FileManager.storeTable(tableName, t);

        int pageCount = t.getPageCount();
        for(int i = 0; i<record.length;i++){
            if(t.getIndexedColumns().contains(t.getColumnsNames()[i])){
                updateBitMapIndex(tableName, t.getColumnsNames()[i], record[i]);
            }
        }
	}

	public static ArrayList<String []> select(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
		ArrayList<String []> res = t.select();
		FileManager.storeTable(tableName, t);
		return res;
	}

	public static ArrayList<String []> select(String tableName, int pageNumber, int recordNumber)
	{
		Table t = FileManager.loadTable(tableName);
		ArrayList<String []> res = t.select(pageNumber, recordNumber);
		FileManager.storeTable(tableName, t);
		return res;
	}

	public static ArrayList<String []> select(String tableName, String[] cols, String[] vals)
	{
		Table t = FileManager.loadTable(tableName);
		ArrayList<String []> res = t.select(cols, vals);
		FileManager.storeTable(tableName, t);
		return res;
	}

	public static String getFullTrace(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
		String res = t.getFullTrace();
		return res;
	}

	public static String getLastTrace(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
		String res = t.getLastTrace();
		return res;
	}

    public static ArrayList<String[]> validateRecords(String tableName) {
        long startTime = System.currentTimeMillis();
        Table t = FileManager.loadTable(tableName);
        ArrayList<String[]> missingRecords = new ArrayList<>();

        int expectedPageCount = t.getPageCount();

        for (int i = 0; i < expectedPageCount; i++) {
            Page p = FileManager.loadTablePage(tableName, i);
            if (p == null) {
                for (int j = 0; j < dataPageSize; j++) {
                    missingRecords.add(new String[t.getColumnsNames().length]);
                }
            }
        }

        long endTime = System.currentTimeMillis();
        String trace = "Validating records, " + missingRecords.size() + " records missing.";
        t.addTraceColumn(trace);
        FileManager.storeTable(tableName, t);
        return missingRecords;
    }

    public static void recoverRecords(String tableName, ArrayList<String[]> missing) {
        Table t = FileManager.loadTable(tableName);
        ArrayList<Integer> recoveredPages = new ArrayList<>();

        int missingIndex = 0;
        for (int pageNum = 0; pageNum < t.getPageCount() && missingIndex < missing.size(); pageNum++) {
            Page existing = FileManager.loadTablePage(tableName, pageNum);
            if (existing == null) {
                Page p = new Page();
                for (int j = 0; j < dataPageSize && missingIndex < missing.size(); j++) {
                    p.insert(missing.get(missingIndex++));
                }
                FileManager.storeTablePage(tableName, pageNum, p);
                recoveredPages.add(pageNum);
            }
        }

        String trace = "Recovering " + missing.size() + " records in pages: " + recoveredPages;
        t.addTraceColumn(trace);
        FileManager.storeTable(tableName, t);
    }

    public static void createBitMapIndex(String tableName, String colName){
        Long startTime = System.currentTimeMillis();
        Table t = FileManager.loadTable(tableName);
        t.addIndexedColumn(colName);
        BitmapIndex BM = new BitmapIndex(tableName,colName);
        int colIndex = -1;
        for(int i = 0; i < t.getColumnsNames().length; i++){
            if(t.getColumnsNames()[i].equals(colName)){
                colIndex = i;
                break;
            }
        }
        int pageCount = t.getPageCount();
        int count = 0;
        for(int i = 0; i < pageCount; i++){
            Page p = FileManager.loadTablePage(tableName, i);
            ArrayList<String []> records = p.select();
            int recordCount = records.size();

            for(int j = 0; j < recordCount ; j++){
                String value = records.get(j)[colIndex];
                boolean found = false;
                for(String key: BM.getBitmap().keySet()){
                    String currentBits = BM.getBitmap().get(key);
                    if(value.equals(key)){
                        found = true;
                        BM.getBitmap().put(key, currentBits + "1");
                    }
                    else{
                        BM.getBitmap().put(key, currentBits + "0");
                    }
                }
                if(!found){

                    BM.getBitmap().put(value,  repeat("0",count)+ "1");
                }
                count++;
            }
        }
        Long endTime = System.currentTimeMillis();
        String trace = "Index created for column: " + colName + ", execution time (mil): " + (endTime - startTime);
        t.addTraceColumn(trace);
        FileManager.storeTableIndex(tableName, colName, BM);
        FileManager.storeTable(tableName, t);
    }

    public static void updateBitMapIndex(String tableName, String colName, String value){
        Table t = FileManager.loadTable(tableName);
        BitmapIndex BM = FileManager.loadTableIndex(tableName, colName);
        int colIndex = -1;
        for(int i = 0; i < t.getColumnsNames().length; i++){
            if(t.getColumnsNames()[i].equals(colName)){
                colIndex = i;
                break;
            }
        }

        int pageCount = t.getPageCount();
        Page p = FileManager.loadTablePage(tableName, pageCount-1);
        ArrayList<String []> records = p.select();
        int recordCount = records.size();
        boolean found = false;
        for(String key: BM.getBitmap().keySet()){
            String currentBits = BM.getBitmap().get(key);
            if(value.equals(key)){
                found = true;
                BM.getBitmap().put(key, currentBits + "1");
            }
            else{
                BM.getBitmap().put(key, currentBits + "0");
            }
        }
        if(!found){
            BM.getBitmap().put(value,  repeat("0",(pageCount-1)*dataPageSize + recordCount-1)+ "1");
        }
        FileManager.storeTableIndex(tableName, colName, BM);
    }

    public static String getValueBits(String tableName, String colName, String value) {
        Table t = FileManager.loadTable(tableName);
        BitmapIndex BM = FileManager.loadTableIndex(tableName, colName);
        String res = BM.getBitmap().get(value);
        if(res == null){
            return repeat("0",t.getRecordsCount());
        }
        return res;
    }

    public static ArrayList<String[]> selectIndex(String tableName, String[] cols, String[] vals) {
        Long startTime = System.currentTimeMillis();
        Table table = FileManager.loadTable(tableName);
        ArrayList<String> indexedCols = table.getIndexedColumns();
        ArrayList<String> indexedColsIntersection = new ArrayList<>();
        ArrayList<String> nonIndexedCols = new ArrayList<>();
        int indexedColCount = 0;
        for (int i = 0; i < cols.length; i++) {
            if (indexedCols.contains(cols[i])) {
                indexedColsIntersection.add(cols[i]);
                indexedColCount++;
            }
            else{
                nonIndexedCols.add(cols[i]);
            }
        }

        StringBuilder traceBuilder = new StringBuilder();
        traceBuilder.append("Select index condition: ").append(Arrays.toString(cols)).append("->").append(Arrays.toString(vals));

        if (indexedColCount > 0) {
            traceBuilder.append(", Indexed columns: ").append(Arrays.toString(indexedColsIntersection.toArray()));
        }

        ArrayList<String[]> resultRecords = new ArrayList<>();
        int indexedSelectionCount = 0;

        if (indexedColCount == cols.length) {
            String res = repeat("1",table.getRecordsCount());
            for (int i = 0; i < cols.length; i++) {
                BitmapIndex BM = FileManager.loadTableIndex(tableName, cols[i]);
                String index = BM.getBitmap().get(vals[i]);
                if (index == null) index = repeat("0",table.getRecordsCount());
                res = andStrings(res, index);
            }
            for (int i = 0; i < res.length(); i++) {
                if (res.charAt(i) == '1') {
                    int pageCount = i / dataPageSize;
                    int recordCount = i % dataPageSize;
                    resultRecords.addAll(table.select(pageCount, recordCount));
                }
            }
            indexedSelectionCount = resultRecords.size();
        } else if (indexedColCount > 0) {
            ArrayList<String> indexedColsList = new ArrayList<>();
            ArrayList<Integer> indexedColPosList = new ArrayList<>();
            for(int i = 0; i < cols.length; i++) {
                if(indexedCols.contains(cols[i])){
                    indexedColsList.add(cols[i]);
                    indexedColPosList.add(i);
                }
            }

            String res = repeat("1",table.getRecordsCount());
            for(int i = 0; i < indexedColsList.size(); i++){
                BitmapIndex BM = FileManager.loadTableIndex(tableName, indexedColsList.get(i));
                String index = BM.getBitmap().get(vals[indexedColPosList.get(i)]);
                if (index == null) index = repeat("0",table.getRecordsCount());
                res = andStrings(res, index);
            }

            for (int i = 0; i < res.length(); i++) {
                if (res.charAt(i) == '1') {
                    int pageCount = i / dataPageSize;
                    int recordCount = i % dataPageSize;
                    resultRecords.addAll(table.select(pageCount, recordCount));
                    indexedSelectionCount++;
                }
            }
            for (int i = 0; i < cols.length; i++) {
                if (!indexedColPosList.contains(i)) {
                    int pos = -1;
                    for(int j = 0; j< table.getColumnsNames().length; j++){
                        if(cols[i].equals(table.getColumnsNames()[j])){
                            pos = j;
                            break;
                        }
                    }
                    ArrayList<String[]> tempRecords = new ArrayList<>();
                    for (String[] record : resultRecords) {
                        if (record[pos].equals(vals[i])) {
                            tempRecords.add(record);
                        }
                    }
                    resultRecords = tempRecords;
                }
            }
        }
        else{
            resultRecords = table.select(cols, vals);
        }

        if (indexedColCount > 0) {
            traceBuilder.append(", Indexed selection count: ").append(indexedSelectionCount);
        }

        if (!nonIndexedCols.isEmpty()) {
            traceBuilder.append(", Non Indexed: ").append(Arrays.toString(nonIndexedCols.toArray()));
        }

        Long endTime = System.currentTimeMillis();
        traceBuilder.append(", Final count: ").append(resultRecords.size())
                .append(", execution time (mil): ").append(endTime - startTime);

        table.addTraceColumn(traceBuilder.toString());
        FileManager.storeTable(tableName, table);
        return resultRecords;
    }

    public static String andStrings(String s1, String s2) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < s1.length(); i++) {
            // If both bits are '1', the result bit is '1'
            if (s1.charAt(i) == '1' && s2.charAt(i) == '1') {
                result.append("1");
            } else {
                // Append '0' if one or both bits are '0'
                result.append("0");
            }
        }

        return result.toString();
    }

    public static String repeat(String str, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

}
