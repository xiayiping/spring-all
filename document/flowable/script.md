# Flowable Script Types and Spring Bean Access (7.0+)

Flowable supports multiple script types for different execution contexts. Here's how to fetch Spring beans in each type using Flowable 7.0+ with Spring Boot starter.

## Supported Script Types

### 1. **Script Task (Execution Listener Scripts)**

Used in BPMN process definitions for script tasks and execution listeners.

**Groovy Example:**
```groovy
import org.flowable.engine.impl.context.Context

// Method 1: Via ProcessEngineConfiguration
def myBean = Context.getProcessEngineConfiguration()
    .getApplicationContext()
    .getBean("myServiceBean")

// Method 2: Via Spring ApplicationContext (if available)
def appContext = execution.getVariable("applicationContext")
def myBean = appContext.getBean(com.example.MyService.class)

// Method 3: Direct bean access (Flowable 7.0+)
def myBean = beans.myServiceBean

// Use the bean
def result = myBean.doSomething(execution.getVariable("inputData"))
execution.setVariable("output", result)
```

**JavaScript Example:**
```javascript
var ProcessEngineConfiguration = Java.type('org.flowable.engine.impl.context.Context');
var config = ProcessEngineConfiguration.getProcessEngineConfiguration();
var myBean = config.getApplicationContext().getBean('myServiceBean');

var result = myBean.doSomething(execution.getVariable('inputData'));
execution.setVariable('output', result);
```

### 2. **Task Listener Scripts**

Used for user task events (create, assignment, complete).

**Groovy Example:**
```groovy
import org.flowable.engine.impl.context.Context

def myBean = Context.getProcessEngineConfiguration()
    .getApplicationContext()
    .getBean("taskNotificationService")

// Access task properties
def taskId = task.getId()
def assignee = task.getAssignee()

// Call bean method
myBean.sendNotification(taskId, assignee)
```

### 3. **Expression Scripts (in Service Tasks)**

Service tasks can use expressions to invoke Spring beans directly.

**BPMN XML Example:**
```xml
<serviceTask id="callSpringBean" 
             flowable:expression="#{myServiceBean.processData(execution)}" 
             flowable:resultVariableName="result"/>

<!-- Or with delegateExpression -->
<serviceTask id="delegateTask" 
             flowable:delegateExpression="#{myJavaDelegate}"/>
```

### 4. **Decision Table Scripts (DMN)**

Used in DMN decision tables for custom functions.

**Groovy Example:**
```groovy
import org.flowable.engine.impl.context.Context

def validationService = Context.getProcessEngineConfiguration()
    .getApplicationContext()
    .getBean("validationService")

def isValid = validationService.validate(inputVariable)
outputVariable = isValid ? "APPROVED" : "REJECTED"
```

### 5. **Event Listener Scripts**

For global event listeners configured in the engine.

**Groovy Example:**
```groovy
import org.flowable.engine.impl.context.Context

def auditService = Context.getProcessEngineConfiguration()
    .getApplicationContext()
    .getBean("auditService")

auditService.logEvent(event.getType(), event.getProcessInstanceId())
```

## Complete Spring Boot Setup (Flowable 7.0+)

### Application Configuration

```java
@Configuration
public class FlowableConfig {
    
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> customConfigurer() {
        return engineConfiguration -> {
            // Enable scripting
            engineConfiguration.setEnableScripting(true);
            
            // Add script resolvers
            List<Resolver> resolvers = new ArrayList<>();
            resolvers.add(new VariableScopeResolver());
            resolvers.add(new BeansResolver());
            engineConfiguration.setResolvers(resolvers);
            
            // Configure script engines
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            engineConfiguration.setScriptEngineManager(scriptEngineManager);
        };
    }
}
```

### Spring Service Bean Example

```java
@Service("myServiceBean")
public class MyServiceBean {
    
    private static final Logger logger = LoggerFactory.getLogger(MyServiceBean.class);
    
    @Autowired
    private SomeRepository repository;
    
    public String processData(DelegateExecution execution) {
        String inputData = (String) execution.getVariable("inputData");
        logger.info("Processing: {}", inputData);
        
        // Business logic
        String result = inputData.toUpperCase();
        
        // Can access other Spring beans
        repository.save(new DataEntity(result));
        
        return result;
    }
    
    public void handleTask(DelegateTask task) {
        logger.info("Task created: {}", task.getId());
        // Task handling logic
    }
}
```

