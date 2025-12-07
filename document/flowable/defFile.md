# create process variable

Here are several ways to define process-level variables in BPMN 2.0 XML that will be initialized when a process instance is created:

## Option 1: Using Start Event with Form Properties

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start">
      <extensionElements>
        <flowable:formProperty id="requestId" name="Request ID" type="string" required="true"/>
        <flowable:formProperty id="requestDate" name="Request Date" type="date" required="true"/>
        <flowable:formProperty id="status" name="Status" type="string" default="PENDING"/>
        <flowable:formProperty id="priority" name="Priority" type="long" default="1"/>
        <flowable:formProperty id="amount" name="Amount" type="double" default="0.0"/>
      </extensionElements>
    </startEvent>
    
    <userTask id="userTask" name="User Task"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="userTask"/>
    
  </process>
  
</definitions>
```

## Option 2: Using Execution Listener to Initialize Variables

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <extensionElements>
      <flowable:executionListener event="start" class="com.example.ProcessInitializationListener"/>
    </extensionElements>
    
    <startEvent id="startEvent" name="Start"/>
    
    <userTask id="userTask" name="User Task"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="userTask"/>
    
  </process>
  
</definitions>
```

**ProcessInitializationListener.java:**

```java
package com.example;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import java.time.LocalDateTime;

public class ProcessInitializationListener implements ExecutionListener {
    
    @Override
    public void notify(DelegateExecution execution) {
        // Initialize process variables
        execution.setVariable("status", "PENDING");
        execution.setVariable("createdDate", LocalDateTime.now());
        execution.setVariable("priority", 1);
        execution.setVariable("amount", 0.0);
        execution.setVariable("approved", false);
    }
}
```

## Option 3: Using Script Task After Start Event

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start"/>
    
    <scriptTask id="initVariables" name="Initialize Variables" 
                scriptFormat="groovy">
      <script>
        <![CDATA[
          execution.setVariable("status", "PENDING");
          execution.setVariable("createdDate", new java.util.Date());
          execution.setVariable("priority", 1);
          execution.setVariable("amount", 0.0);
          execution.setVariable("approved", false);
        ]]>
      </script>
    </scriptTask>
    
    <userTask id="userTask" name="User Task"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="initVariables"/>
    <sequenceFlow id="flow2" sourceRef="initVariables" targetRef="userTask"/>
    
  </process>
  
</definitions>
```

## Option 4: Using Service Task with Delegate Expression

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start"/>
    
    <serviceTask id="initVariables" name="Initialize Variables" 
                 flowable:delegateExpression="${processInitializer}"/>
    
    <userTask id="userTask" name="User Task"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="initVariables"/>
    <sequenceFlow id="flow2" sourceRef="initVariables" targetRef="userTask"/>
    
  </process>
  
</definitions>
```

**ProcessInitializer.java:**

```java
package com.example;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component("processInitializer")
public class ProcessInitializer implements JavaDelegate {
    
    @Override
    public void execute(DelegateExecution execution) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("status", "PENDING");
        variables.put("createdDate", LocalDateTime.now());
        variables.put("priority", 1);
        variables.put("amount", 0.0);
        variables.put("approved", false);
        variables.put("comments", new ArrayList<>());
        
        execution.setVariables(variables);
    }
}
```

## Option 5: Initialize When Starting Process (Programmatically)

While not in BPMN XML, you can initialize variables when starting the process:

```java
import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProcessService {
    
    @Autowired
    private RuntimeService runtimeService;
    
    public String startProcess(String processDefinitionKey, Map<String, Object> customVariables) {
        // Default variables for all process instances
        Map<String, Object> variables = new HashMap<>();
        variables.put("status", "PENDING");
        variables.put("createdDate", LocalDateTime.now());
        variables.put("priority", 1);
        variables.put("amount", 0.0);
        variables.put("approved", false);
        
        // Merge with custom variables (custom variables override defaults)
        if (customVariables != null) {
            variables.putAll(customVariables);
        }
        
        return runtimeService.startProcessInstanceByKey(processDefinitionKey, variables)
                .getId();
    }
}
```

## Option 6: Using Execution Listener with Expression

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start">
      <extensionElements>
        <flowable:executionListener event="end" expression="${execution.setVariable('status', 'PENDING')}"/>
        <flowable:executionListener event="end" expression="${execution.setVariable('priority', 1)}"/>
        <flowable:executionListener event="end" expression="${execution.setVariable('amount', 0.0)}"/>
      </extensionElements>
    </startEvent>
    
    <userTask id="userTask" name="User Task"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="userTask"/>
    
  </process>
  
