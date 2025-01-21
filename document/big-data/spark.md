# Spark read / join data

When you use the `Spark` API, such as `spark.read().format("csv").load("data1.csv")`, there are several considerations regarding **where data is loaded, how it is processed, and where operations like `join` are executed**. Let’s break this down:

---

### **1. `spark.read()` Behavior: Data Loading**

- **Does it load all data into memory?**
    - No, by default, `spark.read` does not load all the data directly into memory at once.
    - Spark **lazily evaluates** transformations, meaning it doesn't immediately read the data from the CSV file into memory. Instead, it creates a **logical plan** representing the operation, and the actual data loading happens only when an **action** is triggered (e.g., `count()`, `show()`, `write()`).
    - When an action is triggered, Spark will read the data in **partitions** from the file system (e.g., HDFS, local disk, S3) and store it in **Spark executors' memory** or spill to disk if necessary.

- **Does it keep data in cache/buffer?**
    - By default, Spark does not cache the data after reading it. If you want to keep the data in memory for repeated operations, you need to explicitly call `.cache()` or `.persist()` on the `Dataset`:
      ```java
      Dataset<Row> dataset1 = spark.read().format("csv").load("data1.csv").cache();
      ```
        - With `.cache()`, Spark will store the data in memory across the cluster, and if memory is insufficient, it will spill to disk.
        - If you don’t cache, Spark will re-read the source data every time the `Dataset` is referenced in subsequent operations.

---

### **2. `Dataset.join`: Where Does the Join Happen?**

- **Does the join happen locally or on Spark servers?**
    - The `join` operation is executed **on the Spark cluster**, not on the local machine. The actual computation happens on the **Spark executors**, which are distributed across the cluster nodes. Even if you're running Spark locally (in `local[*]` mode), the join is still executed in parallel across the available CPU cores.

- **How does Spark process the join?**
    - When you perform `dataset1.join(dataset2, "id")`, Spark generates a **physical execution plan** for the join operation. This involves:
        - **Data shuffling**: If the datasets are not already partitioned on the join key, Spark will shuffle the data across the cluster to ensure that rows with the same join key are colocated on the same executor.
        - **Join execution**: After shuffling, Spark performs the join using one of its join algorithms (e.g., **Sort-Merge Join** for large datasets, or **Broadcast Hash Join** for cases where one dataset is small enough to fit in memory).

- **Is the data kept in memory during the join?**
    - Spark tries to keep intermediate data in memory during the join, but if the dataset is too large to fit in memory, Spark will spill the data to disk. This is controlled by Spark's memory management and shuffle configurations (e.g., `spark.memory.fraction`).

---

### **3. Behavior of `.except`**

- **Does `.except` load data into memory?**
    - Similar to `.join`, `.except` triggers a distributed computation across the cluster. Spark will shuffle the data to determine which rows are present in one dataset but not the other.
    - The data is processed in partitions, and Spark will use memory and spill to disk as needed. It does not load the entire dataset into memory at once.

---

### **4. Summary of Where Things Happen**

| **Operation**                              | **Where It Happens**                                                                                     | **Data in Memory**                                                                                                     |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| `spark.read.format("csv").load()`           | Data is read lazily from the file system (e.g., HDFS, local disk, S3).                                   | By default, data is not cached in memory; it is read in partitions as needed.                                          |
| `.join`                                     | Executed on the Spark cluster (executors); involves shuffling and distributed computation.               | Spark tries to use memory, but may spill to disk if data is too large.                                                 |
| `.except`                                   | Executed on the Spark cluster (executors); involves shuffling and distributed computation.               | Same as `.join`, Spark uses memory when available, but spills to disk if needed.                                       |
| `.cache()` or `.persist()`                  | Explicitly caches the data in memory (or disk if memory is insufficient).                                | Data is cached in memory across the cluster, reducing re-reading of source files in subsequent operations.             |

---

### **5. Performance Optimizations for Your Use Case**

To ensure optimal performance when working with large datasets like in your example:

1. **Use `.cache()` for Reused Data**:
    - If you plan to perform multiple operations (e.g., `join` and `except`) on the same dataset, cache it after reading:
      ```java
      Dataset<Row> dataset1 = spark.read().format("csv").load("data1.csv").cache();
      Dataset<Row> dataset2 = spark.read().format("csv").load("data2.csv").cache();
      ```
    - This avoids re-reading the data from the source for each operation.

2. **Broadcast Smaller Dataset for Joins**:
    - If one of the datasets is small enough to fit in memory on each executor, use a **Broadcast Hash Join** to avoid expensive shuffling:
      ```java
      Dataset<Row> joined = dataset1.join(functions.broadcast(dataset2), "id");
      ```

3. **Partitioning**:
    - If the datasets are already partitioned by the join key, Spark can avoid a shuffle. You can repartition the datasets before the join:
      ```java
      Dataset<Row> repartitioned1 = dataset1.repartition(col("id"));
      Dataset<Row> repartitioned2 = dataset2.repartition(col("id"));
      Dataset<Row> joined = repartitioned1.join(repartitioned2, "id");
      ```

4. **Increase Parallelism**:
    - Ensure your Spark cluster has enough partitions to parallelize the workload effectively. You can increase partitions when reading the data:
      ```java
      Dataset<Row> dataset1 = spark.read().format("csv").option("header", "true").load("data1.csv").repartition(200);
      Dataset<Row> dataset2 = spark.read().format("csv").option("header", "true").load("data2.csv").repartition(200);
      ```

5. **Tune Shuffle and Memory Configurations**:
    - Adjust Spark configurations to allow better shuffle performance and memory usage:
      ```bash
      spark.executor.memory=4g
      spark.executor.cores=4
      spark.sql.shuffle.partitions=200
      spark.memory.fraction=0.8
      ```

---

### **6. Final Notes**

- Spark is designed to handle distributed computation efficiently. Operations like `.join` and `.except` are executed on the cluster, with data stored in memory or spilled to disk as needed.
- To maximize performance, ensure that your data is well-partitioned, cache data when reused, and broadcast small datasets when joining.
- Be mindful of Spark's memory and shuffle configurations, especially for large datasets.

Let me know if you'd like further clarification or assistance with your Spark setup!