### BPMN Process Example

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="Examples">
  
  <process id="scriptBeanProcess" name="Script Bean Process">
    
    <startEvent id="start"/>
    
    <!-- Script Task using Groovy -->
    <scriptTask id="groovyScript" name="Groovy Script" 
                scriptFormat="groovy" flowable:autoStoreVariables="true">
      <script><![CDATA[
        import org.flowable.engine.impl.context.Context
        
        def myBean = Context.getProcessEngineConfiguration()
            .getApplicationContext()
            .getBean("myServiceBean")
        
        def result = myBean.processData(execution)
        execution.setVariable("scriptResult", result)
      ]]></script>
    </scriptTask>
    
    <!-- Service Task using Expression -->
    <serviceTask id="expressionTask" name="Expression Task"
                 flowable:expression="#{myServiceBean.processData(execution)}"
                 flowable:resultVariableName="expressionResult"/>
    
    <!-- Service Task using Delegate Expression -->
    <serviceTask id="delegateTask" name="Delegate Task"
                 flowable:delegateExpression="#{myJavaDelegate}"/>
    
    <!-- Task with Listener -->
    <userTask id="userTask" name="User Task">
      <extensionElements>
        <flowable:taskListener event="create" class="groovy">
          <flowable:script><![CDATA[
            import org.flowable.engine.impl.context.Context
            
            def myBean = Context.getProcessEngineConfiguration()
                .getApplicationContext()
                .getBean("myServiceBean")
            
            myBean.handleTask(task)
          ]]></flowable:script>
        </flowable:taskListener>
      </extensionElements>
    </userTask>
    
    <endEvent id="end"/>
    
    <sequenceFlow sourceRef="start" targetRef="groovyScript"/>
    <sequenceFlow sourceRef="groovyScript" targetRef="expressionTask"/>
    <sequenceFlow sourceRef="expressionTask" targetRef="delegateTask"/>
    <sequenceFlow sourceRef="delegateTask" targetRef="userTask"/>
    <sequenceFlow sourceRef="userTask" targetRef="end"/>
    
  </process>
  
</definitions>
```

## Best Practices

**Prefer Delegate Expression over Script Tasks** when possible for better testability and type safety:

```java
@Component("myJavaDelegate")
public class MyJavaDelegate implements JavaDelegate {
    
    @Autowired
    private MyServiceBean myServiceBean;
    
    @Override
    public void execute(DelegateExecution execution) {
        String result = myServiceBean.processData(execution);
        execution.setVariable("result", result);
    }
}
```

**Use Bean Resolver** (Flowable 7.0+) for cleaner script access:

```groovy
// Cleaner syntax in Flowable 7.0+
def myBean = beans.myServiceBean
def result = myBean.processData(execution)
```

**Enable Script Debugging** in development:

```yaml
flowable:
  process-definition-location-prefix: classpath*:/processes/
  check-process-definitions: true
  scripting:
    enable-scripting: true
```

This approach gives you flexibility to use scripts when needed while maintaining Spring's dependency injection and bean management capabilities.


# JavaScript in Flowable BPMN XML

Here's how to embed JavaScript scripts in Flowable BPMN XML files:

## 1. Script Task with JavaScript

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="Examples">
  
  <process id="javascriptProcess" name="JavaScript Process">
    
    <startEvent id="start"/>
    
    <!-- JavaScript Script Task -->
    <scriptTask id="jsScriptTask" name="JavaScript Script Task" 
                scriptFormat="javascript" 
                flowable:autoStoreVariables="true">
      <script><![CDATA[
        var Context = Java.type('org.flowable.engine.impl.context.Context');
        var config = Context.getProcessEngineConfiguration();
        var appContext = config.getApplicationContext();
        
        // Get Spring bean by name
        var myBean = appContext.getBean('myServiceBean');
        
        // Get variable from execution
        var inputData = execution.getVariable('inputData');
        
        // Call bean method
        var result = myBean.processData(execution);
        
        // Set variables
        execution.setVariable('output', result);
        execution.setVariable('processed', true);
      ]]></script>
    </scriptTask>
    
    <endEvent id="end"/>
    
    <sequenceFlow sourceRef="start" targetRef="jsScriptTask"/>
    <sequenceFlow sourceRef="jsScriptTask" targetRef="end"/>
    
  </process>
  
</definitions>
```