</definitions>
```

## Option 7: Complete Example with Multiple Initialization Methods

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             targetNamespace="http://flowable.org/test">
  
  <process id="purchaseRequest" name="Purchase Request Process" isExecutable="true">
    
    <!-- Process-level execution listener -->
    <extensionElements>
      <flowable:executionListener event="start" class="com.example.ProcessInitializationListener"/>
    </extensionElements>
    
    <!-- Start event with form properties -->
    <startEvent id="startEvent" name="Start Purchase Request">
      <extensionElements>
        <flowable:formProperty id="requestorName" name="Requestor Name" type="string" required="true"/>
        <flowable:formProperty id="department" name="Department" type="string" required="true"/>
        <flowable:formProperty id="requestDate" name="Request Date" type="date" required="true"/>
        <flowable:formProperty id="description" name="Description" type="string"/>
        
        <!-- Initialize variables on start event end -->
        <flowable:executionListener event="end" expression="${execution.setVariable('status', 'SUBMITTED')}"/>
        <flowable:executionListener event="end" expression="${execution.setVariable('currentApprovalLevel', 0)}"/>
      </extensionElements>
    </startEvent>
    
    <!-- Service task to initialize additional variables -->
    <serviceTask id="initializeVariables" name="Initialize Process Variables"
                 flowable:delegateExpression="${processInitializer}"/>
    
    <userTask id="managerApproval" name="Manager Approval">
      <extensionElements>
        <flowable:formProperty id="approved" name="Approved" type="boolean" required="true"/>
        <flowable:formProperty id="comments" name="Comments" type="string"/>
      </extensionElements>
    </userTask>
    
    <endEvent id="endEvent" name="End"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="initializeVariables"/>
    <sequenceFlow id="flow2" sourceRef="initializeVariables" targetRef="managerApproval"/>
    <sequenceFlow id="flow3" sourceRef="managerApproval" targetRef="endEvent"/>
    
  </process>
  
</definitions>
```

## Option 8: Using Data Objects (BPMN 2.0 Standard)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <!-- Define data objects -->
    <dataObject id="statusData" name="Status" itemSubjectRef="stringItem">
      <extensionElements>
        <flowable:value>PENDING</flowable:value>
      </extensionElements>
    </dataObject>
    
    <dataObject id="priorityData" name="Priority" itemSubjectRef="integerItem">
      <extensionElements>
        <flowable:value>1</flowable:value>
      </extensionElements>
    </dataObject>
    
    <startEvent id="startEvent" name="Start"/>
    <userTask id="userTask" name="User Task"/>
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="userTask"/>
    
  </process>
  
  <!-- Item definitions -->
  <itemDefinition id="stringItem" structureRef="java.lang.String"/>
  <itemDefinition id="integerItem" structureRef="java.lang.Integer"/>
  
</definitions>
```

## Option 9: Complex Initialization with Multiple Listeners

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start">
      <extensionElements>
        <!-- Initialize basic variables -->
        <flowable:executionListener event="end" class="com.example.BasicVariablesListener"/>
        <!-- Initialize audit variables -->
        <flowable:executionListener event="end" class="com.example.AuditVariablesListener"/>
        <!-- Initialize business variables -->
        <flowable:executionListener event="end" class="com.example.BusinessVariablesListener"/>
      </extensionElements>
    </startEvent>
    
    <userTask id="userTask" name="User Task"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="userTask"/>
    
  </process>
  
</definitions>
```

**BasicVariablesListener.java:**

```java
package com.example;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

public class BasicVariablesListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        execution.setVariable("status", "INITIATED");
        execution.setVariable("priority", 1);
        execution.setVariable("processType", "STANDARD");
    }
}
```

**AuditVariablesListener.java:**

```java
package com.example;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import java.time.LocalDateTime;

public class AuditVariablesListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        execution.setVariable("createdDate", LocalDateTime.now());
        execution.setVariable("createdBy", execution.getVariable("requestorName"));
        execution.setVariable("lastModifiedDate", LocalDateTime.now());
    }
}
```

## Recommended Approach

For most use cases, I recommend **Option 2** (Execution Listener) or **Option 4** (Service Task with Spring Bean):

**Best Practice BPMN:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <!-- Initialize on process start -->
    <extensionElements>
      <flowable:executionListener event="start" delegateExpression="${processVariableInitializer}"/>
    </extensionElements>
    
    <startEvent id="startEvent" name="Start"/>
    <userTask id="userTask" name="User Task"/>
    <endEvent id="endEvent" name="End"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="userTask"/>
    <sequenceFlow id="flow2" sourceRef="userTask" targetRef="endEvent"/>
    
  </process>
  
</definitions>
```

This approach is clean, maintainable, and allows you to use dependency injection in your listener class.


# variable when task transfer

Here are multiple ways to add or update process variables when transitioning from one task to another in Flowable BPMN:

## Option 1: Using Execution Listener on Sequence Flow

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start"/>
    
    <userTask id="task1" name="Task 1"/>
    
    <userTask id="task2" name="Task 2"/>
    
    <endEvent id="endEvent" name="End"/>
    
    <!-- Add variables when transitioning from task1 to task2 -->
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="task1"/>
    
    <sequenceFlow id="flow2" sourceRef="task1" targetRef="task2">
      <extensionElements>
        <flowable:executionListener event="take" class="com.example.TransitionVariablesListener"/>
      </extensionElements>
    </sequenceFlow>
    
    <sequenceFlow id="flow3" sourceRef="task2" targetRef="endEvent"/>
    
  </process>
  
</definitions>
```

**TransitionVariablesListener.java:**

```java
package com.example;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import java.time.LocalDateTime;

public class TransitionVariablesListener implements ExecutionListener {
    
    @Override
    public void notify(DelegateExecution execution) {
        // Add new variables during transition
        execution.setVariable("transitionTimestamp", LocalDateTime.now());
        execution.setVariable("previousTask", "task1");
        execution.setVariable("nextTask", "task2");
        execution.setVariable("transitionCount", 
            (Integer) execution.getVariable("transitionCount", 0) + 1);
    }
}
```

