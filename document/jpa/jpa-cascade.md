# Cascade Types

## All 

In JPA, the **`cascade`** attribute in associations (e.g., `@OneToMany`, `@ManyToOne`, etc.) is used to propagate operations from a parent entity to its associated child entities. Each **cascade type** defines a specific operation to cascade from the parent to the child entities.

Here’s a detailed explanation of the **cascade types**, their meanings, and the differences between them:

---

### **Cascade Types**

#### **1. `CascadeType.ALL`**
- **Definition:** Applies all cascade operations (`PERSIST`, `MERGE`, `REMOVE`, `REFRESH`, `DETACH`) to the associated entities.
- **Use Case:** When you want all operations performed on the parent to propagate to the child entities.

#### Example:
```java
@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
private List<Child> children;
```
- Saving the parent (`entityManager.persist(parent)`) will save all associated children.
- Deleting the parent (`entityManager.remove(parent)`) will delete all associated children.

---

#### **2. `CascadeType.PERSIST`**
- **Definition:** Propagates the `PERSIST` operation from the parent to the child entities.
- **Effect:** When the parent is persisted, all associated child entities are also persisted.
- **Use Case:** When child entities should be created and saved together with the parent.

#### Example:
```java
@OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
private List<Child> children;
```
- If the parent is persisted (`entityManager.persist(parent)`), all unsaved child entities will also be persisted.
- **But:** If the parent is deleted (`entityManager.remove(parent)`), the child entities will **not** be deleted.

---

#### **3. `CascadeType.MERGE`**
- **Definition:** Propagates the `MERGE` operation from the parent to the child entities.
- **Effect:** When the parent is merged (updated), all associated child entities are also merged.
- **Use Case:** When you want to update both the parent and its children in the database.

#### Example:
```java
@OneToMany(mappedBy = "parent", cascade = CascadeType.MERGE)
private List<Child> children;
```
- If the parent is merged (`entityManager.merge(parent)`), all detached child entities will also be merged into the persistence context.
- **But:** If the parent is saved for the first time (`entityManager.persist(parent)`), the child entities will **not** be persisted unless explicitly persisted.

---

#### **4. `CascadeType.REMOVE`**
- **Definition:** Propagates the `REMOVE` operation from the parent to the child entities.
- **Effect:** When the parent is deleted, all associated child entities are also deleted.
- **Use Case:** When deleting the parent should always delete its child entities.

#### Example:
```java
@OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
private List<Child> children;
```
- If the parent is deleted (`entityManager.remove(parent)`), all associated child entities will also be removed from the database.
- **But:** If the parent is updated, the child entities will remain unaffected.

---

#### **5. `CascadeType.DETACH`**
- **Definition:** Propagates the `DETACH` operation from the parent to the child entities.
- **Effect:** When the parent is detached from the persistence context, all associated child entities are also detached.
- **Use Case:** When detaching the parent entity should also detach its children to avoid unintentional updates.

#### Example:
```java
@OneToMany(mappedBy = "parent", cascade = CascadeType.DETACH)
private List<Child> children;
```
- If the parent is detached (`entityManager.detach(parent)`), all associated child entities will also be detached from the persistence context.
- **But:** If the parent is persisted, the child entities will **not** be saved automatically.

---

#### **6. `CascadeType.REFRESH`**
- **Definition:** Propagates the `REFRESH` operation from the parent to the child entities.
- **Effect:** When the parent entity is refreshed from the database, all associated child entities are also refreshed.
- **Use Case:** When you want changes in the database to be reflected in both the parent and its children.

#### Example:
```java
@OneToMany(mappedBy = "parent", cascade = CascadeType.REFRESH)
private List<Child> children;
```
- If the parent is refreshed (`entityManager.refresh(parent)`), all associated child entities will also be refreshed.
- **But:** If the parent is saved or updated, the child entities will remain unaffected.

---

### **Differences Between Cascade Types**

| **Cascade Type** | **Description**                                                                                 | **Effect**                                                                                       | **Example Use Case**                                                                                   |
|-------------------|-------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| `ALL`             | Includes all operations: `PERSIST`, `MERGE`, `REMOVE`, `DETACH`, `REFRESH`                     | Any operation on the parent will cascade to the children.                                        | When you want all operations on the parent to affect the children.                                      |
| `PERSIST`         | Saves child entities when the parent is persisted.                                             | Only propagates `persist` operation.                                                            | When child entities are new and need to be saved with the parent.                                       |
| `MERGE`           | Updates child entities when the parent is updated or merged.                                   | Only propagates `merge` operation.                                                              | When child entities are already in the database and need to be updated with the parent.                 |
| `REMOVE`          | Deletes child entities when the parent is deleted.                                             | Only propagates `remove` operation.                                                             | When deleting the parent should always delete its children.                                             |
| `DETACH`          | Detaches child entities when the parent is detached from the persistence context.              | Only propagates `detach` operation.                                                             | When you want to detach the parent and all its children from the persistence context.                   |
| `REFRESH`         | Refreshes child entities when the parent is refreshed (reloaded from the database).            | Only propagates `refresh` operation.                                                            | When you need to refresh both the parent and its children to reflect the latest database state.         |

---

### **Practical Example: Combining Cascade Types**

#### Example Use Case:
- A **`Parent`** entity has a `@OneToMany` relationship with a **`Child`** entity.
- When the parent is saved, the children should also be saved.
- When the parent is deleted, the children should also be deleted.
- When the parent is updated, the children should also be updated.

