In Flowable, you can add script execution on a sequence flow using **Condition Expressions** or by adding **Execution Listeners**. Here are the approaches:

## 1. Using Execution Listeners on Sequence Flow

You can attach an execution listener to a sequence flow that executes a script when the flow is taken:

```xml
<sequenceFlow id="flow1" sourceRef="task1" targetRef="task2">
  <extensionElements>
    <flowable:executionListener event="take" class="org.flowable.engine.delegate.ExecutionListener">
      <flowable:script>
        <flowable:scriptFormat>groovy</flowable:scriptFormat>
        <flowable:script>
          <![CDATA[
            println "Sequence flow executed!"
            execution.setVariable("flowTaken", true)
            // Your script logic here
          ]]>
        </flowable:script>
      </flowable:script>
    </flowable:executionListener>
  </extensionElements>
</sequenceFlow>
```

## 2. Using Script as Condition Expression

If you want to evaluate a condition AND execute script logic:

```xml
<sequenceFlow id="flow1" sourceRef="gateway1" targetRef="task2">
  <conditionExpression xsi:type="tFormalExpression">
    <![CDATA[
      ${
        var result = myVariable > 10;
        execution.setVariable("conditionChecked", true);
        result
      }
    ]]>
  </conditionExpression>
</sequenceFlow>
```

## 3. Programmatic Approach (Java)

Create a custom execution listener class:

```java
public class SequenceFlowListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        System.out.println("Sequence flow executed: " + execution.getCurrentFlowElement().getId());
        
        // Your logic here
        execution.setVariable("timestamp", new Date());
    }
}
```

Then reference it in your BPMN:

```xml
<sequenceFlow id="flow1" sourceRef="task1" targetRef="task2">
  <extensionElements>
    <flowable:executionListener event="take" 
                                class="com.example.SequenceFlowListener"/>
  </extensionElements>
</sequenceFlow>
```

## Key Points

- The **event="take"** triggers when the sequence flow is traversed
- You can use Groovy, JavaScript, or JUEL expressions for scripting
- The `execution` object gives you access to process variables and context
- Execution listeners on sequence flows are useful for logging, auditing, or setting transition-specific variables

Which approach fits your use case best?


If your IDE doesn't recognize `flowable:taskListener`, here are the solutions:

## 1. Add Flowable Schema Location to Your BPMN File

Update your definitions element to include the schema location:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:flowable="http://flowable.org/bpmn"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
             xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL 
                                 http://www.omg.org/spec/BPMN/20100524/BPMN20.xsd"
             targetNamespace="http://flowable.org/processdef"
             typeLanguage="http://www.w3.org/2001/XMLSchema"
             expressionLanguage="http://www.w3.org/1999/XPath">
```

## 2. Add Flowable Maven Dependencies

Make sure you have Flowable dependencies in your `pom.xml`:

```xml
<dependency>
    <groupId>org.flowable</groupId>
    <artifactId>flowable-engine</artifactId>
    <version>7.0.1</version> <!-- or your version -->
</dependency>
```

## 3. Download Flowable XSD Schema Manually

The Flowable BPMN schema might not be publicly available at a URL. You can:

**Option A: Extract from JAR**
1. Find `flowable-bpmn-model-{version}.jar` in your Maven repository
2. Extract the XSD file (usually in `org/flowable/impl/bpmn/parser/`)
3. Place it in your project resources
4. Reference it locally:

```xml
xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL 
                    http://www.omg.org/spec/BPMN/20100524/BPMN20.xsd
                    http://flowable.org/bpmn 
                    ./schemas/flowable-bpmn.xsd"
```

**Option B: Use Without Schema Validation**

Remove schema validation and just use the namespace:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/processdef">
```

## 4. IDE-Specific Solutions

### For IntelliJ IDEA:
1. Go to **Settings > Languages & Frameworks > Schemas and DTDs**
2. Add external schema mapping:
   - URI: `http://flowable.org/bpmn`
   - File: Point to local XSD if you have it

### For Eclipse:
1. Go to **Window > Preferences > XML > XML Catalog**
2. Add new catalog entry for Flowable namespace

### For VS Code:
Install the **Flowable** or **BPMN** extension from the marketplace

## 5. Use Flowable Modeler (Recommended)

Instead of hand-coding XML, use Flowable's visual modeler:
- **Flowable Modeler** (web-based, part of Flowable UI)
- **Flowable Eclipse Plugin**
- **Camunda Modeler** (also supports Flowable with some adjustments)

## Quick Workaround

If IDE recognition is not critical and the BPMN works at runtime, you can:

1. Ignore IDE warnings
2. Or add this comment to suppress validation:

```xml
<!-- @formatter:off -->
<extensionElements>
    <flowable:taskListener event="create" class="com.example.MyListener"/>
</extensionElements>
<!-- @formatter:on -->
```

The most important thing is that **Flowable engine will recognize and execute the elements correctly at runtime**, even if your IDE shows warnings.

Which IDE are you using?