## 2. JavaScript in Execution Listener

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="Examples">
  
  <process id="listenerProcess" name="Listener Process">
    
    <startEvent id="start">
      <extensionElements>
        <!-- Start Event Listener -->
        <flowable:executionListener event="start">
          <flowable:script scriptFormat="javascript"><![CDATA[
            var Context = Java.type('org.flowable.engine.impl.context.Context');
            var config = Context.getProcessEngineConfiguration();
            var logService = config.getApplicationContext().getBean('logService');
            
            logService.logProcessStart(execution.getProcessInstanceId());
          ]]></flowable:script>
        </flowable:executionListener>
      </extensionElements>
    </startEvent>
    
    <serviceTask id="serviceTask" name="Service Task">
      <extensionElements>
        <!-- Service Task Listeners -->
        <flowable:executionListener event="start">
          <flowable:script scriptFormat="javascript"><![CDATA[
            var auditBean = Java.type('org.flowable.engine.impl.context.Context')
              .getProcessEngineConfiguration()
              .getApplicationContext()
              .getBean('auditService');
            
            auditBean.audit('Task Started', execution.getCurrentActivityId());
          ]]></flowable:script>
        </flowable:executionListener>
        
        <flowable:executionListener event="end">
          <flowable:script scriptFormat="javascript"><![CDATA[
            var auditBean = Java.type('org.flowable.engine.impl.context.Context')
              .getProcessEngineConfiguration()
              .getApplicationContext()
              .getBean('auditService');
            
            auditBean.audit('Task Ended', execution.getCurrentActivityId());
          ]]></flowable:script>
        </flowable:executionListener>
      </extensionElements>
    </serviceTask>
    
    <endEvent id="end"/>
    
    <sequenceFlow sourceRef="start" targetRef="serviceTask"/>
    <sequenceFlow sourceRef="serviceTask" targetRef="end"/>
    
  </process>
  
</definitions>
```

## 3. JavaScript in Task Listener

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="Examples">
  
  <process id="taskListenerProcess" name="Task Listener Process">
    
    <startEvent id="start"/>
    
    <userTask id="userTask" name="Review Task" flowable:assignee="user1">
      <extensionElements>
        
        <!-- Task Create Listener -->
        <flowable:taskListener event="create">
          <flowable:script scriptFormat="javascript"><![CDATA[
            var Context = Java.type('org.flowable.engine.impl.context.Context');
            var notificationService = Context.getProcessEngineConfiguration()
              .getApplicationContext()
              .getBean('notificationService');
            
            var taskId = task.getId();
            var taskName = task.getName();
            var assignee = task.getAssignee();
            
            notificationService.sendTaskCreatedNotification(taskId, taskName, assignee);
          ]]></flowable:script>
        </flowable:taskListener>
        
        <!-- Task Assignment Listener -->
        <flowable:taskListener event="assignment">
          <flowable:script scriptFormat="javascript"><![CDATA[
            var Context = Java.type('org.flowable.engine.impl.context.Context');
            var userService = Context.getProcessEngineConfiguration()
              .getApplicationContext()
              .getBean('userService');
            
            var newAssignee = task.getAssignee();
            if (newAssignee !== null) {
              userService.notifyAssignment(newAssignee, task.getId());
            }
          ]]></flowable:script>
        </flowable:taskListener>
        
        <!-- Task Complete Listener -->
        <flowable:taskListener event="complete">
          <flowable:script scriptFormat="javascript"><![CDATA[
            var Context = Java.type('org.flowable.engine.impl.context.Context');
            var auditService = Context.getProcessEngineConfiguration()
              .getApplicationContext()
              .getBean('auditService');
            
            var taskId = task.getId();
            var completedBy = task.getAssignee();
            
            auditService.logTaskCompletion(taskId, completedBy);
            
            // Access task variables
            var outcome = task.getVariable('outcome');
            execution.setVariable('taskOutcome', outcome);
          ]]></flowable:script>
        </flowable:taskListener>
        
      </extensionElements>
    </userTask>
    
    <endEvent id="end"/>
    
    <sequenceFlow sourceRef="start" targetRef="userTask"/>
    <sequenceFlow sourceRef="userTask" targetRef="end"/>
    
  </process>
  
</definitions>
```

