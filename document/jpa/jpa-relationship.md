# Side Nature

In JPA, the **`@OneToOne`** relationship determines where the foreign key (FK) is stored based on the configuration of the mapping. You can control where the FK is stored by configuring the **owning side** of the relationship. The owning side is the side that contains and manages the foreign key column in the database.

---

### **Controlling Where the Foreign Key is Stored**

1. **Owning Side vs. Inverse (MappedBy) Side in `@OneToOne`**
    - The **owning side** is responsible for persisting and managing the foreign key column.
    - The **inverse side** (defined with `mappedBy`) does not contain the foreign key column and simply maps back to the owning side.

   You can control where the FK is stored by deciding which entity is the owning side and which is the inverse side.

#### **Example: Storing the FK in Table A**

```java
@Entity
public class A {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "b_id") // FK column in A referencing B
    private B b;
}
```

```java
@Entity
public class B {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "b") // Inverse side
    private A a;
}
```

In this example:
- The foreign key (`b_id`) is stored in table **A** because the `@JoinColumn` annotation is on the `A` entity.
- The `mappedBy = "b"` in `B` indicates that `B` is the inverse side of the relationship.
- 如果你是很确定使用 root entity 模式 并且你的 one 端就是 root entity ，推荐 join column ，
- 如果不是严格 root entity 模式， 则可能从 many 端单独存储， 推荐 mappedBy ， 单需要 PostPersist 和 postUpdate 两个annotation 来处理 mappedBy 的值

#### **Example: Storing the FK in Table B**

```java
@Entity
public class A {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "a") // Inverse side
    private B b;
}
```

```java
@Entity
public class B {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "a_id") // FK column in B referencing A
    private A a;
}
```

In this example:
- The foreign key (`a_id`) is stored in table **B** because the `@JoinColumn` annotation is on the `B` entity.
- The `mappedBy = "a"` in `A` indicates that `A` is the inverse side of the relationship.

---

### **Key Annotations for Controlling FK**
1. **`@JoinColumn`**
    - Used to specify the foreign key column.
    - It is specified on the owning side of the relationship.
    - The table corresponding to the owning side will have the foreign key column.

2. **`mappedBy`**
    - Used on the inverse side to indicate the field in the owning side that maps the relationship.
    - The inverse side does not contain the foreign key column.

---

### **sideNature of ForeignKeyDescriptor**

The **`sideNature`** of a `ForeignKeyDescriptor` refers to the **nature of the relationship side** in Hibernate. It helps Hibernate understand whether the relationship is:

1. **Owning Side**: The side responsible for managing the foreign key.
2. **Inverse Side**: The side mapped by the owning side.

In technical terms:
- Hibernate uses a `ForeignKeyDescriptor` to define how the foreign key is mapped between the two entities.
- The `sideNature` determines whether the foreign key is located on the owning side or the inverse side of the relationship.

#### Example in Hibernate:
- If `sideNature` is set to **`OWNING`**, it means the side is responsible for persisting and managing the foreign key.
- If `sideNature` is set to **`INVERSE`**, it means the foreign key is **not managed** by this side but instead points to the owning side.

This is an internal detail in Hibernate that corresponds to how it builds the relationship mapping.

---

### **Summary**
- **Controlling FK Location**: Use `@JoinColumn` on the owning side (the table storing the FK) and `mappedBy` on the inverse side.
- **sideNature of ForeignKeyDescriptor**: Defines whether the side is the owning side (`OWNING`) or the inverse side (`INVERSE`) of the relationship in Hibernate's internal mapping logic.

In Hibernate, the concept of **side nature** for a `ForeignKeyDescriptor` involves two enums: **`KEY`** and **`TARGET`**. These enums are used internally by Hibernate to describe the **nature of the relationship** between two entities in the context of a foreign key. Let’s break them down in detail:

---

### **1. `KEY` Side**
- The **`KEY`** side represents the **side that holds the foreign key column** in the database.
- This is the **owning side** of the relationship.
- The entity on the `KEY` side is the one responsible for managing the foreign key and persisting its value to the database.

#### Example:
Consider the following `@OneToOne` relationship:

```java
@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "desk_id") // FK column in Employee
    private Desk desk;
}
```

```java
@Entity
public class Desk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "desk") // Inverse side
    private Employee employee;
}
```

Here:
- **`Employee`** is the `KEY` side because it contains the `@JoinColumn` annotation (`desk_id` is stored in the `Employee` table).
- The `KEY` side is responsible for managing the foreign key column.

In Hibernate's internal mapping structure, the `KEY` side refers to the **entity that owns the foreign key in the database schema**.

---

### **2. `TARGET` Side**
- The **`TARGET`** side represents the **target entity being referenced by the foreign key**.
- This is the **inverse side** of the relationship.
- The `TARGET` side does not manage the foreign key directly; instead, it is **mapped by** the `KEY` side.

#### Example:
Using the same example above:
- **`Desk`** is the `TARGET` side because it is referenced by the foreign key column `desk_id` in the `Employee` table.
- The `Desk` entity is mapped by the `mappedBy = "desk"` attribute in the `Employee` entity.

In Hibernate's internal mapping, the `TARGET` side refers to the **entity being pointed to by the foreign key**.

---

### **How `KEY` and `TARGET` Work Together**
The `KEY` and `TARGET` enums are part of Hibernate's `ForeignKeyDescriptor` and describe the two sides of a foreign key relationship in the following way:

1. **KEY Side**:
    - This is the side where the foreign key column physically resides in the database.
    - It is responsible for managing the relationship.

2. **TARGET Side**:
    - This is the side that the foreign key points to (the referenced entity).
    - It is the inverse side of the relationship.

---

### **Practical Example for Understanding `KEY` and `TARGET`**
Let’s consider a **`@OneToOne`** relationship between `User` and `Profile`:

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "profile_id") // FK column in User
    private Profile profile;
}
```

```java
@Entity
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "profile") // Inverse side
    private User user;
}
```

- The `User` table contains the `profile_id` foreign key, so `User` is the **KEY** side.
- The `Profile` entity is the one being referenced by the `profile_id` foreign key, so `Profile` is the **TARGET** side.

---

### **Summary of `KEY` and `TARGET`**
| **Side**      | **Description**                                                                 | **Example**            |
|---------------|---------------------------------------------------------------------------------|------------------------|
| **`KEY`**     | The side that holds the foreign key column and manages the relationship.        | `User` in the example. |
| **`TARGET`**  | The side being referenced by the foreign key, i.e., the inverse side.           | `Profile` in the example. |

---

### **Why Does Hibernate Need This?**
Hibernate uses the `KEY` and `TARGET` enums to:
1. Determine where the foreign key column is stored.
2. Manage the relationship correctly during persistence, updates, and deletions.
3. Ensure that the owning (KEY) side and the inverse (TARGET) side are synchronized in memory and in the database.

This internal distinction allows Hibernate to efficiently handle both sides of the relationship in accordance with the JPA specification.