## Option 2: Using Execution Listener with Expression

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start"/>
    
    <userTask id="task1" name="Task 1"/>
    
    <userTask id="task2" name="Task 2"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="task1"/>
    
    <sequenceFlow id="flow2" sourceRef="task1" targetRef="task2">
      <extensionElements>
        <!-- Set variables using expressions -->
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('status', 'IN_REVIEW')}"/>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('lastTransition', 'task1_to_task2')}"/>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('reviewStartTime', T(java.time.LocalDateTime).now())}"/>
      </extensionElements>
    </sequenceFlow>
    
    <sequenceFlow id="flow3" sourceRef="task2" targetRef="endEvent"/>
    
  </process>
  
</definitions>
```

## Option 3: Using Task Listener on Task Complete

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start"/>
    
    <userTask id="task1" name="Task 1">
      <extensionElements>
        <!-- Add variables when task1 completes -->
        <flowable:taskListener event="complete" class="com.example.Task1CompleteListener"/>
      </extensionElements>
    </userTask>
    
    <userTask id="task2" name="Task 2">
      <extensionElements>
        <!-- Add variables when task2 starts -->
        <flowable:taskListener event="create" class="com.example.Task2CreateListener"/>
      </extensionElements>
    </userTask>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="task1"/>
    <sequenceFlow id="flow2" sourceRef="task1" targetRef="task2"/>
    
  </process>
  
</definitions>
```

**Task1CompleteListener.java:**

```java
package com.example;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import java.time.LocalDateTime;

public class Task1CompleteListener implements TaskListener {
    
    @Override
    public void notify(DelegateTask delegateTask) {
        // Add variables when task1 is completed
        delegateTask.setVariable("task1CompletedBy", delegateTask.getAssignee());
        delegateTask.setVariable("task1CompletedAt", LocalDateTime.now());
        delegateTask.setVariable("task1Duration", 
            calculateDuration(delegateTask.getCreateTime()));
        delegateTask.setVariable("workflowStep", "REVIEW");
    }
    
    private long calculateDuration(java.util.Date createTime) {
        return System.currentTimeMillis() - createTime.getTime();
    }
}
```

**Task2CreateListener.java:**

```java
package com.example;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import java.time.LocalDateTime;

public class Task2CreateListener implements TaskListener {
    
    @Override
    public void notify(DelegateTask delegateTask) {
        // Add variables when task2 is created
        delegateTask.setVariable("task2StartedAt", LocalDateTime.now());
        delegateTask.setVariable("reviewerAssigned", delegateTask.getAssignee());
        delegateTask.setVariable("currentStage", "REVIEW");
    }
}
```

## Option 4: Using Service Task Between User Tasks

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start"/>
    
    <userTask id="task1" name="Submit Request"/>
    
    <!-- Service task to set variables between tasks -->
    <serviceTask id="updateVariables" name="Update Process Variables"
                 flowable:delegateExpression="${variableUpdaterService}"/>
    
    <userTask id="task2" name="Review Request"/>
    
    <endEvent id="endEvent" name="End"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="task1"/>
    <sequenceFlow id="flow2" sourceRef="task1" targetRef="updateVariables"/>
    <sequenceFlow id="flow3" sourceRef="updateVariables" targetRef="task2"/>
    <sequenceFlow id="flow4" sourceRef="task2" targetRef="endEvent"/>
    
  </process>
  
</definitions>
```

**VariableUpdaterService.java:**

```java
package com.example;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component("variableUpdaterService")
public class VariableUpdaterService implements JavaDelegate {
    
    @Override
    public void execute(DelegateExecution execution) {
        // Add multiple variables at once
        Map<String, Object> newVariables = new HashMap<>();
        newVariables.put("status", "PENDING_REVIEW");
        newVariables.put("submittedAt", LocalDateTime.now());
        newVariables.put("reviewRequired", true);
        newVariables.put("priority", calculatePriority(execution));
        
        execution.setVariables(newVariables);
        
        // Update existing variable
        Integer stepCount = (Integer) execution.getVariable("stepCount");
        if (stepCount == null) {
            stepCount = 0;
        }
        execution.setVariable("stepCount", stepCount + 1);
    }
    
    private int calculatePriority(DelegateExecution execution) {
        // Business logic to calculate priority
        Double amount = (Double) execution.getVariable("amount");
        if (amount != null && amount > 10000) {
            return 3; // High priority
        }
        return 1; // Normal priority
    }
}
```

## Option 5: Using Script Task Between User Tasks

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start"/>
    
    <userTask id="task1" name="Task 1"/>
    
    <!-- Script task to set variables -->
    <scriptTask id="setVariables" name="Set Variables" scriptFormat="groovy">
      <script>
        <![CDATA[
          execution.setVariable("status", "IN_PROGRESS");
          execution.setVariable("transitionTime", new java.util.Date());
          execution.setVariable("previousTaskId", "task1");
          execution.setVariable("nextTaskId", "task2");
          
          // Increment counter
          def count = execution.getVariable("taskCount") ?: 0;
          execution.setVariable("taskCount", count + 1);
        ]]>
      </script>
    </scriptTask>
    
    <userTask id="task2" name="Task 2"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="task1"/>
    <sequenceFlow id="flow2" sourceRef="task1" targetRef="setVariables"/>
    <sequenceFlow id="flow3" sourceRef="setVariables" targetRef="task2"/>
    
  </process>
  
</definitions>
```

