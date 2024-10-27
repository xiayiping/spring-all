In Spring Data JPA, transaction management is typically handled through the `@Transactional` annotation, which automatically commits or rolls back transactions based on the success or failure of the operations executed within the annotated method. However, there may be scenarios where you want more control over the transaction, such as adding a custom step during the transaction and deciding when to roll back.

Here’s how you can manually control the transaction and trigger a rollback in a customized way:

### Steps to Customize Transaction Rollback:

1. **Use the `@Transactional` annotation**: This will ensure that the method execution is wrapped in a transaction.

2. **Leverage the `TransactionStatus` and `PlatformTransactionManager`:** You can manually manage the transaction (commit or rollback) with these.

3. **Custom Rollback Logic**: You can use `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()` to programmatically mark the transaction for rollback if a condition is met.

### Example 1: Using `@Transactional` with Custom Rollback Logic

```java
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class MyService {

    @Transactional
    public void myTransactionalMethod() {
        try {
            // Step 1: Some database operation
            performDbOperation1();

            // Step 2: Custom logic
            if (someConditionFails()) {
                // Mark transaction for rollback
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;  // Stop further execution
            }

            // Step 3: Another database operation
            performDbOperation2();

        } catch (Exception e) {
            // Handle exception if needed
            throw new RuntimeException("Error during transaction", e);
        }
    }

    private void performDbOperation1() {
        // Perform some database operation
    }

    private void performDbOperation2() {
        // Perform another database operation
    }

    private boolean someConditionFails() {
        // Custom condition check that may cause rollback
        return true; // Simulating a failure condition
    }
}
```

In this example, the method `myTransactionalMethod` is annotated with `@Transactional`, so it’s wrapped in a transaction. If a certain condition fails (in `someConditionFails`), the transaction is marked for rollback using `setRollbackOnly()`.

### Example 2: Manual Transaction Management with `PlatformTransactionManager`

If you need more fine-grained control over the transaction, you can also manage the transaction programmatically by using `PlatformTransactionManager`.

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
public class MyService {

    @Autowired
    private PlatformTransactionManager transactionManager;

    public void myManualTransactionalMethod() {
        // Define the transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MyTransaction");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        // Start the transaction
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            // Step 1: Perform some database operation
            performDbOperation1();

            // Step 2: Custom logic
            if (someConditionFails()) {
                // Rollback manually
                transactionManager.rollback(status);
                return;  // Stop further execution
            }

            // Step 3: Perform another database operation
            performDbOperation2();

            // Commit the transaction
            transactionManager.commit(status);
        } catch (Exception e) {
            // Rollback in case of an exception
            transactionManager.rollback(status);
            throw new RuntimeException("Error during transaction", e);
        }
    }

    private void performDbOperation1() {
        // Perform some database operation
    }

    private void performDbOperation2() {
        // Perform another database operation
    }

    private boolean someConditionFails() {
        // Custom condition check that may cause rollback
        return true; // Simulating a failure condition
    }
}
```

In this example, we manually manage the transaction using `PlatformTransactionManager`. We control the transaction boundaries (begin, commit, and rollback) manually:

- We start a transaction using `transactionManager.getTransaction()`.
- We commit the transaction using `transactionManager.commit()`.
- If any condition fails or an exception occurs, we can roll back the transaction using `transactionManager.rollback()`.

### Key Points

1. **`@Transactional`**: Spring’s most common method for transaction management. Automatically handles rollback on exceptions, but you can manually mark the transaction for rollback with `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()`.

2. **Programmatic Transaction Management**: Using `PlatformTransactionManager` gives you full control over the transaction lifecycle but is less common because it’s more verbose and error-prone.

3. **Rollbacks**: Rollbacks can be triggered by exceptions (unchecked by default) or manually if a certain condition is met.

### Conclusion

Both methods are valid, and choosing one depends on your requirements. The `@Transactional` annotation is easier to use and provides automatic rollback behavior, but if you need more control or to perform custom logic at different points in your transaction, programmatic transaction management with `PlatformTransactionManager` might be more appropriate.


## TransactionTemplate

Spring provides a `TransactionTemplate` class that you can use for programmatic transaction management in Spring Boot. The `TransactionTemplate` is part of the Spring Framework and provides a convenient way to manage transactions programmatically without needing to explicitly manage transaction boundaries like commits and rollbacks. It simplifies the use of the `PlatformTransactionManager`.

### Key Advantages of `TransactionTemplate`:

- **Simplified syntax** for programmatic transaction management.
- **Automatic rollback** on exceptions, but you can customize the rollback behavior.
- You can **control the propagation and isolation levels** more easily than with manual transaction management.

### How to Use `TransactionTemplate`

#### Step 1: Add Dependencies
If you are using Spring Boot, the transaction management libraries are included by default when you use Spring Data JPA or Spring JDBC. However, make sure you have the following dependency in your `pom.xml` or `build.gradle`:

**Maven:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

**Gradle:**

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```

