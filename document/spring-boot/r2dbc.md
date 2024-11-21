## Get Started 

https://docs.spring.io/spring-data/relational/reference/r2dbc/getting-started.html

## ConnectionFactory

The **R2DBC `ConnectionFactory`** is not inherently a connection pool. It is simply an API and implementation responsible for creating database connections. Whether or not connections are pooled depends on the specific implementation of the `ConnectionFactory` you are using.

### Key Points:
1. **Raw ConnectionFactory**:
    - A raw `ConnectionFactory` creates a new connection to the database each time it is called.
    - This behavior is not optimal for applications that need to reuse connections for better performance.

2. **Pooled ConnectionFactory**:
    - To enable connection pooling, you need to wrap your `ConnectionFactory` with a pooling library like **Reactor Pool** (commonly used with R2DBC).
    - Libraries such as **r2dbc-pool** provide a `ConnectionPool` implementation that uses a `ConnectionFactory` internally but manages a pool of reusable connections.

### Example: Wrapping a ConnectionFactory with a Connection Pool
Here’s how you can use a pooled `ConnectionFactory` in R2DBC:

```java
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;

public class R2dbcExample {
    public static void main(String[] args) {
        // Create a raw ConnectionFactory (non-pooled)
        ConnectionFactory rawFactory = new PostgresqlConnectionFactory(
            PostgresqlConnectionConfiguration.builder()
                .host("localhost")
                .port(5432)
                .database("mydb")
                .username("myuser")
                .password("mypassword")
                .build()
        );

        // Wrap the raw ConnectionFactory with a ConnectionPool
        ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder(rawFactory)
            .maxIdleTime(Duration.ofMinutes(30)) // Maximum idle time for a connection
            .initialSize(10)                     // Initial number of connections in the pool
            .maxSize(20)                         // Maximum number of connections in the pool
            .build();

        ConnectionPool connectionPool = new ConnectionPool(poolConfig);

        // Use connectionPool as your ConnectionFactory
    }
}
```

### Summary:
- A **raw `ConnectionFactory`** is not a pooled connection provider.
- If you need connection pooling, you must use a library like **r2dbc-pool** or configure a `ConnectionPool` explicitly.
- Libraries like **Spring Data R2DBC** can integrate with a `ConnectionPool` seamlessly, so you get pooled connections automatically when configured properly.

## Transaction

https://spring.io/blog/2019/05/16/reactive-transactions-with-spring

```java
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public static void main(String[] args) {
    // Connection Factory (e.g., R2DBC Connection Factory)
    ConnectionFactory connectionFactory = null;

    // Create a ReactiveTransactionManager
    R2dbcTransactionManager transactionManager = new R2dbcTransactionManager(connectionFactory);

    // Define Transaction Properties
    DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    transactionDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);

    // Create a TransactionalOperator with the TransactionDefinition
    TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager, transactionDefinition);

    // Use the TransactionalOperator in Reactive Code
    Mono<Void> transactionalMono = transactionalOperator.execute(transactionStatus -> {
        // Your transactional work here
        return performDatabaseOperations();
    });

    transactionalMono.subscribe();
}
```