## Option 6: Programmatically When Completing Task

```java
package com.example;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TaskCompletionService {
    
    @Autowired
    private TaskService taskService;
    
    public void completeTask(String taskId, Map<String, Object> taskVariables) {
        // Get the task
        Task task = taskService.createTaskQuery()
            .taskId(taskId)
            .singleResult();
        
        // Prepare variables to be set during completion
        Map<String, Object> variables = new HashMap<>();
        
        // Add transition variables
        variables.put("completedTaskId", taskId);
        variables.put("completedTaskName", task.getName());
        variables.put("completedBy", task.getAssignee());
        variables.put("completedAt", LocalDateTime.now());
        variables.put("status", "COMPLETED");
        
        // Add task-specific variables
        if (taskVariables != null) {
            variables.putAll(taskVariables);
        }
        
        // Complete task with variables
        taskService.complete(taskId, variables);
    }
    
    public void completeTaskWithTransitionData(String taskId, String nextTaskInfo) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("transitionTimestamp", LocalDateTime.now());
        variables.put("nextTask", nextTaskInfo);
        variables.put("workflowPhase", "REVIEW");
        
        taskService.complete(taskId, variables);
    }
}
```

## Option 7: Using Gateway with Conditions and Variables

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="myProcess" name="My Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start"/>
    
    <userTask id="task1" name="Submit Request"/>
    
    <exclusiveGateway id="gateway" name="Check Amount"/>
    
    <!-- High value path - sets different variables -->
    <sequenceFlow id="highValueFlow" sourceRef="gateway" targetRef="managerApproval">
      <conditionExpression xsi:type="tFormalExpression">
        <![CDATA[${amount > 10000}]]>
      </conditionExpression>
      <extensionElements>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('approvalLevel', 'MANAGER')}"/>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('priority', 'HIGH')}"/>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('requiresManagerApproval', true)}"/>
      </extensionElements>
    </sequenceFlow>
    
    <!-- Normal path - sets different variables -->
    <sequenceFlow id="normalFlow" sourceRef="gateway" targetRef="supervisorApproval">
      <conditionExpression xsi:type="tFormalExpression">
        <![CDATA[${amount <= 10000}]]>
      </conditionExpression>
      <extensionElements>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('approvalLevel', 'SUPERVISOR')}"/>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('priority', 'NORMAL')}"/>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('requiresManagerApproval', false)}"/>
      </extensionElements>
    </sequenceFlow>
    
    <userTask id="managerApproval" name="Manager Approval"/>
    <userTask id="supervisorApproval" name="Supervisor Approval"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="task1"/>
    <sequenceFlow id="flow2" sourceRef="task1" targetRef="gateway"/>
    
  </process>
  
</definitions>
```

## Option 8: Complete Example with Multiple Approaches

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             targetNamespace="http://flowable.org/test">
  
  <process id="purchaseRequest" name="Purchase Request Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start"/>
    
    <!-- Task 1: Submit Request -->
    <userTask id="submitRequest" name="Submit Request">
      <extensionElements>
        <flowable:formProperty id="itemName" name="Item Name" type="string" required="true"/>
        <flowable:formProperty id="amount" name="Amount" type="double" required="true"/>
        
        <!-- Set variables when task completes -->
        <flowable:taskListener event="complete" delegateExpression="${requestSubmitListener}"/>
      </extensionElements>
    </userTask>
    
    <!-- Sequence flow with execution listener -->
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="submitRequest"/>
    
    <sequenceFlow id="flow2" sourceRef="submitRequest" targetRef="validateRequest">
      <extensionElements>
        <!-- Set variables during transition -->
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('submittedAt', T(java.time.LocalDateTime).now())}"/>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('status', 'SUBMITTED')}"/>
        <flowable:executionListener event="take" 
          delegateExpression="${transitionLogger}"/>
      </extensionElements>
    </sequenceFlow>
    
    <!-- Service task to validate and set variables -->
    <serviceTask id="validateRequest" name="Validate Request"
                 flowable:delegateExpression="${requestValidator}"/>
    
    <sequenceFlow id="flow3" sourceRef="validateRequest" targetRef="approveRequest">
      <extensionElements>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('validatedAt', T(java.time.LocalDateTime).now())}"/>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('status', 'PENDING_APPROVAL')}"/>
      </extensionElements>
    </sequenceFlow>
    
    <!-- Task 2: Approve Request -->
    <userTask id="approveRequest" name="Approve Request" flowable:candidateGroups="managers">
      <extensionElements>
        <flowable:formProperty id="approved" name="Approved" type="boolean" required="true"/>
        <flowable:formProperty id="comments" name="Comments" type="string"/>
        
        <!-- Set variables when task is created -->
        <flowable:taskListener event="create" delegateExpression="${approvalTaskCreateListener}"/>
        
        <!-- Set variables when task completes -->
        <flowable:taskListener event="complete" delegateExpression="${approvalCompleteListener}"/>
      </extensionElements>
    </userTask>
    
    <sequenceFlow id="flow4" sourceRef="approveRequest" targetRef="endEvent">
      <extensionElements>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('completedAt', T(java.time.LocalDateTime).now())}"/>
        <flowable:executionListener event="take" 
          expression="${execution.setVariable('status', 'COMPLETED')}"/>
      </extensionElements>
    </sequenceFlow>
    
    <endEvent id="endEvent" name="End"/>
    
  </process>
  
</definitions>
```