#### Step 2: Define `TransactionTemplate`

The `TransactionTemplate` can be injected into your service class and used to wrap your transactional code.

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class MyService {

    private final TransactionTemplate transactionTemplate;

    @Autowired
    public MyService(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void doTransactionalWork() {
        transactionTemplate.execute(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(TransactionStatus status) {
                try {
                    // Step 1: Perform some database operation
                    performDbOperation1();

                    // Step 2: Custom logic
                    if (someConditionFails()) {
                        // Mark the transaction for rollback
                        status.setRollbackOnly();
                        return null;  // Stop further execution
                    }

                    // Step 3: Perform another database operation
                    performDbOperation2();

                } catch (Exception e) {
                    // Transaction will be rolled back automatically if an exception is thrown
                    throw new RuntimeException("Error during transactional work", e);
                }

                return null; // Return null for void transaction
            }
        });
    }

    private void performDbOperation1() {
        // Perform some database operation
    }

    private void performDbOperation2() {
        // Perform another database operation
    }

    private boolean someConditionFails() {
        // Custom condition check that may cause rollback
        return true; // Simulating a failure condition
    }
}
```

#### Explanation:

1. **TransactionTemplate Injection**: The `TransactionTemplate` is injected into your service class via Spring’s Dependency Injection mechanism.

2. **`transactionTemplate.execute()`**: The `execute` method takes a `TransactionCallback` as a parameter. This callback contains the transactional code. If this method completes without an exception, the transaction will commit. If an exception is thrown, the transaction will be rolled back automatically.

3. **Manual Rollback**: You can manually mark the transaction for rollback by calling `status.setRollbackOnly()` inside the `doInTransaction` method.

4. **Transaction Status**: You can use the `TransactionStatus` object to check the transaction state or manually control when to roll back or commit.

#### Step 3: Customize Transaction Attributes

You can also customize the transaction propagation behavior, isolation level, or timeout via the `TransactionTemplate`. For example:

```java
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class MyService {

    private final TransactionTemplate transactionTemplate;

    public MyService(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;

        // Customize transaction attributes
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        this.transactionTemplate.setTimeout(30);  // 30 seconds timeout
    }

    public void doTransactionalWork() {
        transactionTemplate.execute(status -> {
            // Transactional code here (same as before)
            return null; // Return null for void transaction
        });
    }
}
```

You can customize the settings of the `TransactionTemplate`:

- **Propagation Behavior**: It controls how the transaction boundaries interact with other existing transactions. For example, `PROPAGATION_REQUIRED` means that the method will run inside an existing transaction if one exists, or start a new one if none exists.

- **Isolation Level**: This defines how the transaction is isolated from other transactions. For example, `ISOLATION_READ_COMMITTED` ensures that dirty reads are prevented.

- **Timeout**: This defines how long (in seconds) the transaction will wait before it times out.

### When to Use `TransactionTemplate`?

- **Programmatic Transaction Management**: When you need more control than the declarative `@Transactional` provides, such as when you want to use custom rollback conditions or execute multiple operations in a more controlled order.

- **Fine-grained Control**: When your transaction logic is complex and you need to perform multiple operations, conditionally committing or rolling back based on results.

### Example: Conditional Transaction Rollback

Here’s an example where you use `TransactionTemplate` to roll back the transaction based on a custom condition:

```java
@Service
public class MyService {

    private final TransactionTemplate transactionTemplate;

    @Autowired
    public MyService(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void doTransactionalWork() {
        transactionTemplate.execute(status -> {
            try {
                // Step 1: Perform some database operation
                performDbOperation1();

                // Step 2: Check custom rollback condition
                if (someConditionFails()) {
                    // Manually mark the transaction for rollback
                    status.setRollbackOnly();
                }

                // Step 3: Perform another database operation
                performDbOperation2();

            } catch (Exception e) {
                // Transaction will automatically rollback on exception
                throw new RuntimeException(e);
            }

            return null;
        });
    }

    private void performDbOperation1() {
        // Perform some database operation
    }

    private void performDbOperation2() {
        // Perform another database operation
    }

    private boolean someConditionFails() {
        // Custom condition check that may cause rollback
        return false; // Simulate condition for rollback
    }
}
```

### Conclusion

- **`TransactionTemplate`** is a powerful utility that allows you to programmatically manage transactions in Spring Boot.
- It simplifies the transaction management process by handling commits and rollbacks automatically based on exceptions or custom conditions.
- You can also customize attributes like propagation, isolation, and timeout, giving you full control over the transaction lifecycle.

For simple cases, using `@Transactional` may be sufficient, but when you require more control, `TransactionTemplate` is a great option.