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