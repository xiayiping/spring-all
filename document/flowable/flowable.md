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

# Dynamic setup assignee

In Flowable, you can dynamically set the assignee of a user task using expressions, process variables, or execution listeners. Here's how you can achieve this:

---

### **1. Using Expressions in the Assignee Field**
You can use expressions to dynamically assign a user task to a user. For example:

- In the Flowable Modeler, when defining a User Task, set the **Assignee** field to an expression like `${assignee}`.
- Before the task is reached in the process, ensure that the process variable `assignee` is set to the appropriate value. For example:

  ```java
  runtimeService.setVariable(executionId, "assignee", "john.doe");
  ```

This will assign the task to the user with the ID `john.doe`.

---

### **2. Using Execution Listeners**
You can use an **execution listener** to set the assignee dynamically when the task is created. Add an execution listener to the User Task with the following code:

#### Java Example:
```java
taskService.setAssignee(taskId, "john.doe");
```

You can also use a script if you're using a scripting language like Groovy or JavaScript in your BPMN model.

#### Groovy Example:
```groovy
task.assignee = execution.getVariable("assignee")
```

---

### **3. Using Process Variables**
You can set a process variable before the task is reached in the process. For example, in a service task (or any other task type), you can set the `assignee` variable:

```java
runtimeService.setVariable(executionId, "assignee", "john.doe");
```

Then, in the User Task, use `${assignee}` in the Assignee field as mentioned in **#1**.

---

### **4. Using Task Listeners**
Task listeners can also be used to assign tasks dynamically when the task is created. You can add a task listener in Flowable Modeler or in your BPMN XML configuration.

#### Example:
```xml
<taskListener event="create" class="com.example.AssignTaskListener" />
```
In the `AssignTaskListener` class:

```java
public class AssignTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        String assignee = (String) delegateTask.getVariable("assignee");
        delegateTask.setAssignee(assignee);
    }
}
```

---

### **5. Using Flowable Identity Service**
If the assignee is based on some dynamic logic, such as roles or groups, you can use Flowable's Identity Service to fetch users dynamically and assign them to tasks.

For example:
```java
List<User> users = identityService.createUserQuery().memberOfGroup("managers").list();
if (!users.isEmpty()) {
    taskService.setAssignee(taskId, users.get(0).getId());
}
```

---

### **Summary**
- Use `${variableName}` in the Assignee field to dynamically assign based on a process variable.
- Use execution or task listeners for more complex logic.
- Ensure that the required process variables are set before the task is reached.
- Optionally, use the Flowable Identity Service to fetch users dynamically based on roles or groups.

By combining these approaches, you can dynamically assign tasks in Flowable to meet your application's requirements.

# Useful documents

https://www.flowable.com/open-source/docs/userguide-5/index.html

https://github.com/flowable/flowable-engine/blob/main/modules/flowable-dmn-engine-configurator/src/test/java/org/flowable/dmn/test/runtime/DmnTaskTest.java