**Supporting Java Classes:**

```java
package com.example;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component("requestSubmitListener")
public class RequestSubmitListener implements TaskListener {
    
    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setVariable("requestSubmittedBy", delegateTask.getAssignee());
        delegateTask.setVariable("requestSubmittedAt", LocalDateTime.now());
        delegateTask.setVariable("workflowPhase", "VALIDATION");
    }
}

@Component("approvalTaskCreateListener")
public class ApprovalTaskCreateListener implements TaskListener {
    
    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setVariable("approvalStartedAt", LocalDateTime.now());
        delegateTask.setVariable("approverAssigned", delegateTask.getAssignee());
        delegateTask.setVariable("workflowPhase", "APPROVAL");
    }
}

@Component("approvalCompleteListener")
public class ApprovalCompleteListener implements TaskListener {
    
    @Override
    public void notify(DelegateTask delegateTask) {
        Boolean approved = (Boolean) delegateTask.getVariable("approved");
        String comments = (String) delegateTask.getVariable("comments");
        
        delegateTask.setVariable("approvalDecision", approved ? "APPROVED" : "REJECTED");
        delegateTask.setVariable("approvalComments", comments);
        delegateTask.setVariable("approvedBy", delegateTask.getAssignee());
        delegateTask.setVariable("approvedAt", LocalDateTime.now());
        delegateTask.setVariable("finalStatus", approved ? "APPROVED" : "REJECTED");
    }
}

@Component("transitionLogger")
public class TransitionLogger implements ExecutionListener {
    
    @Override
    public void notify(DelegateExecution execution) {
        execution.setVariable("lastTransitionTime", LocalDateTime.now());
        
        Integer transitionCount = (Integer) execution.getVariable("transitionCount");
        execution.setVariable("transitionCount", transitionCount == null ? 1 : transitionCount + 1);
    }
}

@Component("requestValidator")
public class RequestValidator implements JavaDelegate {
    
    @Override
    public void execute(DelegateExecution execution) {
        Double amount = (Double) execution.getVariable("amount");
        
        // Set validation variables
        execution.setVariable("validationPassed", amount != null && amount > 0);
        execution.setVariable("validatedBy", "SYSTEM");
        execution.setVariable("validatedAt", LocalDateTime.now());
        
        // Set priority based on amount
        if (amount != null) {
            if (amount > 50000) {
                execution.setVariable("priority", "CRITICAL");
                execution.setVariable("approvalRequired", "DIRECTOR");
            } else if (amount > 10000) {
                execution.setVariable("priority", "HIGH");
                execution.setVariable("approvalRequired", "MANAGER");
            } else {
                execution.setVariable("priority", "NORMAL");
                execution.setVariable("approvalRequired", "SUPERVISOR");
            }
        }
    }
}
```

## Option 9: Using Local Variables (Task-Scoped)

```java
package com.example;

import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TaskVariableService {
    
    @Autowired
    private TaskService taskService;
    
    // Set process-level variables (available throughout process)
    public void setProcessVariables(String taskId, Map<String, Object> variables) {
        taskService.setVariables(taskId, variables);
    }
    
    // Set local variables (only for this task)
    public void setLocalVariables(String taskId, Map<String, Object> variables) {
        taskService.setVariablesLocal(taskId, variables);
    }
    
    // Complete task with both process and local variables
    public void completeTaskWithVariables(String taskId) {
        // Process variables - available to next tasks
        Map<String, Object> processVars = new HashMap<>();
        processVars.put("status", "APPROVED");
        processVars.put("approvedAt", LocalDateTime.now());
        
        // Complete with variables
        taskService.complete(taskId, processVars);
    }
}
```

## Recommended Approach

For most scenarios, I recommend:

1. **Task Listeners** (Option 3) - When you need to capture task-specific information (who completed it, when, etc.)
2. **Execution Listeners on Sequence Flows** (Option 1) - When you need to set variables during the transition itself
3. **Service Task** (Option 4) - When you have complex business logic to determine variable values

These approaches are clean, maintainable, and follow BPMN best practices.

# Task Delay Execute 

There are several ways to defer task execution in Flowable. Here are the main approaches:

## Option 1: Using Timer Intermediate Event (Recommended)

This is the most common and clean approach for adding delays in a process.

**BPMN with Timer Intermediate Event:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="deferredTaskProcess" name="Deferred Task Process" isExecutable="true">
    
    <startEvent id="startEvent" name="Start"/>
    
    <!-- Task before delay -->
    <serviceTask id="taskBeforeDelay" name="Task Before Delay" 
                 flowable:delegateExpression="${initialTaskService}"/>
    
    <!-- Timer Event - Wait 5 minutes -->
    <intermediateCatchEvent id="waitTimer" name="Wait 5 Minutes">
      <timerEventDefinition>
        <!-- ISO 8601 duration format: PT5M = 5 minutes -->
        <timeDuration>PT5M</timeDuration>
      </timerEventDefinition>
    </intermediateCatchEvent>
    
    <!-- Task after delay -->
    <serviceTask id="taskAfterDelay" name="Task After Delay" 
                 flowable:delegateExpression="${deferredTaskService}"/>
    
    <endEvent id="endEvent" name="End"/>
    
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="taskBeforeDelay"/>
    <sequenceFlow id="flow2" sourceRef="taskBeforeDelay" targetRef="waitTimer"/>
    <sequenceFlow id="flow3" sourceRef="waitTimer" targetRef="taskAfterDelay"/>
    <sequenceFlow id="flow4" sourceRef="taskAfterDelay" targetRef="endEvent"/>
    
  </process>
  