## 4. JavaScript in Sequence Flow Conditions

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="Examples">
  
  <process id="conditionalProcess" name="Conditional Process">
    
    <startEvent id="start"/>
    
    <exclusiveGateway id="gateway"/>
    
    <serviceTask id="approveTask" name="Approve"/>
    <serviceTask id="rejectTask" name="Reject"/>
    
    <endEvent id="end"/>
    
    <sequenceFlow sourceRef="start" targetRef="gateway"/>
    
    <!-- Sequence Flow with JavaScript Condition -->
    <sequenceFlow sourceRef="gateway" targetRef="approveTask">
      <conditionExpression xsi:type="tFormalExpression">
        <![CDATA[
          var Context = Java.type('org.flowable.engine.impl.context.Context');
          var validationService = Context.getProcessEngineConfiguration()
            .getApplicationContext()
            .getBean('validationService');
          
          var amount = execution.getVariable('amount');
          var result = validationService.isApproved(amount);
          
          result === true;
        ]]>
      </conditionExpression>
    </sequenceFlow>
    
    <sequenceFlow sourceRef="gateway" targetRef="rejectTask">
      <conditionExpression xsi:type="tFormalExpression">
        <![CDATA[
          var Context = Java.type('org.flowable.engine.impl.context.Context');
          var validationService = Context.getProcessEngineConfiguration()
            .getApplicationContext()
            .getBean('validationService');
          
          var amount = execution.getVariable('amount');
          var result = validationService.isApproved(amount);
          
          result === false;
        ]]>
      </conditionExpression>
    </sequenceFlow>
    
    <sequenceFlow sourceRef="approveTask" targetRef="end"/>
    <sequenceFlow sourceRef="rejectTask" targetRef="end"/>
    
  </process>
  
</definitions>
```

## 5. Complete Example with Multiple JavaScript Usages

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="Examples"
             xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL 
                                 http://www.omg.org/spec/BPMN/20100524/BPMN20.xsd">
  
  <process id="completeJsProcess" name="Complete JavaScript Process">
    
    <!-- Start Event with Listener -->
    <startEvent id="start">
      <extensionElements>
        <flowable:executionListener event="start">
          <flowable:script scriptFormat="javascript"><![CDATA[
            var Context = Java.type('org.flowable.engine.impl.context.Context');
            var initService = Context.getProcessEngineConfiguration()
              .getApplicationContext()
              .getBean('initializationService');
            
            var processId = execution.getProcessInstanceId();
            initService.initialize(processId);
            
            execution.setVariable('initialized', true);
          ]]></flowable:script>
        </flowable:executionListener>
      </extensionElements>
    </startEvent>
    
    <!-- Script Task -->
    <scriptTask id="dataProcessing" name="Process Data" 
                scriptFormat="javascript" 
                flowable:autoStoreVariables="true">
      <script><![CDATA[
        var Context = Java.type('org.flowable.engine.impl.context.Context');
        var dataService = Context.getProcessEngineConfiguration()
          .getApplicationContext()
          .getBean('dataProcessingService');
        
        var rawData = execution.getVariable('rawData');
        var processedData = dataService.process(rawData);
        
        execution.setVariable('processedData', processedData);
        execution.setVariable('processingDate', new java.util.Date());
      ]]></script>
    </scriptTask>
    
    <!-- User Task with Listeners -->
    <userTask id="reviewTask" name="Review Data">
      <extensionElements>
        <flowable:taskListener event="create">
          <flowable:script scriptFormat="javascript"><![CDATA[
            var Context = Java.type('org.flowable.engine.impl.context.Context');
            var emailService = Context.getProcessEngineConfiguration()
              .getApplicationContext()
              .getBean('emailService');
            
            emailService.sendTaskNotification(
              task.getId(), 
              task.getName(), 
              task.getAssignee()
            );
          ]]></flowable:script>
        </flowable:taskListener>
        
        <flowable:taskListener event="complete">
          <flowable:script scriptFormat="javascript"><![CDATA[
            var Context = Java.type('org.flowable.engine.impl.context.Context');
            var auditService = Context.getProcessEngineConfiguration()
              .getApplicationContext()
              .getBean('auditService');
            
            var decision = task.getVariable('decision');
            auditService.logDecision(task.getId(), decision);
          ]]></flowable:script>
        </flowable:taskListener>
      </extensionElements>
    </userTask>
    
    <!-- Gateway -->
    <exclusiveGateway id="decisionGateway"/>
    
    <!-- End Events -->
    <endEvent id="approvedEnd" name="Approved"/>
    <endEvent id="rejectedEnd" name="Rejected"/>
    
    <!-- Sequence Flows -->
    <sequenceFlow sourceRef="start" targetRef="dataProcessing"/>
    <sequenceFlow sourceRef="dataProcessing" targetRef="reviewTask"/>
    <sequenceFlow sourceRef="reviewTask" targetRef="decisionGateway"/>
    
    <sequenceFlow sourceRef="decisionGateway" targetRef="approvedEnd" name="Approved">
      <conditionExpression xsi:type="tFormalExpression">
        <![CDATA[
          var decision = execution.getVariable('decision');
          decision === 'APPROVED';
        ]]>
      </conditionExpression>
    </sequenceFlow>
    
    <sequenceFlow sourceRef="decisionGateway" targetRef="rejectedEnd" name="Rejected">
      <conditionExpression xsi:type="tFormalExpression">
        <![CDATA[
          var decision = execution.getVariable('decision');
          decision === 'REJECTED';
        ]]>
      </conditionExpression>
    </sequenceFlow>
    
  </process>
  
</definitions>
```

