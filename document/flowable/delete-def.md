# Delete

In Flowable BPM, **deleting a process definition does NOT automatically delete running process instances**, but the behavior depends on how you delete it:

## Deletion Options:

**1. Standard Delete (cascade = false) - Default:**
```java
repositoryService.deleteDeployment(deploymentId);
```
- **Fails if there are running process instances**
- Throws `FlowableException`: "Cannot delete deployment ... because there are running process instances"
- **Safe approach** - prevents accidental deletion

**2. Cascade Delete (cascade = true):**
```java
repositoryService.deleteDeployment(deploymentId, true);
```
- **Deletes the deployment AND all related process instances** (running and completed)
- Also deletes:
    - All process instances (running and historic)
    - All tasks
    - All variables
    - All execution data
- **Dangerous** - use with caution

**3. Delete Only Process Definition (keeping instances):**
```java
// Delete specific process definition version
repositoryService.deleteProcessDefinition(processDefinitionId);
```
- Deletes the process definition metadata
- **Running instances continue to execute** using the cached definition
- New instances cannot be started from this definition
- Throws error if you try without cascade and instances exist

## What Happens to Running Instances:

**If you use cascade delete:**
- All process instances are **terminated and deleted**
- All related data is removed
- Cannot be recovered

**If delete fails (no cascade):**
- Running instances **remain intact**
- They continue executing normally
- You must handle them first

**If you successfully delete without cascade (no instances exist):**
- Only the definition is removed
- No impact on anything

## Best Practices:

**1. Check for Running Instances First:**
```java
long count = runtimeService.createProcessInstanceQuery()
    .processDefinitionId(processDefinitionId)
    .count();

if (count > 0) {
    System.out.println("Cannot delete: " + count + " instances are running");
    // Handle running instances first
} else {
    repositoryService.deleteDeployment(deploymentId);
}
```

**2. Suspend Definition Instead of Deleting:**
```java
// Suspend process definition - prevents new instances
repositoryService.suspendProcessDefinitionById(processDefinitionId);

// Wait for running instances to complete
// Then delete later
```

**3. Complete or Cancel Running Instances First:**
```java
// Get all running instances
List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
    .processDefinitionId(processDefinitionId)
    .list();

// Delete each instance
for (ProcessInstance instance : instances) {
    runtimeService.deleteProcessInstance(instance.getId(), "Definition being removed");
}

// Now safe to delete deployment
repositoryService.deleteDeployment(deploymentId);
```

**4. Safe Cascade Pattern:**
```java
public void safeDeleteDeployment(String deploymentId) {
    try {
        // Try without cascade first
        repositoryService.deleteDeployment(deploymentId);
        System.out.println("Deployment deleted successfully");
    } catch (FlowableException e) {
        // Ask user for confirmation
        System.out.println("There are running instances. Force delete? (yes/no)");
        
        if (userConfirms()) {
            repositoryService.deleteDeployment(deploymentId, true);
            System.out.println("Deployment and all instances deleted");
        }
    }
}
```

## Impact Summary:

| Action | Running Instances | Historic Data | New Instances |
|--------|------------------|---------------|---------------|
| Delete (no cascade) | ❌ Fails if exist | No change | ❌ Can't start |
| Delete (cascade) | ✅ Deleted | ✅ Deleted | ❌ Can't start |
| Suspend definition | ✅ Keep running | No change | ❌ Can't start |
| Delete instance manually | Selected ones deleted | Selected ones deleted | ✅ Can still start |

## Recommendations:

1. **Never use cascade delete in production** without explicit user confirmation
2. **Suspend the definition first** to prevent new instances
3. **Wait for running instances to complete naturally**
4. **Use migration** if you need to update running instances to a new version
5. **Keep historic data** for audit purposes - consider soft deletes instead

**Version Migration (Alternative to Deletion):**
```java
// Instead of deleting, migrate to new version
ProcessInstanceMigrationBuilder migration = runtimeService
    .createProcessInstanceMigrationBuilder()
    .migrateToProcessDefinition(newProcessDefinitionId);

runtimeService.migrateProcessInstance(processInstanceId, migration);
```

So yes, Flowable **allows** you to delete definitions and cascade delete instances, but it **protects** you by default (requires explicit cascade flag).


# Cached Instances

**Yes, if you restart Flowable after deleting the process definition, running instances will likely break.**

## What Happens on Restart:

**Before Restart:**
- Running instances use **in-memory cached** definition
- They continue executing normally
- Everything works fine

**After Restart:**
- Cache is cleared
- Flowable tries to **reload process definitions from database**
- **Definition is missing** (you deleted it)
- Running instances **fail** when they try to execute next step

## Specific Failure Scenarios:

**1. When Instance Tries to Continue:**
```java
// Error when instance wakes up (timer, async job, user task completion)
FlowableObjectNotFoundException: Process definition with id 'myProcess:1:12345' was not found
```

