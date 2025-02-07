

# db

## SqlServer

### Show active connections

```sql
SELECT
    s.session_id,
    s.login_name,
    s.host_name,
    s.program_name,
    r.status,
    r.command,
    r.database_id,
    r.start_time,
    r.cpu_time,
    r.total_elapsed_time
FROM
    sys.dm_exec_sessions s
LEFT JOIN
    sys.dm_exec_requests r
ON
    s.session_id = r.session_id
WHERE
    s.is_user_process = 1; 


SELECT
    spid,
    kpid,
    blocked,
    dbid,
    loginame,
    hostname,
    program_name,
    status,
    cmd,
    cpu,
    physical_io,
    memusage,
    login_time,
    last_batch,
    dbid,
    open_tran
FROM
    sys.sysprocesses
WHERE
    dbid = DB_ID('your_database_name');


SELECT
    c.session_id,
    c.connect_time,
    c.client_net_address,
    s.login_name,
    s.host_name,
    s.program_name,
    s.status,
    s.database_id
FROM
    sys.dm_exec_connections c
JOIN
    sys.dm_exec_sessions s
ON
    c.session_id = s.session_id
WHERE
    s.database_id = DB_ID('your_database_name');
```

# Isolation Level

Database **isolation levels** are a critical concept in database management systems (DBMS) that define the degree to which transactions are isolated from each other. They determine how data modifications made by one transaction are visible to other concurrent transactions, ensuring consistency and correctness in a multi-user environment.

The **SQL standard** defines four isolation levels, which strike a balance between data consistency and system performance. These levels address concurrency issues such as **dirty reads**, **non-repeatable reads**, and **phantom reads**.

---

## **1. Isolation Levels Overview**

| Isolation Level         | Dirty Reads  | Non-Repeatable Reads   | Phantom Reads   | Description                                                              |
|-------------------------|--------------|------------------------|-----------------|--------------------------------------------------------------------------|
| **Read Uncommitted**    | ✅ Allowed    | ✅ Allowed              | ✅ Allowed       | Transactions can see uncommitted changes made by others (low isolation). |
| **Read Committed**      | ❌ Prevented  | ✅ Allowed              | ✅ Allowed       | Transactions only see committed changes by others.                       |
| **Repeatable Read**     | ❌ Prevented  | ❌ Prevented            | ✅ Allowed       | Ensures the same data is read consistently within a transaction.         |
| **Serializable**        | ❌ Prevented  | ❌ Prevented            | ❌ Prevented     | Fully isolates transactions (highest isolation, but lowest performance). |

---

### **Concurrency Issues Addressed by Isolation Levels**
1. **Dirty Reads**:
    - A transaction reads uncommitted changes made by another transaction.
    - Example: Transaction A updates a row but hasn't committed, and Transaction B reads that uncommitted value.

2. **Non-Repeatable Reads**:
    - A transaction reads the same row twice and gets different results because another transaction modified (and committed) the row in between.
    - Example: Transaction A reads a value, Transaction B updates and commits the value, and Transaction A reads it again.

3. **Phantom Reads**:
    - A transaction reads a set of rows that match a condition, but another transaction inserts or deletes rows that affect the result during the transaction.
    - Example: Transaction A queries rows where `age > 30`, and Transaction B inserts a new row with `age = 35` during Transaction A.

---

## **2. Detailed Explanation of Isolation Levels**

### **a) Read Uncommitted**
- **Description**: The lowest level of isolation. Transactions can read uncommitted changes made by other transactions (dirty reads).
- **Use Case**: Rarely used, as it prioritizes performance over data consistency. Suitable for reporting or analytics where exact values are less critical.
- **Concurrency Issues**: Dirty reads, non-repeatable reads, and phantom reads are all possible.

#### Example:
```sql
Transaction A: UPDATE accounts SET balance = 500 WHERE id = 1;
Transaction B: SELECT balance FROM accounts WHERE id = 1;  -- Reads uncommitted value (500)
```

---

### **b) Read Committed**
- **Description**: Ensures that a transaction only sees committed changes made by other transactions. Dirty reads are prevented, but non-repeatable and phantom reads may occur.
- **Use Case**: Commonly used isolation level as it balances consistency and performance.
- **Concurrency Issues**: Non-repeatable reads and phantom reads are possible.