## Key Points

1. **Use `<![CDATA[...]]>`** to wrap JavaScript code to avoid XML parsing issues
2. **Script Format**: Always specify `scriptFormat="javascript"`
3. **Java Type Access**: Use `Java.type('fully.qualified.ClassName')` to access Java classes
4. **Bean Access Pattern**: Context → ProcessEngineConfiguration → ApplicationContext → getBean()
5. **Variables**: Access via `execution.getVariable()` and `execution.setVariable()`
6. **Task Access**: In task listeners, use `task.getId()`, `task.getAssignee()`, etc.

This allows you to leverage Spring beans directly in your BPMN process definitions while keeping business logic in your Java services.



# Groovy vs JavaScript in Flowable: Recommendation

## **Use Groovy** ✅

For Flowable 7.0+ with Spring Boot, **Groovy is the recommended choice**.

## Why Groovy is Better

### 1. **Native Java Interoperability**
Groovy compiles to JVM bytecode and has seamless Java integration.

```groovy
// Groovy - Clean and natural
import org.flowable.engine.impl.context.Context

def myBean = Context.getProcessEngineConfiguration()
    .getApplicationContext()
    .getBean("myServiceBean")

def result = myBean.processData(execution)
execution.setVariable("output", result)
```

```javascript
// JavaScript - Verbose with Java interop
var Context = Java.type('org.flowable.engine.impl.context.Context');
var config = Context.getProcessEngineConfiguration();
var myBean = config.getApplicationContext().getBean('myServiceBean');

var result = myBean.processData(execution);
execution.setVariable('output', result);
```

### 2. **Type Safety & IDE Support**
Groovy provides better autocomplete, refactoring, and compile-time checking.

```groovy
// Groovy - Type-safe when needed
import com.example.MyService

MyService myBean = Context.getProcessEngineConfiguration()
    .getApplicationContext()
    .getBean(MyService.class)

// IDE knows the return type and methods
String result = myBean.processData(execution)
```

### 3. **Direct Spring Bean Access (Flowable 7.0+)**

```groovy
// Groovy - Built-in beans resolver
def myBean = beans.myServiceBean
def anotherBean = beans.emailService

// Much cleaner than JavaScript equivalent
```

### 4. **Better Collection Handling**

```groovy
// Groovy - Expressive collections
def approvers = ['user1', 'user2', 'user3']
approvers.each { user ->
    taskService.addCandidateUser(task.id, user)
}

def amounts = execution.getVariable('items').collect { it.amount }
def total = amounts.sum()
```

```javascript
// JavaScript - More verbose
var approvers = ['user1', 'user2', 'user3'];
for (var i = 0; i < approvers.length; i++) {
    taskService.addCandidateUser(task.getId(), approvers[i]);
}
```

### 5. **Null Safety**

```groovy
// Groovy - Safe navigation operator
def email = execution.getVariable('user')?.email ?: 'default@example.com'
def name = execution.getVariable('customer')?.profile?.name
```

### 6. **GString Interpolation**

```groovy
// Groovy - Clean string templates
def message = "Hello ${userName}, your order ${orderId} is ready"
execution.setVariable('notification', message)
```

```javascript
// JavaScript - Concatenation or templates
var message = "Hello " + userName + ", your order " + orderId + " is ready";
```

## Complete Groovy Examples

### Script Task Example