**2. Query/Display Failures:**
```java
// Viewing process diagram fails
runtimeService.getProcessDiagram(processInstanceId);
// Throws: Process definition not found

// Getting activity info fails
runtimeService.getActiveActivityIds(executionId);
// Throws: Process definition not found
```

**3. Task Completion Failures:**
```java
// User tries to complete a task
taskService.complete(taskId);
// May throw: Unable to find process definition for task
```

## Database State After Deletion:

When you delete a process definition (without cascade):

**What's Deleted:**
- `ACT_RE_PROCDEF` - Process definition record
- `ACT_RE_DEPLOYMENT` - Deployment record
- `ACT_GE_BYTEARRAY` - BPMN XML and resources

**What Remains:**
- `ACT_RU_EXECUTION` - Runtime execution data (references deleted definition ID)
- `ACT_RU_TASK` - Active tasks (references deleted definition ID)
- `ACT_RU_VARIABLE` - Process variables
- Orphaned data with **broken foreign key references**

## Proof of Concept:

```java
// Step 1: Start process
ProcessInstance instance = runtimeService.startProcessInstanceByKey("myProcess");
String instanceId = instance.getId();

// Step 2: Delete definition (assuming no validation)
repositoryService.deleteDeployment(deploymentId);

// Step 3: Instances still work (using cache)
taskService.complete(taskId); // Works!

// Step 4: Restart Flowable application
// ... restart ...

// Step 5: Try to continue
taskService.complete(nextTaskId); // FAILS!
// FlowableObjectNotFoundException: Process definition 'myProcess:1:xxx' not found
```

## Solutions:

**1. Don't Delete Definitions with Running Instances (Recommended):**
```java
public void safeDelete(String deploymentId) {
    long runningCount = runtimeService.createProcessInstanceQuery()
        .deploymentId(deploymentId)
        .count();
    
    if (runningCount > 0) {
        throw new IllegalStateException(
            "Cannot delete: " + runningCount + " instances still running"
        );
    }
    
    repositoryService.deleteDeployment(deploymentId);
}
```

**2. Use Cascade Delete (Deletes Everything):**
```java
// Deletes definition AND all instances
repositoryService.deleteDeployment(deploymentId, true);
// After restart: no orphaned data, no errors
```

**3. Suspend Definition Instead:**
```java
// Prevents new instances, keeps existing ones safe
repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);

// Later, after all instances complete:
repositoryService.deleteDeployment(deploymentId);
```

**4. Manually Clean Up Instances First:**
```java
// Cancel all running instances
List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
    .processDefinitionId(processDefinitionId)
    .list();

for (ProcessInstance instance : instances) {
    runtimeService.deleteProcessInstance(
        instance.getId(), 
        "Definition being removed"
    );
}

// Now safe to delete
repositoryService.deleteDeployment(deploymentId);
```

**5. Redeploy Before Restart (Recovery):**
```java
// If you accidentally deleted, redeploy the same version
repositoryService.createDeployment()
    .addClasspathResource("processes/myProcess.bpmn20.xml")
    .deploy();

// Instances will work again after restart
```

## What Flowable Does NOT Do:

- ❌ Does NOT store complete process definition with each instance
- ❌ Does NOT keep definition snapshots per instance
- ❌ Does NOT automatically migrate instances when definition deleted
- ❌ Does NOT prevent restart with orphaned instances (just fails later)

## Best Practice - Version Management:

```java
@Service
public class ProcessDefinitionService {
    
    public void deleteOldVersion(String processKey, int version) {
        // 1. Check for running instances
        ProcessDefinition definition = repositoryService
            .createProcessDefinitionQuery()
            .processDefinitionKey(processKey)
            .processDefinitionVersion(version)
            .singleResult();
        
        if (definition == null) {
            throw new NotFoundException("Definition not found");
        }
        
        long count = runtimeService.createProcessInstanceQuery()
            .processDefinitionId(definition.getId())
            .count();
        
        if (count > 0) {
            throw new IllegalStateException(
                "Cannot delete version " + version + 
                ": " + count + " instances still running. " +
                "Wait for completion or migrate to newer version."
            );
        }
        
        // 2. Safe to delete
        repositoryService.deleteDeployment(
            definition.getDeploymentId()
        );
    }
}
```

## Summary:

| Scenario | Works Before Restart | Works After Restart |
|----------|---------------------|---------------------|
| Definition deleted, instances running | ✅ Yes (cached) | ❌ No (breaks) |
| Cascade delete (all deleted) | N/A (no instances) | ✅ Yes (clean state) |
| Definition suspended | ✅ Yes | ✅ Yes |
| Instances deleted first | ✅ Yes | ✅ Yes |

**Answer: Yes, they will break after restart.** Always handle running instances before deleting definitions.