#### Code Example:
```java
@Entity
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private List<Child> children = new ArrayList<>();

    // Constructors, getters, setters, etc.
}

@Entity
public class Child {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Parent parent;

    // Constructors, getters, setters, etc.
}
```

#### Behavior:
1. When a `Parent` is persisted, all new `Child` entities in the `children` list will also be saved.
2. When a `Parent` is merged, the `Child` entities will also be updated.
3. When a `Parent` is deleted, all associated `Child` entities will also be removed.

---

### **Best Practices**

1. Use **`CascadeType.ALL`** only when you are sure all operations on the parent should cascade to the children.
2. Avoid cascading **`REMOVE`** if child entities are shared with other parent entities, as it may delete data unintentionally.
3. Use **helper methods** (like `addChild` or `removeChild`) to maintain bidirectional synchronization between parent and child entities.

By understanding the differences between cascade types, you can design relationships in your application more effectively and avoid unintended side effects!

## Does Persist includes Merge

No, **`CascadeType.PERSIST` does not include `CascadeType.MERGE`**. These are two distinct operations in JPA, and they behave differently.

Here’s a detailed explanation of the difference between `PERSIST` and `MERGE`:

---

### **1. `CascadeType.PERSIST`**

- **Definition:** Propagates the `persist` operation from the parent to the associated child entities.
- **Behavior:**
    - If the parent entity is persisted (saved for the first time) using `EntityManager.persist()`, all associated child entities that are not yet in the database will also be persisted.
    - **Does not affect existing (already persisted) entities.**
    - **Does not update detached entities.**
- **Key Point:** `CascadeType.PERSIST` works only for new entities that are being inserted into the database.

#### Example:
```java
Parent parent = new Parent();
Child child = new Child();
parent.addChild(child); // Associate child with parent

entityManager.persist(parent); // Saves both the parent and the child
```

- If `parent` and `child` are new (not yet in the database), both are saved.
- If `parent` or `child` are already in the database, calling `persist()` will throw an exception (`EntityExistsException`).

---

### **2. `CascadeType.MERGE`**

- **Definition:** Propagates the `merge` operation from the parent to the associated child entities.
- **Behavior:**
    - If the parent entity is merged (updated or reattached to the persistence context) using `EntityManager.merge()`, all associated child entities will also be merged.
    - **Works for both detached and new entities.**
    - Updates the database with the current state of the parent and child entities.
- **Key Point:** `CascadeType.MERGE` is used for updating existing entities or reattaching detached entities.

#### Example:
```java
Parent parent = entityManager.find(Parent.class, 1L); // Load a parent entity
Child child = new Child();
parent.addChild(child); // Associate a new child with the parent

entityManager.merge(parent); // Updates the parent and merges the child into the database
```

- If `parent` is already in the database and `child` is new, `merge()` will insert the new `child` into the database and update the `parent`.
- If `parent` and `child` are both detached, `merge()` will reattach them to the persistence context and update their states in the database.

---

### **Key Differences**

| **Feature**               | **`CascadeType.PERSIST`**                                         | **`CascadeType.MERGE`**                                           |
|---------------------------|-------------------------------------------------------------------|-------------------------------------------------------------------|
| **Operation**             | Propagates `persist` (insert new entities).                      | Propagates `merge` (update or reattach entities).                |
| **Applies To**            | Only new entities (not yet in the database).                     | Both detached and new entities.                                  |
| **Supports Updates?**     | No, it only inserts new entities.                                | Yes, it updates existing entities in the database.               |
| **Error for Existing Data?** | Throws `EntityExistsException` if the entity already exists.     | Safely updates or reattaches entities, even if they exist.        |
| **Use Case**              | Save a new parent and its new children in one call.              | Update an existing parent and its associated children.            |

---

### **Does `PERSIST` Include `MERGE`?**

No, `PERSIST` and `MERGE` are separate operations:

- **`PERSIST`**: Only for saving new entities.
- **`MERGE`**: For updating or reattaching entities (can handle both new and detached entities).

If you want both behaviors (insert for new entities and update for existing ones), you must explicitly include both `CascadeType.PERSIST` and `CascadeType.MERGE` in the relationship:

#### Example:
```java
@OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
private List<Child> children;
```

This ensures:
1. New child entities are saved when the parent is persisted.
2. Existing or detached child entities are updated when the parent is merged.

---

### **Practical Example**

#### Entities:
```java
@Entity
public class Parent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Child> children = new ArrayList<>();

    // Add child helper method
    public void addChild(Child child) {
        this.children.add(child);
        child.setParent(this);
    }
}

@Entity
public class Child {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Parent parent;
}
```

#### Application Code:
```java
// 1. Persist new entities
Parent parent = new Parent();
Child child1 = new Child();
Child child2 = new Child();
parent.addChild(child1);
parent.addChild(child2);

entityManager.persist(parent); // Saves both parent and children

// 2. Merge existing and new entities
Parent existingParent = entityManager.find(Parent.class, 1L); // Load an existing parent
Child newChild = new Child();
existingParent.addChild(newChild);

entityManager.merge(existingParent); // Updates parent and inserts new child
```

---

### **Summary**

- **`PERSIST` does not include `MERGE`.**
- Use `CascadeType.PERSIST` to cascade save operations for new entities.
- Use `CascadeType.MERGE` to cascade update or reattach operations for detached or existing entities.
- If you need both behaviors, explicitly include both `CascadeType.PERSIST` and `CascadeType.MERGE`.