```xml
<scriptTask id="processOrder" name="Process Order" 
            scriptFormat="groovy" flowable:autoStoreVariables="true">
  <script><![CDATA[
    import org.flowable.engine.impl.context.Context
    import com.example.OrderService
    
    // Get Spring bean
    def orderService = Context.getProcessEngineConfiguration()
        .getApplicationContext()
        .getBean(OrderService.class)
    
    // Or using beans resolver (Flowable 7.0+)
    def orderService = beans.orderService
    
    // Get process variables
    def orderId = execution.getVariable('orderId')
    def customerId = execution.getVariable('customerId')
    
    // Call service method
    def order = orderService.processOrder(orderId, customerId)
    
    // Set result variables
    execution.setVariable('orderTotal', order.total)
    execution.setVariable('orderStatus', order.status)
    
    // Conditional logic
    if (order.total > 1000) {
        execution.setVariable('requiresApproval', true)
    }
  ]]></script>
</scriptTask>
```

### Task Listener Example

```xml
<userTask id="approveOrder" name="Approve Order">
  <extensionElements>
    <flowable:taskListener event="create" class="groovy">
      <flowable:script><![CDATA[
        import org.flowable.engine.impl.context.Context
        
        // Get notification service
        def notificationService = beans.notificationService
        
        // Get task variables
        def assignee = task.assignee
        def orderTotal = execution.getVariable('orderTotal')
        
        // Send notification
        notificationService.sendEmail(
            assignee,
            "Order Approval Required",
            "Please approve order with total: $orderTotal"
        )
        
        // Set task priority based on amount
        if (orderTotal > 5000) {
            task.priority = 100
        }
      ]]></flowable:script>
    </flowable:taskListener>
    
    <flowable:taskListener event="complete" class="groovy">
      <flowable:script><![CDATA[
        def auditService = beans.auditService
        def decision = task.getVariable('approved')
        
        auditService.log(
            'ORDER_APPROVAL',
            task.id,
            [decision: decision, user: task.assignee]
        )
      ]]></flowable:script>
    </flowable:taskListener>
  </extensionElements>
</userTask>
```

### Execution Listener Example

```xml
<process id="orderProcess" name="Order Process">
  <extensionElements>
    <flowable:executionListener event="start" class="groovy">
      <flowable:script><![CDATA[
        def metricsService = beans.metricsService
        metricsService.incrementCounter('processes.started')
        
        execution.setVariable('startTime', new Date())
      ]]></flowable:script>
    </flowable:executionListener>
    
    <flowable:executionListener event="end" class="groovy">
      <flowable:script><![CDATA[
        def metricsService = beans.metricsService
        def startTime = execution.getVariable('startTime')
        def duration = new Date().time - startTime.time
        
        metricsService.recordDuration('processes.order', duration)
      ]]></flowable:script>
    </flowable:executionListener>
  </extensionElements>
  
  <!-- process definition -->
</process>
```

### Advanced Groovy Features

```groovy
// Complex data manipulation
def items = execution.getVariable('orderItems') as List

// Filter and transform
def expensiveItems = items.findAll { it.price > 100 }
def totalExpensive = expensiveItems.sum { it.price * it.quantity }

// Grouping
def itemsByCategory = items.groupBy { it.category }

// Map to new structure
def summary = items.collect { 
    [name: it.name, total: it.price * it.quantity] 
}

// Set multiple variables at once
execution.setVariables([
    'expensiveItemCount': expensiveItems.size(),
    'expensiveTotal': totalExpensive,
    'itemsByCategory': itemsByCategory,
    'summary': summary
])
```

## Dependencies Required

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Flowable Spring Boot Starter -->
    <dependency>
        <groupId>org.flowable</groupId>
        <artifactId>flowable-spring-boot-starter</artifactId>
        <version>7.0.1</version>
    </dependency>
    
    <!-- Groovy (usually included transitively, but explicit is better) -->
    <dependency>
        <groupId>org.codehaus.groovy</groupId>
        <artifactId>groovy-jsr223</artifactId>
        <version>3.0.19</version>
    </dependency>
</dependencies>
```

## Configuration

```yaml
# application.yml
flowable:
  process-definition-location-prefix: classpath*:/processes/
  check-process-definitions: true
  database-schema-update: true
  async-executor-activate: true
  scripting:
    enable-scripting: true
```

## When to Use JavaScript

Use JavaScript **only** if:
- Your team has strong JavaScript background but no Java/Groovy experience
- You're integrating with existing JavaScript-based systems
- You need exact ECMAScript compatibility

Even then, consider using Java delegates instead for better maintainability.

## Summary

**Choose Groovy** for:
- ✅ Better Java/Spring integration
- ✅ Type safety and IDE support
- ✅ Cleaner, more expressive syntax
- ✅ Better debugging experience
- ✅ Native collection handling
- ✅ Stronger ecosystem in JVM world

Groovy is the pragmatic choice for Flowable with Spring Boot.