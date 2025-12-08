To add a script to a task using Groovy in Flowable, you can use **Task Listeners**. Here's how:

## Task Listener with Groovy Script

```xml
<userTask id="myTask" name="My User Task">
  <extensionElements>
    <flowable:taskListener event="create" class="org.flowable.engine.impl.bpmn.listener.ScriptTaskListener">
      <flowable:field name="script">
        <flowable:string>
          <![CDATA[
            println "Task created: " + task.getName()
            task.setAssignee("john")
            task.setPriority(50)
          ]]>
        </flowable:string>
      </flowable:field>
      <flowable:field name="language">
        <flowable:string>groovy</flowable:string>
      </flowable:field>
    </flowable:taskListener>
  </extensionElements>
</userTask>
```

## Simpler Syntax (Flowable 6+)

```xml
<userTask id="myTask" name="My User Task">
  <extensionElements>
    <flowable:taskListener event="create" language="groovy">
      <flowable:script>
        <![CDATA[
          task.setAssignee("john")
          task.setPriority(50)
          execution.setVariable("taskCreatedTime", new Date())
        ]]>
      </flowable:script>
    </flowable:taskListener>
  </extensionElements>
</userTask>
```

## Available Events

You can trigger scripts on different task events:

**create** - when task is created:
```xml
<flowable:taskListener event="create" language="groovy">
  <flowable:script>
    <![CDATA[
      println "Task created: ${task.id}"
      task.setAssignee("manager")
    ]]>
  </flowable:script>
</flowable:taskListener>
```

**assignment** - when task is assigned:
```xml
<flowable:taskListener event="assignment" language="groovy">
  <flowable:script>
    <![CDATA[
      println "Task assigned to: ${task.assignee}"
      execution.setVariable("assignedTo", task.assignee)
    ]]>
  </flowable:script>
</flowable:taskListener>
```

**complete** - when task is completed:
```xml
<flowable:taskListener event="complete" language="groovy">
  <flowable:script>
    <![CDATA[
      println "Task completed by: ${task.assignee}"
      execution.setVariable("completedTime", new Date())
    ]]>
  </flowable:script>
</flowable:taskListener>
```

**delete** - when task is deleted:
```xml
<flowable:taskListener event="delete" language="groovy">
  <flowable:script>
    <![CDATA[
      println "Task deleted"
    ]]>
  </flowable:script>
</flowable:taskListener>
```

## Complete Example with Multiple Listeners

```xml
<userTask id="approvalTask" name="Approval Task">
  <extensionElements>
    <!-- On task creation -->
    <flowable:taskListener event="create" language="groovy">
      <flowable:script>
        <![CDATA[
          // Set initial assignee based on amount
          def amount = execution.getVariable("amount")
          if (amount > 10000) {
            task.setAssignee("seniorManager")
          } else {
            task.setAssignee("manager")
          }
          task.setPriority(50)
        ]]>
      </flowable:script>
    </flowable:taskListener>
    
    <!-- On task completion -->
    <flowable:taskListener event="complete" language="groovy">
      <flowable:script>
        <![CDATA[
          // Log completion
          def completedBy = task.assignee
          def completedAt = new Date()
          execution.setVariable("completedBy", completedBy)
          execution.setVariable("completedAt", completedAt)
          
          println "Task completed by ${completedBy} at ${completedAt}"
        ]]>
      </flowable:script>
    </flowable:taskListener>
  </extensionElements>
</userTask>
```

## Available Objects in Task Listener Scripts

- **task** - the current task instance
- **execution** - the execution context
- **taskService** - Flowable's task service
- **runtimeService** - Flowable's runtime service

## Common Operations

**Set assignee:**
```groovy
task.setAssignee("username")
```

**Set priority:**
```groovy
task.setPriority(100)
```

**Set due date:**
```groovy
task.setDueDate(new Date() + 7) // 7 days from now
```

**Add candidate users:**
```groovy
taskService.addCandidateUser(task.id, "user1")
```

**Set variables:**
```groovy
execution.setVariable("key", "value")
task.setVariableLocal("localKey", "localValue")
```

This should help you add Groovy scripts to your task listeners!