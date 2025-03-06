# SOLID

The **SOLID principles** are a set of design principles in object-oriented programming that help developers create more maintainable, understandable, and flexible software. Introduced by **Robert C. Martin (Uncle Bob)**, these principles aim to help developers avoid common pitfalls in software design, such as tightly coupled code and hard-to-maintain systems.

The acronym **SOLID** represents five key principles:

---

### 1. **S - Single Responsibility Principle (SRP)**
**Definition:** A class should have only one reason to change.  
**Explanation:**  
Each class should only be responsible for one part of the software's functionality. If a class has multiple responsibilities, changes in one responsibility might affect others, leading to issues in the system.

**Example:**
```java
// Violates SRP: Handles both report generation and saving
class Report {
    void generateReport() { /* logic to create a report */ }
    void saveToFile() { /* logic to save the report */ }
}

// SRP-compliant:
class Report {
    void generateReport() { /* logic to create a report */ }
}

class ReportSaver {
    void saveToFile(Report report) { /* logic to save the report */ }
}
```

---

### 2. **O - Open/Closed Principle (OCP)**
**Definition:** Classes should be open for extension but closed for modification.  
**Explanation:**  
You should be able to add new functionality to a class without modifying its existing code. This minimizes the risk of introducing bugs into existing functionality.

**Example:**
```java
// Violates OCP: Adding a new shape requires modifying the existing class
class ShapeCalculator {
    double calculateArea(Object shape) {
        if (shape instanceof Circle) {
            return Math.PI * ((Circle) shape).radius * ((Circle) shape).radius;
        } else if (shape instanceof Rectangle) {
            return ((Rectangle) shape).width * ((Rectangle) shape).height;
        }
        return 0;
    }
}

// OCP-compliant:
interface Shape {
    double calculateArea();
}

class Circle implements Shape {
    double radius;
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}

class Rectangle implements Shape {
    double width, height;
    public double calculateArea() {
        return width * height;
    }
}
```

---

### 3. **L - Liskov Substitution Principle (LSP)**
**Definition:** Subtypes must be substitutable for their base types.  
**Explanation:**  
You should be able to use a derived class in place of its base class without altering the correctness of the program. This ensures that the behavior of the system is predictable when using polymorphism.

**Example:**
```java
// Violates LSP: Square breaks Rectangle's behavior
class Rectangle {
    int width, height;
    void setWidth(int width) { this.width = width; }
    void setHeight(int height) { this.height = height; }
}

class Square extends Rectangle {
    void setWidth(int width) { super.setWidth(width); super.setHeight(width); }
    void setHeight(int height) { super.setWidth(height); super.setHeight(height); }
}

// LSP-compliant: Avoid inheritance when behavior doesn't align
class Shape {
    // Common shape logic
}

class Rectangle extends Shape {
    int width, height;
}

class Square extends Shape {
    int side;
}
```

---

### 4. **I - Interface Segregation Principle (ISP)**
**Definition:** A class should not be forced to implement interfaces it does not use.  
**Explanation:**  
Large interfaces should be broken down into smaller, more specific ones. This ensures that classes only implement methods they actually need.

**Example:**
```java
// Violates ISP: Unnecessary methods for Printer
interface Machine {
    void print();
    void scan();
    void fax();
}

class Printer implements Machine {
    public void print() { /* printing logic */ }
    public void scan() { throw new UnsupportedOperationException(); }
    public void fax() { throw new UnsupportedOperationException(); }
}

// ISP-compliant:
interface Printer {
    void print();
}

interface Scanner {
    void scan();
}

class SimplePrinter implements Printer {
    public void print() { /* printing logic */ }
}
```

---

### 5. **D - Dependency Inversion Principle (DIP)**
**Definition:** High-level modules should not depend on low-level modules. Both should depend on abstractions.  
**Explanation:**  
This principle encourages using interfaces or abstractions to manage dependencies, making the code more flexible and less coupled to concrete implementations.

**Example:**
```java
// Violates DIP: High-level module depends on low-level implementation
class BackendDeveloper {
    void writeJavaCode() { /* writes Java code */ }
}

class FrontendDeveloper {
    void writeJavaScriptCode() { /* writes JavaScript code */ }
}

class Project {
    BackendDeveloper backend = new BackendDeveloper();
    FrontendDeveloper frontend = new FrontendDeveloper();
}

// DIP-compliant:
interface Developer {
    void writeCode();
}

class BackendDeveloper implements Developer {
    public void writeCode() { /* writes Java code */ }
}

class FrontendDeveloper implements Developer {
    public void writeCode() { /* writes JavaScript code */ }
}

class Project {
    List<Developer> developers;
    Project(List<Developer> developers) {
        this.developers = developers;
    }
}
```

---

### Summary of SOLID Principles:
- **S**: Keep classes focused on a single responsibility.
- **O**: Allow extension without modifying existing code.
- **L**: Subtypes should behave like their base types.
- **I**: Keep interfaces small and specific to the needs of the implementing class.
- **D**: Depend on abstractions rather than concrete implementations.

By adhering to these principles, you can build software that is easier to maintain, test, and extend over time.