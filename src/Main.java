import DBMS.FileManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;

import static DBMS.DBApp.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
//    public static void main(String[] args) throws IOException {
//        // Initialize the file system/manager
//        FileManager.reset();
//
//        // Define table columns
//        String[] cols = {"id", "name", "major", "semester", "gpa"};
//        createTable("student", cols);
//
//        // Initial Insertions (Rows 1-3)
//        String[] r1 = {"1", "stud1", "CS", "5", "0.9"};
//        insert("student", r1);
//
//        String[] r2 = {"2", "stud2", "BI", "7", "1.2"};
//        insert("student", r2);
//
//        String[] r3 = {"3", "stud3", "CS", "2", "2.4"};
//        insert("student", r3);
//
//        // Create Bitmap Indexes
//        createBitMapIndex("student", "gpa");
//        createBitMapIndex("student", "major");
//
//        // Print initial index states
//        System.out.println("Bitmap of the value of CS from the major index: " +
//                getValueBits("student", "major", "CS"));
//        System.out.println("Bitmap of the value of 1.2 from the gpa index: " +
//                getValueBits("student", "gpa", "1.2"));
//
//        // New Insertions (Rows 4-5)
//        String[] r4 = {"4", "stud4", "CS", "9", "1.2"};
//        insert("student", r4);
//
//        String[] r5 = {"5", "stud5", "BI", "4", "3.5"};
//        insert("student", r5);
//
//        System.out.println("\nAfter new insertions:");
//        System.out.println("Bitmap of the value of CS from the major index: " +
//                getValueBits("student", "major", "CS"));
//        System.out.println("Bitmap of the value of 1.2 from the gpa index: " +
//                getValueBits("student", "gpa", "1.2"));
//
//        // --- Selection 1: All columns in condition are indexed ---
//        System.out.println("\nOutput of selection using index when all columns are indexed:");
//        ArrayList<String[]> result1 = selectIndex("student",
//                new String[]{"major", "gpa"},
//                new String[]{"CS", "1.2"});
//        printResults(result1);
//        System.out.println("Last trace of the table: " + getLastTrace("student"));
//        System.out.println("------------------------------------------------------------");
//
//        // --- Selection 2: Only one column in condition is indexed ---
//        System.out.println("Output of selection using index when only one column is indexed:");
//        ArrayList<String[]> result2 = selectIndex("student",
//                new String[]{"major", "semester"},
//                new String[]{"CS", "5"});
//        printResults(result2);
//        System.out.println("Last trace of the table: " + getLastTrace("student"));
//        System.out.println("------------------------------------------------------------");
//
//        // --- Selection 3: Mixed indexed and non-indexed columns ---
//        System.out.println("Output of selection using index when some columns are indexed:");
//        ArrayList<String[]> result3 = selectIndex("student",
//                new String[]{"major", "semester", "gpa"},
//                new String[]{"CS", "5", "0.9"});
//        printResults(result3);
//        System.out.println("Last trace of the table: " + getLastTrace("student"));
//        System.out.println("------------------------------------------------------------");
//
//        // Full system traces
//        System.out.println("Full Trace of the table:");
//        System.out.println(getFullTrace("student"));
//        System.out.println("------------------------------------------------------------");
//        System.out.println("The trace of the Tables Folder:");
//        System.out.println(FileManager.trace());
//
public static void main(String []args) throws IOException
{
    FileManager.reset();
    String[] cols = {"id","name","major","semester","gpa"};
    createTable("student", cols);
    String[] r1 = {"1", "stud1", "CS", "5", "0.9"};
    insert("student", r1);

    String[] r2 = {"2", "stud2", "BI", "7", "1.2"};
    insert("student", r2);

    String[] r3 = {"3", "stud3", "CS", "2", "2.4"};
    insert("student", r3);

    String[] r4 = {"4", "stud4", "CS", "9", "1.2"};
    insert("student", r4);

    String[] r5 = {"5", "stud5", "BI", "4", "3.5"};
    insert("student", r5);

    //////// This is the code used to delete pages from the table
    System.out.println("File Manager trace before deleting pages: "+
            FileManager.trace());
    String path = FileManager.class.getResource("FileManager.class").
            toString();
    File directory = new File(path.substring(6,path.length()-17) + File
            .separator

            + "Tables//student" + File.separator);
    File[] contents = directory.listFiles();
    int[] pageDel = {0,3};
    for(int i=0;i<pageDel.length;i++)
    {
        contents[pageDel[i]].delete();
    }
    ////////End of deleting pages code

    System.out.println("File Manager trace after deleting pages: "+
            FileManager.trace());
    ArrayList<String[]> tr = validateRecords("student");
    System.out.println("Missing records count: "/*+tr.size()*/);

    recoverRecords("student", tr);
    System.out.println("---------------------------------------------");
    System.out.println("Recovering the missing records.");
    tr = validateRecords("student");
    System.out.println("Missing record count: "/*+tr.size()*/);
    System.out.println("File Manager trace after recovering missing records: " + FileManager.trace());
    System.out.println("---------------------------------------------");
    System.out.println("Full trace of the table: ");
    System.out.println(/*getFullTrace("student")*/);
}
    /**
     * Helper method to print the ArrayList results clearly.
     */
    private static void printResults(ArrayList<String[]> results) {
        for (String[] row : results) {
            for (String cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }
}