</definitions>
```

**Service Implementation:**

```java
package com.example.service;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("initialTaskService")
public class InitialTaskService implements JavaDelegate {
    
    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("Initial task executed at: " + new java.util.Date());
        execution.setVariable("initialTaskCompleted", true);
        execution.setVariable("initialTaskTime", System.currentTimeMillis());
    }
}

@Component("deferredTaskService")
public class DeferredTaskService implements JavaDelegate {
    
    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("Deferred task executed at: " + new java.util.Date());
        
        Long initialTime = (Long) execution.getVariable("initialTaskTime");
        long delayInMs = System.currentTimeMillis() - initialTime;
        long delayInMinutes = delayInMs / (1000 * 60);
        
        System.out.println("Task was delayed by approximately " + delayInMinutes + " minutes");
        execution.setVariable("deferredTaskCompleted", true);
    }
}
```

## Option 2: Using Timer Duration with Different Formats

**Various Timer Duration Formats:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="timerExamplesProcess" name="Timer Examples" isExecutable="true">
    
    <startEvent id="start"/>
    
    <!-- Example 1: Wait 5 minutes -->
    <intermediateCatchEvent id="wait5Minutes" name="Wait 5 Minutes">
      <timerEventDefinition>
        <timeDuration>PT5M</timeDuration>
      </timerEventDefinition>
    </intermediateCatchEvent>
    
    <!-- Example 2: Wait 30 seconds -->
    <intermediateCatchEvent id="wait30Seconds" name="Wait 30 Seconds">
      <timerEventDefinition>
        <timeDuration>PT30S</timeDuration>
      </timerEventDefinition>
    </intermediateCatchEvent>
    
    <!-- Example 3: Wait 2 hours -->
    <intermediateCatchEvent id="wait2Hours" name="Wait 2 Hours">
      <timerEventDefinition>
        <timeDuration>PT2H</timeDuration>
      </timerEventDefinition>
    </intermediateCatchEvent>
    
    <!-- Example 4: Wait 1 day -->
    <intermediateCatchEvent id="wait1Day" name="Wait 1 Day">
      <timerEventDefinition>
        <timeDuration>P1D</timeDuration>
      </timerEventDefinition>
    </intermediateCatchEvent>
    
    <!-- Example 5: Wait 1 day 2 hours 30 minutes -->
    <intermediateCatchEvent id="waitComplex" name="Wait Complex Duration">
      <timerEventDefinition>
        <timeDuration>P1DT2H30M</timeDuration>
      </timerEventDefinition>
    </intermediateCatchEvent>
    
    <!-- Example 6: Dynamic duration from process variable -->
    <intermediateCatchEvent id="waitDynamic" name="Wait Dynamic">
      <timerEventDefinition>
        <timeDuration>${waitDuration}</timeDuration>
      </timerEventDefinition>
    </intermediateCatchEvent>
    
    <endEvent id="end"/>
    
    <sequenceFlow sourceRef="start" targetRef="wait5Minutes"/>
    <sequenceFlow sourceRef="wait5Minutes" targetRef="wait30Seconds"/>
    <sequenceFlow sourceRef="wait30Seconds" targetRef="wait2Hours"/>
    <sequenceFlow sourceRef="wait2Hours" targetRef="wait1Day"/>
    <sequenceFlow sourceRef="wait1Day" targetRef="waitComplex"/>
    <sequenceFlow sourceRef="waitComplex" targetRef="waitDynamic"/>
    <sequenceFlow sourceRef="waitDynamic" targetRef="end"/>
    
  </process>
  
</definitions>
```

**ISO 8601 Duration Format Reference:**
- `PT30S` = 30 seconds
- `PT5M` = 5 minutes
- `PT2H` = 2 hours
- `P1D` = 1 day
- `P1DT2H30M` = 1 day, 2 hours, 30 minutes
- `P` = Period designator
- `T` = Time designator (separates date and time components)

## Option 3: Using Timer with Specific Date/Time

**Wait Until Specific Date/Time:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="scheduledTaskProcess" name="Scheduled Task Process" isExecutable="true">
    
    <startEvent id="start"/>
    
    <!-- Wait until specific date/time -->
    <intermediateCatchEvent id="waitUntilDate" name="Wait Until Specific Time">
      <timerEventDefinition>
        <!-- ISO 8601 date format: 2025-12-31T23:59:59 -->
        <timeDate>2025-12-31T23:59:59</timeDate>
      </timerEventDefinition>
    </intermediateCatchEvent>
    
    <!-- Dynamic date from process variable -->
    <intermediateCatchEvent id="waitUntilDynamicDate" name="Wait Until Dynamic Time">
      <timerEventDefinition>
        <timeDate>${scheduledExecutionTime}</timeDate>
      </timerEventDefinition>
    </intermediateCatchEvent>
    
    <serviceTask id="executeScheduledTask" name="Execute Scheduled Task"
                 flowable:delegateExpression="${scheduledTaskService}"/>
    
    <endEvent id="end"/>
    
    <sequenceFlow sourceRef="start" targetRef="waitUntilDate"/>
    <sequenceFlow sourceRef="waitUntilDate" targetRef="waitUntilDynamicDate"/>
    <sequenceFlow sourceRef="waitUntilDynamicDate" targetRef="executeScheduledTask"/>
    <sequenceFlow sourceRef="executeScheduledTask" targetRef="end"/>
    
  </process>
  