#### Example:
```sql
Transaction A: UPDATE accounts SET balance = 500 WHERE id = 1;
Transaction B: SELECT balance FROM accounts WHERE id = 1;  -- Waits until Transaction A commits
```

---

### **c) Repeatable Read**
- **Description**: Ensures that if a transaction reads a row twice, it will see the same data both times, even if another transaction modifies the data. Prevents dirty reads and non-repeatable reads.
- **Use Case**: Suitable for applications requiring consistent data during a transaction (e.g., financial systems).
- **Concurrency Issues**: Phantom reads are still possible.

#### Example:
```sql
Transaction A: SELECT balance FROM accounts WHERE id = 1;  -- Reads balance = 1000
Transaction B: UPDATE accounts SET balance = 500 WHERE id = 1;  -- Blocked until Transaction A finishes
Transaction A: SELECT balance FROM accounts WHERE id = 1;  -- Still sees balance = 1000
```

---

### **d) Serializable**
- **Description**: The highest level of isolation. Transactions are executed as if they were serialized (one after another). Prevents all concurrency issues, including phantom reads, by locking data or using advanced techniques like predicate locking.
- **Use Case**: Used when absolute correctness is required, but it comes with a performance tradeoff.
- **Concurrency Issues**: None.

#### Example:
```sql
Transaction A: SELECT * FROM accounts WHERE balance > 1000;  -- Locks rows
Transaction B: INSERT INTO accounts (id, balance) VALUES (3, 2000);  -- Blocked until Transaction A finishes
```

---

## **3. Configuring Isolation Levels**

### **a) In SQL**
You can explicitly set the isolation level for a transaction using the `SET TRANSACTION` statement.

#### Example:
```sql
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
BEGIN TRANSACTION;

-- Your SQL operations here

COMMIT;
```

---

### **b) In Spring Boot (With JPA)**

You can configure the isolation level programmatically using the `@Transactional` annotation in Spring.

#### Example:
```java
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

@Service
public class AccountService {

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void performTransaction() {
        // Your database operations here
    }
}
```

#### Available Isolation Levels in Spring:
- `Isolation.READ_UNCOMMITTED`
- `Isolation.READ_COMMITTED` (default)
- `Isolation.REPEATABLE_READ`
- `Isolation.SERIALIZABLE`

---

### **c) Database-Specific Isolation Levels**

#### PostgreSQL:
- Default is `READ COMMITTED`.
- Supports `READ UNCOMMITTED`, `READ COMMITTED`, `REPEATABLE READ`, and `SERIALIZABLE`.

#### MySQL:
- Default is `REPEATABLE READ`.
- Supports `READ UNCOMMITTED`, `READ COMMITTED`, `REPEATABLE READ`, and `SERIALIZABLE`.

#### SQL Server:
- Default is `READ COMMITTED`.
- Supports `READ UNCOMMITTED`, `READ COMMITTED`, `REPEATABLE READ`, `SERIALIZABLE`, and `SNAPSHOT`.

---

## **4. Choosing the Right Isolation Level**

| Use Case                                 | Recommended Isolation Level |
|------------------------------------------|-----------------------------|
| Analytics/Reporting (low consistency)    | Read Uncommitted            |
| General-purpose apps (balance)           | Read Committed              |
| Financial systems (higher consistency)   | Repeatable Read             |
| Critical systems (highest consistency)   | Serializable                |

---

### **5. Summary**
- Isolation levels control how transactions interact with each other.
- Higher isolation levels improve data consistency but reduce concurrency and performance.
- Use the appropriate level based on the application's requirements for consistency and performance.

Let me know if you'd like further clarification or examples! 😊

# Locks and Isolations

Yes, databases use **locks** (among other mechanisms) to help enforce isolation levels like **Repeatable Read** and **Read Committed**, but the type and scope of locking depend on the specific isolation level, database system, and implementation. Here's how locks are typically used to implement isolation levels:

---

## **1. How Locks Are Used for Isolation Levels**

### **a) Read Uncommitted**
- **Locking**:
   - Minimal or no locks are used for reads.
   - A transaction can read uncommitted changes made by other transactions.
- **Behavior**:
   - Dirty reads are allowed.
   - Data inconsistencies are possible as no strict locking mechanisms are applied.

---

### **b) Read Committed**
- **Locking**:
   - **Shared locks** are applied for reads (and released immediately after the read is completed).
   - **Exclusive locks** are applied for writes (and held until the transaction commits/rolls back).
