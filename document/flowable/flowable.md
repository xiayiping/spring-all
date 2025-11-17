# Task

When a process instance reaches a **user input activity** (typically a **user task** in a BPMN model), you can dynamically input variables using the `TaskService`. This allows you to complete the user task and provide additional variables at runtime.

### Key Concepts:

1. **Task Variables vs. Process Variables**:
    - **Task Variables**: Variables scoped to a specific task.
    - **Process Variables**: Variables scoped to the entire process instance.
    - When you provide variables while completing a task, those variables are merged into the **process instance variables** by default.

2. **Merging Variables**:
    - If you input variables when completing a user task, they are **merged** with the existing process instance variables.
    - If a variable with the same name already exists in the process instance, the new value will overwrite the existing value.

---

### Steps to Dynamically Input Variables for a User Task

1. **Query the Active User Task**:
   Use the `TaskService` to query the active user task for the process instance.

2. **Complete the User Task with Variables**:
   Use the `complete` method of `TaskService` to dynamically provide input variables when completing the task.

---

### Example Code

#### 1. **Query Active User Task**

Use the `TaskService` to find the active user task for a process instance:

```java
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;

import java.util.List;

public class UserTaskHandler {

    private final TaskService taskService;

    public UserTaskHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    public Task getActiveUserTask(String processInstanceId) {
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId) // Find tasks for the given process instance
                .active() // Only active tasks
                .list();

        if (tasks.isEmpty()) {
            throw new IllegalStateException("No active tasks found for process instance ID: " + processInstanceId);
        }

        // Return the first active task (assuming a single active task)
        return tasks.get(0);
    }
}
```

---

#### 2. **Complete the User Task with Variables**

Use the `complete` method of `TaskService` to input variables dynamically when completing the task:

```java
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;

import java.util.HashMap;
import java.util.Map;

public class UserTaskCompleter {

    private final TaskService taskService;

    public UserTaskCompleter(TaskService taskService) {
        this.taskService = taskService;
    }

    public void completeTaskWithVariables(String taskId, Map<String, Object> inputVariables) {
        // Complete the task and pass the variables
        taskService.complete(taskId, inputVariables);

        System.out.println("Task " + taskId + " completed with variables: " + inputVariables);
    }
}
```

---

#### 3. **Full Example: Query and Complete a User Task**

Here’s how you can query the active task and complete it dynamically:

```java
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;

import java.util.HashMap;
import java.util.Map;

public class UserTaskExample {

    private final TaskService taskService;

    public UserTaskExample(TaskService taskService) {
        this.taskService = taskService;
    }

    public void handleUserTask(String processInstanceId) {
        // Step 1: Query the active user task
        Task activeTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .singleResult();

        if (activeTask == null) {
            System.out.println("No active user task found for process instance ID: " + processInstanceId);
            return;
        }

        System.out.println("Active task found: " + activeTask.getName() + " (ID: " + activeTask.getId() + ")");

        // Step 2: Dynamically input variables and complete the task
        Map<String, Object> inputVariables = new HashMap<>();
        inputVariables.put("approval", "approved");
        inputVariables.put("comments", "Task completed successfully");

        taskService.complete(activeTask.getId(), inputVariables);

        System.out.println("Task " + activeTask.getId() + " completed with variables: " + inputVariables);
    }
}
```

---

### 4. **Verify Process Variables**

After completing the task, you can verify that the input variables were merged with the process instance variables:

```java
import org.flowable.engine.RuntimeService;

import java.util.Map;

public class ProcessVariableChecker {

    private final RuntimeService runtimeService;

    public ProcessVariableChecker(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public void checkProcessVariables(String processInstanceId) {
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);

        System.out.println("Process variables after task completion: " + variables);
    }
}
```

---

### What Happens to Variables?

1. **New Variables**:
    - If the input variables provided during task completion include new variables (not present in the process instance), they will be **added** to the process instance.

2. **Existing Variables**:
    - If the input variables include variables that already exist in the process instance, their values will **overwrite** the existing ones.

3. **Task-Scoped Variables**:
    - If you want variables to be scoped to the task itself (and not saved to the process instance), use the `setVariableLocal` method before completing the task.

