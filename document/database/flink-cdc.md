# What

Apache Flink CDC (Change Data Capture) is a framework that enables **real-time data ingestion and processing** of changes from databases into Flink applications. It is built on top of Apache Flink, a distributed stream processing framework, and is designed to capture and process incremental changes (inserts, updates, and deletes) from databases in a fault-tolerant and scalable way. Flink CDC is commonly used for real-time analytics, data replication, ETL (Extract, Transform, Load) pipelines, and maintaining materialized views.

### Key Features of Flink CDC
1. **CDC Streams**: Captures data changes (inserts, updates, deletes) from databases in real-time.
2. **Source Connectors**: Provides pre-built connectors to databases, such as:
    - MySQL
    - PostgreSQL
    - Oracle
    - MongoDB
    - SQL Server
3. **Integration with Flink**:
    - Seamlessly integrates with Flink's DataStream or Table/SQL APIs.
    - Enables processing of CDC events as part of Flink's stream processing pipelines.
4. **High Throughput and Scalability**: Processes large-scale data changes efficiently using Flink's distributed architecture.
5. **Exactly-Once Guarantees**: Ensures data consistency even in failure scenarios.
6. **Schema Evolution**: Handles changes in database schemas, such as adding or modifying columns.

### How Does Flink CDC Work?
Flink CDC uses **Debezium** (a CDC library) under the hood to capture database changes. Debezium connects to a database's replication logs (e.g., MySQL binlog, PostgreSQL WAL) and streams the changes to Flink. These changes are then processed within Flink pipelines in real-time.

1. **Source**: Flink CDC connects to a database using a CDC source connector. It reads changes from the database's replication mechanism (e.g., binlogs, WAL, etc.).
2. **Stream Processing**: The captured changes (insert, update, delete events) are streamed into Flink, where users can process, transform, or aggregate the data.
3. **Sink**: The processed data can be written to various sinks, such as a data warehouse, message queue (e.g., Kafka), or another database.

### Use Cases for Flink CDC
Flink CDC is particularly useful in scenarios where real-time synchronization and incremental data processing are required:

1. **Real-Time ETL Pipelines**:
    - Extract data changes from databases.
    - Transform and load the data into analytics systems like Apache Kafka, Apache Pinot, or Apache Druid.

2. **Database Replication**:
    - Keep multiple databases in sync.
    - Migrate data from one database system to another.

3. **Materialized Views**:
    - Maintain up-to-date materialized views in real-time for analytics or reporting.

4. **Microservices Communication**:
    - Sync data between microservices in a distributed system.

### Example Architecture
A typical Flink CDC pipeline might look like this:

1. **Source**: MySQL database (Flink CDC reads MySQL binlogs).
2. **Processing**: Apache Flink pipeline performs real-time transformations and aggregations.
3. **Sink**: Writes processed data to a target such as Apache Kafka, Elasticsearch, or a data warehouse.

### Flink CDC Example Code
Here is an example of how to use the MySQL CDC connector with Flink:

```java
import com.ververica.cdc.connectors.mysql.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FlinkCDCExample {
    public static void main(String[] args) throws Exception {
        // Create Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Create a MySQL CDC source
        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname("localhost")
                .port(3306)
                .databaseList("example_db") // Set database names
                .tableList("example_db.example_table") // Set table names
                .username("root")
                .password("password")
                .deserializer(new JsonDebeziumDeserializationSchema()) // Deserializes Debezium JSON events
                .startupOptions(StartupOptions.initial())
                .build();

        // Add the source to the Flink pipeline
        env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL CDC Source")
                .print(); // Print the captured changes to the console

        // Execute the Flink job
        env.execute("Flink CDC Example");
    }
}
```

### Advantages of Flink CDC
- **Real-Time Processing**: Enables real-time processing of database changes.
- **Unified Streaming and Batch**: Combines streaming and batch processing in Flink pipelines.
- **Fault Tolerant**: Built on Apache Flink's robust distributed processing engine.
- **Rich Ecosystem**: Supports integration with a wide variety of databases and sinks.
- **Exactly-Once Semantics**: Ensures data consistency and prevents duplicates.

### Supported Databases by Flink CDC
- MySQL
- PostgreSQL
- Oracle
- MongoDB
- SQL Server
- Others (via Debezium connectors)

### Conclusion
Flink CDC is a powerful tool for implementing real-time, incremental data pipelines with Apache Flink. By capturing database changes in real-time, Flink CDC enables users to build scalable, fault-tolerant, and low-latency ETL processes, making it an essential tool for modern data engineering.