- **Behavior**:
   - Prevents dirty reads (a transaction cannot read uncommitted changes).
   - Non-repeatable reads are possible because the shared locks for reads are released immediately, allowing other transactions to update the data.
- **Example**:
   - Transaction A reads a row.
   - Transaction B updates the same row and commits.
   - If Transaction A reads the row again, it may see the updated value (non-repeatable read).

---

### **c) Repeatable Read**
- **Locking**:
   - **Shared locks** are applied for reads and held until the transaction completes.
   - **Exclusive locks** are applied for writes and held until the transaction completes.
- **Behavior**:
   - Prevents dirty reads and non-repeatable reads.
   - Ensures that data read by a transaction remains consistent throughout the transaction.
   - **Phantom reads** are still possible because Repeatable Read does not lock ranges of rows (it only locks rows that are accessed explicitly).
- **Example**:
   - Transaction A reads a row and holds a shared lock.
   - Transaction B tries to update the same row but is blocked until Transaction A completes.

---

### **d) Serializable**
- **Locking**:
   - Implements the highest level of isolation using **range locks** or **predicate locks**.
   - Prevents dirty reads, non-repeatable reads, and phantom reads.
   - Transactions are executed as if they were serialized one after another.
- **Behavior**:
   - Range locks are applied to ensure no other transaction can insert, update, or delete rows that would affect the result of the current transaction.
- **Example**:
   - Transaction A queries rows where `age > 30` and locks the range of rows that satisfy the condition.
   - Transaction B tries to insert a row with `age = 35`, but it is blocked until Transaction A completes.

---

## **2. Lock Types Used in Databases**

### **a) Shared Locks (S)**
- Used for **reads**.
- Allows multiple transactions to read the same data simultaneously.
- Prevents other transactions from writing to the locked data.

### **b) Exclusive Locks (X)**
- Used for **writes**.
- Prevents other transactions from reading or writing to the locked data.

### **c) Intent Locks**
- Used to indicate a transaction's **intent** to place a shared or exclusive lock on a resource.
- Helps avoid deadlocks by making locking more predictable in hierarchical structures (e.g., table-level vs. row-level locks).

### **d) Range Locks**
- Used in **Serializable** isolation level to lock a range of rows (not just individual rows).
- Prevents phantom reads by blocking other transactions from inserting or deleting rows within the locked range.

---

## **3. Database-Specific Implementations**

### **a) MySQL (InnoDB)**
- **Read Committed**:
   - Uses shared and exclusive row-level locks.
   - Releases shared locks immediately after reading.
- **Repeatable Read**:
   - Default isolation level for InnoDB.
   - Uses row-level locks and the **MVCC** (Multi-Version Concurrency Control) mechanism to provide consistent reads without blocking writers.
   - Prevents non-repeatable reads but uses gap locks to prevent phantom reads.
- **Serializable**:
   - Uses range locks to prevent phantom reads.

---

### **b) PostgreSQL**
- Relies heavily on **MVCC** instead of traditional locking for reads.
- **Read Committed**:
   - Reads only committed data but does not block writers.
- **Repeatable Read**:
   - Creates a snapshot of the database at the start of the transaction.
   - Prevents dirty and non-repeatable reads but does not lock rows for phantom reads.
- **Serializable**:
   - Uses a combination of MVCC and predicate locking to prevent phantom reads.

---

### **c) SQL Server**
- Uses a combination of locks and **row versioning**:
- **Read Committed**:
   - Default isolation level. Shared locks are used for reads, and exclusive locks are used for writes.
- **Repeatable Read**:
   - Shared locks for reads are held until the transaction completes.
- **Serializable**:
   - Range locks are used to prevent phantom reads.

---

## **4. Locking vs. MVCC**

### **Locking**
- Traditional databases like SQL Server and MySQL rely heavily on locks to enforce isolation levels.
- Pros:
   - Easy to understand and implement.
   - Ensures strong consistency.
- Cons:
   - Can cause contention, blocking, and deadlocks in highly concurrent environments.

### **MVCC (Multi-Version Concurrency Control)**
- Used by databases like PostgreSQL and MySQL (InnoDB) to reduce locking contention.
- Instead of locking rows, MVCC creates multiple versions of data for each transaction.
- Readers get a snapshot of the database at the start of the transaction, ensuring consistent reads without blocking writers.
- Writers create new versions of rows rather than overwriting them, ensuring readers are not blocked.