---

### Example: Adding Task-Scoped Variables (Optional)

If you want variables to be task-scoped (only available for the current task and not merged into the process instance), you can set them as **local variables**:

```java
taskService.setVariableLocal(taskId, "taskScopedVariable", "someValue");
taskService.complete(taskId);
```

Task-scoped variables won't be merged with the process instance variables.

---

### Final Workflow

1. **Query Active User Task**:
    - Use `TaskService.createTaskQuery()` to find the active user task.

2. **Complete the Task with Variables**:
    - Use `taskService.complete(taskId, variables)` to dynamically input variables.

3. **Process Variables Merge**:
    - The input variables will automatically be merged into the process instance variables.

4. **Verify Variables**:
    - Use `runtimeService.getVariables(processInstanceId)` to check the merged variables.

---

### Summary

- To dynamically input variables into a user task:
    1. Query the active user task using `TaskService.createTaskQuery()`.
    2. Complete the task using `TaskService.complete(taskId, variables)` and provide the variables.
- The input variables will **merge into the process instance variables** unless explicitly set as task-local variables.
- Existing variables in the process instance with the same name will be **overwritten** by the input variables.

Let me know if you need further clarification! 🚀

# Useful documents

https://www.flowable.com/open-source/docs/userguide-5/index.html

https://github.com/flowable/flowable-engine/blob/main/modules/flowable-dmn-engine-configurator/src/test/java/org/flowable/dmn/test/runtime/DmnTaskTest.java


# spring bean

To get a Spring bean from a Java delegate in a Flowable BPMN process within a Spring Boot application, you can use the `ApplicationContext` provided by Spring. Here is a step-by-step guide:

---

### 1. **Inject Spring Beans into Java Delegates:**
If your Java delegate is a Spring-managed bean (annotated with `@Component`), you can directly inject other Spring beans into it using `@Autowired`.

Example:

```java
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyDelegate implements JavaDelegate {

    @Autowired
    private MyService myService; // Inject your Spring bean here

    @Override
    public void execute(DelegateExecution execution) {
        // Use the injected service
        myService.performTask();
    }
}
```

---

### 2. **Manually Retrieve a Bean from ApplicationContext:**
If your delegate is not a Spring-managed bean, you can get the `ApplicationContext` and retrieve the bean manually.

#### Option 1: Use `@Autowired` to Inject `ApplicationContext`

You can inject `ApplicationContext` into the class and retrieve beans from it.

```java
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MyDelegate implements JavaDelegate {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void execute(DelegateExecution execution) {
        // Retrieve the Spring bean dynamically
        MyService myService = applicationContext.getBean(MyService.class);
        myService.performTask();
    }
}
```

#### Option 2: Use `ContextAware` Classes to Get `ApplicationContext`

If you cannot inject `ApplicationContext` directly, you can implement a utility class to hold the context.

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}
```

Now, retrieve beans in your delegate:

```java
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class MyDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // Use SpringContextHolder to get the bean
        MyService myService = SpringContextHolder.getBean(MyService.class);
        myService.performTask();
    }
}
```

---

### 3. **Register Java Delegates as Spring Beans in Flowable:**
To ensure your Java delegate is properly managed by Spring, you can register it in the Flowable process by using the `@Component` annotation or explicitly in the BPMN XML.

#### Option 1: Use `@Component`
Annotate your delegate with `@Component` and reference it in the BPMN process using its Spring bean name.

```java
@Component("myDelegate")
public class MyDelegate implements JavaDelegate {
    // Implementation
}
```

In your BPMN XML:

```xml
<serviceTask id="myTask" name="My Task" flowable:delegateExpression="${myDelegate}" />
```

#### Option 2: Explicitly Define Beans in BPMN
If you’re not using `@Component`, you can reference the delegate as a bean in the BPMN file using the `delegateExpression` attribute.

---

### Summary
- **Preferred Option**: Annotate your delegate with `@Component` and inject dependencies using `@Autowired`.
- **Fallback Option**: Use `ApplicationContext` to retrieve beans manually when the delegate is not Spring-managed.
  
Using Spring's dependency injection ensures your Flowable delegates integrate seamlessly with Spring Boot.