</definitions>
```

**Start Process with Scheduled Time:**

```java
package com.example.service;

import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ScheduledProcessService {
    
    @Autowired
    private RuntimeService runtimeService;
    
    public String startScheduledProcess() {
        // Calculate execution time (5 minutes from now)
        LocalDateTime executionTime = LocalDateTime.now().plusMinutes(5);
        String formattedTime = executionTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("scheduledExecutionTime", formattedTime);
        variables.put("taskData", "some data");
        
        return runtimeService.startProcessInstanceByKey("scheduledTaskProcess", variables)
            .getProcessInstanceId();
    }
    
    public String startScheduledProcessWithDelay(int minutesDelay) {
        LocalDateTime executionTime = LocalDateTime.now().plusMinutes(minutesDelay);
        String formattedTime = executionTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("scheduledExecutionTime", formattedTime);
        
        return runtimeService.startProcessInstanceByKey("scheduledTaskProcess", variables)
            .getProcessInstanceId();
    }
}
```

## Option 4: Using Timer Cycle for Repeated Delays

**Repeating Timer (useful for polling or retry scenarios):**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="repeatingTimerProcess" name="Repeating Timer Process" isExecutable="true">
    
    <startEvent id="start"/>
    
    <!-- Timer that repeats every 5 minutes, up to 3 times -->
    <intermediateCatchEvent id="repeatingTimer" name="Repeat Every 5 Minutes">
      <timerEventDefinition>
        <!-- R3/PT5M means: Repeat 3 times with 5 minute intervals -->
        <timeCycle>R3/PT5M</timeCycle>
      </timerEventDefinition>
    </intermediateCatchEvent>
    
    <!-- Task that executes after each interval -->
    <serviceTask id="repeatedTask" name="Repeated Task"
                 flowable:delegateExpression="${repeatedTaskService}"/>
    
    <endEvent id="end"/>
    
    <sequenceFlow sourceRef="start" targetRef="repeatingTimer"/>
    <sequenceFlow sourceRef="repeatingTimer" targetRef="repeatedTask"/>
    <sequenceFlow sourceRef="repeatedTask" targetRef="end"/>
    
  </process>
  
</definitions>
```

**Timer Cycle Format:**
- `R3/PT5M` = Repeat 3 times, every 5 minutes
- `R/PT1H` = Repeat indefinitely, every 1 hour
- `R5/PT30S` = Repeat 5 times, every 30 seconds
- `R/P1D` = Repeat indefinitely, every 1 day

## Option 5: Timer Boundary Event (Non-Interrupting)

**Task with Delayed Side Effect:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="boundaryTimerProcess" name="Boundary Timer Process" isExecutable="true">
    
    <startEvent id="start"/>
    
    <!-- User task with non-interrupting timer -->
    <userTask id="waitForApproval" name="Wait for Approval">
      <!-- Non-interrupting timer boundary event -->
      <boundaryEvent id="reminderTimer" name="Send Reminder After 5 Minutes" 
                     cancelActivity="false" attachedToRef="waitForApproval">
        <timerEventDefinition>
          <timeDuration>PT5M</timeDuration>
        </timerEventDefinition>
      </boundaryEvent>
    </userTask>
    
    <!-- This executes after 5 minutes if approval is not completed -->
    <serviceTask id="sendReminder" name="Send Reminder"
                 flowable:delegateExpression="${reminderService}"/>
    
    <endEvent id="endReminder"/>
    
    <!-- Continue with main flow after approval -->
    <serviceTask id="processApproval" name="Process Approval"
                 flowable:delegateExpression="${approvalService}"/>
    
    <endEvent id="end"/>
    
    <!-- Main flow -->
    <sequenceFlow sourceRef="start" targetRef="waitForApproval"/>
    <sequenceFlow sourceRef="waitForApproval" targetRef="processApproval"/>
    <sequenceFlow sourceRef="processApproval" targetRef="end"/>
    
    <!-- Timer flow -->
    <sequenceFlow sourceRef="reminderTimer" targetRef="sendReminder"/>
    <sequenceFlow sourceRef="sendReminder" targetRef="endReminder"/>
    
  </process>
  