---

## **5. Summary**

| Isolation Level       | Mechanism Used                             | Lock Types Used                  |
|-----------------------|--------------------------------------------|----------------------------------|
| **Read Uncommitted**  | No strict locking, minimal constraints     | None or minimal locks            |
| **Read Committed**    | Shared and exclusive locks                 | Shared (read), Exclusive (write) |
| **Repeatable Read**   | Shared locks for reads, MVCC for reads     | Shared, Exclusive, Gap Locks     |
| **Serializable**      | Range locks, predicate locks, or MVCC      | Range Locks, Serializable Locks  |

- **Databases use locks and/or MVCC** to enforce isolation levels.
- Higher isolation levels (like Serializable) require stricter locking or range-based mechanisms, which can reduce concurrency.
- Modern databases like PostgreSQL and MySQL (InnoDB) rely on MVCC to provide high concurrency without excessive locking.

Let me know if you'd like to dive deeper into a specific database or mechanism! 😊


# 6 Normal Forms

In database design, **normalization** is the process of organizing data to minimize redundancy and dependency. This is achieved by dividing a database into tables and defining relationships between them based on rules known as **normal forms (NF)**. There are **six normal forms (6NF)**, but in practice, only the first three or four are commonly used.

Below is an explanation of the **six normal forms (NF)** in database design with examples:

---

## **1. First Normal Form (1NF)**

### **Definition:**
A table is in **1NF** if:
1. All columns contain **atomic values** (i.e., no repeating groups or arrays).
2. Each row is uniquely identifiable (via a primary key).

### **Key Features:**
- No multivalued attributes (e.g., arrays or lists within a single column).
- Each cell contains a single value.

### **Example:**

**Not in 1NF (Repeating Groups):**
| StudentID | Name       | Courses        |
|-----------|------------|----------------|
| 1         | Alice      | Math, Science  |
| 2         | Bob        | English        |

**In 1NF (Atomic Values):**
| StudentID | Name       | Course         |
|-----------|------------|----------------|
| 1         | Alice      | Math           |
| 1         | Alice      | Science        |
| 2         | Bob        | English        |

### **Goal:**
Eliminate repeating groups. Ensure atomicity of data.

---

## **2. Second Normal Form (2NF)**

### **Definition:**
A table is in **2NF** if:
1. It is in **1NF**.
2. All non-primary key attributes are **fully functionally dependent** on the primary key (i.e., no partial dependency).

### **Key Features:**
- Partial dependencies occur when a non-primary key column is dependent on part of a composite key rather than the whole key. Remove these dependencies.

### **Example:**

**Not in 2NF (Partial Dependency):**
| StudentID | CourseID | StudentName | CourseName   |
|-----------|----------|-------------|--------------|
| 1         | 101      | Alice       | Math         |
| 1         | 102      | Alice       | Science      |

- Here, `StudentName` depends only on `StudentID`, not on the combination of `StudentID` and `CourseID`.

**In 2NF (Remove Partial Dependency):**
| StudentID | StudentName |
|-----------|-------------|
| 1         | Alice       |

| CourseID | CourseName   |
|----------|--------------|
| 101      | Math         |
| 102      | Science      |

| StudentID | CourseID |
|-----------|----------|
| 1         | 101      |
| 1         | 102      |

### **Goal:**
Eliminate partial dependencies by splitting tables.

---

## **3. Third Normal Form (3NF)**

### **Definition:**
A table is in **3NF** if:
1. It is in **2NF**.
2. There are no **transitive dependencies** (i.e., non-primary key attributes depend only on the primary key, not on other non-primary attributes).

### **Key Features:**
- Transitive dependency means that a non-key attribute depends on another non-key attribute.

### **Example:**

**Not in 3NF (Transitive Dependency):**
| StudentID | StudentName | DepartmentID | DepartmentName |
|-----------|-------------|--------------|----------------|
| 1         | Alice       | 10           | Math           |
| 2         | Bob         | 20           | Science        |

- Here, `DepartmentName` depends on `DepartmentID`, which depends on `StudentID`.

**In 3NF (Remove Transitive Dependency):**
| DepartmentID | DepartmentName |
|--------------|----------------|
| 10           | Math           |
| 20           | Science        |

| StudentID | StudentName | DepartmentID |
|-----------|-------------|--------------|
| 1         | Alice       | 10           |
| 2         | Bob         | 20           |

