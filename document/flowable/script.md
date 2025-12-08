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