</definitions>
```

## Option 6: Timer Boundary Event (Interrupting)

**Timeout/Escalation Scenario:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="timeoutProcess" name="Timeout Process" isExecutable="true">
    
    <startEvent id="start"/>
    
    <!-- Service task with timeout -->
    <serviceTask id="longRunningTask" name="Long Running Task"
                 flowable:delegateExpression="${longRunningTaskService}">
      
      <!-- Interrupting timer - cancels the task after 5 minutes -->
      <boundaryEvent id="timeoutTimer" name="Timeout After 5 Minutes" 
                     cancelActivity="true" attachedToRef="longRunningTask">
        <timerEventDefinition>
          <timeDuration>PT5M</timeDuration>
        </timerEventDefinition>
      </boundaryEvent>
    </serviceTask>
    
    <!-- Success path -->
    <serviceTask id="processSuccess" name="Process Success"
                 flowable:delegateExpression="${successService}"/>
    
    <endEvent id="endSuccess"/>
    
    <!-- Timeout path -->
    <serviceTask id="handleTimeout" name="Handle Timeout"
                 flowable:delegateExpression="${timeoutService}"/>
    
    <endEvent id="endTimeout"/>
    
    <!-- Main flow -->
    <sequenceFlow sourceRef="start" targetRef="longRunningTask"/>
    <sequenceFlow sourceRef="longRunningTask" targetRef="processSuccess"/>
    <sequenceFlow sourceRef="processSuccess" targetRef="endSuccess"/>
    
    <!-- Timeout flow -->
    <sequenceFlow sourceRef="timeoutTimer" targetRef="handleTimeout"/>
    <sequenceFlow sourceRef="handleTimeout" targetRef="endTimeout"/>
    
  </process>
  
</definitions>
```

**Service Implementations:**

```java
package com.example.service;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("longRunningTaskService")
public class LongRunningTaskService implements JavaDelegate {
    
    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("Starting long running task...");
        
        // Simulate long running operation
        // If this takes more than 5 minutes, the timer will fire
        try {
            // Your actual long-running logic here
            Thread.sleep(2000); // Simulating work
            
            execution.setVariable("taskCompleted", true);
            System.out.println("Task completed successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            execution.setVariable("taskInterrupted", true);
        }
    }
}

@Component("timeoutService")
public class TimeoutService implements JavaDelegate {
    
    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("Task timed out after 5 minutes!");
        
        // Handle timeout - send notification, log, etc.
        execution.setVariable("timeoutOccurred", true);
        execution.setVariable("timeoutTimestamp", new java.util.Date());
        
        // Could trigger compensation, retry logic, or escalation
    }
}

@Component("successService")
public class SuccessService implements JavaDelegate {
    
    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("Processing successful completion");
        execution.setVariable("processedSuccessfully", true);
    }
}
```

## Option 7: Timer Start Event (Schedule Process Start)

**Process that starts at scheduled time:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="scheduledStartProcess" name="Scheduled Start Process" isExecutable="true">
    
    <!-- Timer Start Event - Process starts every 5 minutes automatically -->
    <startEvent id="timerStart" name="Start Every 5 Minutes">
      <timerEventDefinition>
        <!-- R means repeat indefinitely -->
        <timeCycle>R/PT5M</timeCycle>
      </timerEventDefinition>
    </startEvent>
    
    <serviceTask id="scheduledTask" name="Scheduled Task"
                 flowable:delegateExpression="${scheduledTaskService}"/>
    
    <endEvent id="end"/>
    
    <sequenceFlow sourceRef="timerStart" targetRef="scheduledTask"/>
    <sequenceFlow sourceRef="scheduledTask" targetRef="end"/>
    
  </process>
  
</definitions>
```

## Option 8: Async Service Task with Delay

**Using Flowable's Async Execution with Delay:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="asyncDelayProcess" name="Async Delay Process" isExecutable="true">
    
    <startEvent id="start"/>
    
    <!-- Regular task -->
    <serviceTask id="immediateTask" name="Immediate Task"
                 flowable:delegateExpression="${immediateTaskService}"/>
    
    <!-- Async task with timer -->
    <intermediateCatchEvent id="asyncDelay" name="Async Delay">
      <timerEventDefinition>
        <timeDuration>PT5M</timeDuration>
      </timerEventDefinition>
    </intermediateCatchEvent>
    
    <!-- This task executes asynchronously after the delay -->
    <serviceTask id="asyncTask" name="Async Task" 
                 flowable:async="true"
                 flowable:delegateExpression="${asyncTaskService}"/>
    
    <endEvent id="end"/>
    
    <sequenceFlow sourceRef="start" targetRef="immediateTask"/>
    <sequenceFlow sourceRef="immediateTask" targetRef="asyncDelay"/>
    <sequenceFlow sourceRef="asyncDelay" targetRef="asyncTask"/>
    <sequenceFlow sourceRef="asyncTask" targetRef="end"/>
    
  </process>
  
</definitions>
```

## Complete Working Example

**Complete BPMN Process with 5-Minute Delay:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  
  <process id="orderProcessWithDelay" name="Order Process with Delay" isExecutable="true">
    
    <startEvent id="startEvent" name="Order Received"/>
    
    <!-- Immediate validation -->
    <serviceTask id="validateOrder" name="Validate Order"
                 flowable:delegateExpression="${orderValidationService}"/>
    
    <!-- Wait 5 minutes before payment processing -->
    <intermediateCatchEvent id="waitBeforePayment" name="Wait 5 Minutes">
      <timerEventDefinition>
        <timeDuration>PT5M</timeDuration>
      </timerEventDefinition>
    </intermediateCatchEvent>
    
    <!-- Process payment after delay -->
    <serviceTask id="processPayment" name="Process Payment"
                 flowable:delegateExpression="${paymentService}"/>
    
    <!-- Check payment status -->
    <exclusiveGateway id="paymentGateway" name="Payment Success?"/>