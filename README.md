# DBMS — Java Page-Based Database Engine

A lightweight, file-backed relational database engine built in Java. It supports table management, paginated record storage, conditional querying, bitmap indexing, and operation tracing.

---

## Architecture Overview

```
DBApp (Public API)
    │
    ├── Table       — Schema, metadata, trace log, and query coordination
    ├── Page        — Fixed-size record containers (serialized to disk)
    ├── BitmapIndex — Column-level bitmap index for fast equality lookups
    └── FileManager — Serialization/deserialization layer (Tables, Pages, Indexes)
```

Data is persisted under a `Tables/` directory. Each table gets its own subdirectory containing:
- `<TableName>.db` — Serialized `Table` object
- `0.db`, `1.db`, ... — Serialized `Page` objects
- `<ColumnName>.db` — Serialized `BitmapIndex` objects (if indexed)

---

## Core Concepts

### Page Size
Controlled by `DBApp.dataPageSize` (default: `2` records per page). All insert and addressing logic derives from this constant.

### Record Addressing
Records are addressed by `(pageNumber, recordIndex)` pairs, where:
```
pageNumber  = globalRecordIndex / dataPageSize
recordIndex = globalRecordIndex % dataPageSize
```

### Bitmap Index
Each distinct value in an indexed column maps to a bitstring over all records — `1` at position `i` if record `i` holds that value, `0` otherwise. AND-ing multiple bitstrings enables multi-column indexed lookups.

---

## API Reference (`DBApp`)

### Table Operations

```java
// Create a new table with the given column names
DBApp.createTable(String tableName, String[] columnNames);

// Insert a record (values must align with columnNames order)
DBApp.insert(String tableName, String[] record);
```

### Select Operations

```java
// Select all records
ArrayList<String[]> DBApp.select(String tableName);

// Select a single record by page and record index
ArrayList<String[]> DBApp.select(String tableName, int pageNumber, int recordNumber);

// Select records matching column=value conditions (linear scan)
ArrayList<String[]> DBApp.select(String tableName, String[] cols, String[] vals);

// Select using bitmap indexes where available, falling back to linear scan
ArrayList<String[]> DBApp.selectIndex(String tableName, String[] cols, String[] vals);
```

### Bitmap Index Operations

```java
// Build a bitmap index on an existing column
DBApp.createBitMapIndex(String tableName, String colName);

// Get the bitstring for a specific value in an indexed column
String DBApp.getValueBits(String tableName, String colName, String value);
```

### Recovery Operations

```java
// Detect missing pages and return placeholder records
ArrayList<String[]> DBApp.validateRecords(String tableName);

// Re-insert recovered records into missing pages
DBApp.recoverRecords(String tableName, ArrayList<String[]> missing);
```

### Tracing

```java
// Returns full operation log for the table
String DBApp.getFullTrace(String tableName);

// Returns the most recent log entry
String DBApp.getLastTrace(String tableName);
```

---

## Selective Index Query Behavior (`selectIndex`)

`selectIndex` applies a hybrid strategy based on how many of the query columns are indexed:

| Scenario | Behavior |
|---|---|
| All columns indexed | AND all bitstrings → resolve record pointers |
| Some columns indexed | AND indexed bitstrings → filter candidates → linear scan remaining columns |
| No columns indexed | Full linear scan via `Table.select(cols, vals)` |

---

## Usage Example

```java
// Setup
DBApp.createTable("students", new String[]{"id", "major", "year"});

// Insert records
DBApp.insert("students", new String[]{"1", "CS", "3"});
DBApp.insert("students", new String[]{"2", "CS", "2"});
DBApp.insert("students", new String[]{"3", "EE", "3"});

// Build bitmap index on "major"
DBApp.createBitMapIndex("students", "major");

// Query using index
ArrayList<String[]> result = DBApp.selectIndex(
    "students",
    new String[]{"major"},
    new String[]{"CS"}
);

// Inspect operation trace
System.out.println(DBApp.getFullTrace("students"));
```

---

## FileManager

All I/O is handled through `FileManager` via Java object serialization. Key methods:

| Method | Description |
|---|---|
| `storeTable / loadTable` | Persist or retrieve a `Table` object |
| `storeTablePage / loadTablePage` | Persist or retrieve a `Page` by index |
| `storeTableIndex / loadTableIndex` | Persist or retrieve a `BitmapIndex` by column name |
| `reset()` | Wipe all stored data |
| `trace()` | Return a directory tree string of stored files |

The storage root is resolved relative to the compiled `FileManager.class` location.

---

## Limitations

- **No deletion or update support** — insert-only.
- **No type system** — all values are stored as `String`.
- **No primary key enforcement** — duplicates are allowed.
- **Bitmap indexes are not auto-updated on bulk loads** — call `createBitMapIndex` after bulk inserts, or use `insert` (which calls `updateBitMapIndex` automatically for indexed columns).
- **Fixed page size** — changing `dataPageSize` after data has been written will corrupt record addressing.
- **No concurrency control** — not safe for concurrent access.

---

## Project Structure

```
DBMS/
├── DBApp.java          — Public API and query engine
├── Table.java          — Table metadata and query coordination
├── Page.java           — Fixed-size record storage unit
├── BitmapIndex.java    — Bitmap index data structure
└── FileManager.java    — File serialization layer
```