### **Goal:**
Eliminate transitive dependencies.

---

## **4. Boyce-Codd Normal Form (BCNF)**

### **Definition:**
A table is in **BCNF** if:
1. It is in **3NF**.
2. For every functional dependency `(X → Y)`, `X` is a superkey (i.e., `X` uniquely identifies a row).

### **Key Features:**
- BCNF is stricter than 3NF. It ensures there are no anomalies caused by non-superkey dependencies.

### **Example:**

**Not in BCNF:**
| CourseID | Instructor | Room   |
|----------|------------|--------|
| 101      | Smith      | Room1  |
| 102      | Smith      | Room2  |

- `Instructor → Room` is a dependency, but `Instructor` is not a superkey.

**In BCNF:**
| Instructor | Room   |
|------------|--------|
| Smith      | Room1  |

| CourseID | Instructor |
|----------|------------|
| 101      | Smith      |

### **Goal:**
Ensure all dependencies are on superkeys.

---

## **5. Fourth Normal Form (4NF)**

### **Definition:**
A table is in **4NF** if:
1. It is in **BCNF**.
2. It has no **multivalued dependencies**.

### **Key Features:**
- Multivalued dependency occurs when one attribute is dependent on another, independently of other attributes.

### **Example:**

**Not in 4NF:**
| StudentID | Course   | Hobby    |
|-----------|----------|----------|
| 1         | Math     | Chess    |
| 1         | Science  | Chess    |
| 1         | Math     | Painting |

- Here, `Course` and `Hobby` are independent.

**In 4NF (Separate Multivalued Dependencies):**
| StudentID | Course   |
|-----------|----------|
| 1         | Math     |
| 1         | Science  |

| StudentID | Hobby    |
|-----------|----------|
| 1         | Chess    |
| 1         | Painting |

### **Goal:**
Eliminate multivalued dependencies.

---

## **6. Fifth Normal Form (5NF)**

### **Definition:**
A table is in **5NF** if:
1. It is in **4NF**.
2. It has no **join dependencies** (i.e., tables cannot be decomposed further without losing data).

### **Key Features:**
- 5NF ensures that data is split into the smallest possible related tables.

### **Example:**

**Not in 5NF:**
| ProjectID | EmployeeID | Skill   |
|-----------|------------|---------|
| 1         | 101        | Java    |
| 1         | 101        | Python  |
| 2         | 102        | Python  |
| 2         | 102        | SQL     |

- This can be split into three tables without losing data.

**In 5NF (Decomposed):**
| ProjectID | EmployeeID |
|-----------|------------|
| 1         | 101        |
| 2         | 102        |

| EmployeeID | Skill   |
|------------|---------|
| 101        | Java    |
| 101        | Python  |
| 102        | Python  |
| 102        | SQL     |

| ProjectID | Skill   |
|-----------|---------|
| 1         | Java    |
| 1         | Python  |
| 2         | Python  |
| 2         | SQL     |

### **Goal:**
Eliminate join dependencies for full decomposition.

---

## **6th Normal Form (6NF)**

### **Definition:**
A table is in **6NF** if:
1. It is in **5NF**.
2. It has no **non-trivial join dependencies**, ensuring **irreducible relations**.

### **Key Features:**
- Rarely used in real-world applications.
- Mostly applicable to temporal databases (tracking changing data over time).

### **Example:**
Used in specialized scenarios like data warehousing or time-series data.

---

## **Summary of Normal Forms**

| **Normal Form**   | **Key Concept**                                | **Goal**                                |
|-------------------|------------------------------------------------|-----------------------------------------|
| **1NF**           | Atomic values, no repeating groups             | Ensure atomicity                        |
| **2NF**           | No partial dependency (on composite keys)      | Eliminate partial dependencies          |
| **3NF**           | No transitive dependency                       | Eliminate transitive dependencies       |
| **BCNF**          | Every determinant is a superkey                | Stricter version of 3NF                 |
| **4NF**           | No multivalued dependencies                    | Eliminate multivalued dependencies      |
| **5NF**           | No join dependencies                           | Complete decomposition                  |
| **6NF**           | Irreducible relations                          | Fully normalized temporal data          |

---

In practice, databases are often designed up to **3NF** or **BCNF**, as higher normal forms can lead to overly complex designs with diminishing returns. Let me know if you'd like clarification on any specific normal form! 😊