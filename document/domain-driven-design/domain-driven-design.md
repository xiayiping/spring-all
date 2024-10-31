## Domain Driven Design

In **Domain-Driven Design (DDD)**, the architecture is often organized into **four layers** or **tiers**. These layers structure the application from the core business logic to the outermost infrastructure concerns. The most common terminology for this layered architecture is often referred to as **the Onion Architecture** or **Hexagonal Architecture**.

Here’s the list of the four layers, moving from the **inside (core)** to the **outside (infrastructure)**:

### 1. **Domain Layer (Core/Innermost)**
- **Purpose**: This is the heart of the application. It contains the core business logic and rules that are central to the problem domain.
- **Components**:
    - **Entities**: Business objects that have a unique identity and lifecycle.
    - **Value Objects**: Objects that represent descriptive aspects of the domain with no identity.
    - **Aggregates**: A cluster of related entities and value objects that are treated as a unit.
    - **Domain Services**: Business logic that doesn’t naturally fit within a single entity or value object.
    - **Domain Events**: Events that are triggered by domain actions and represent something significant happening within the domain.
    - **Factories**: Responsible for complex object creation.
    - **Repositories (Interfaces)**: Abstractions for accessing and persisting aggregates.

- **Characteristics**:
    - **Purely focused on business logic**.
    - Should have no dependencies on frameworks, databases, or external systems.
    - Changes in the domain should reflect changes in business rules, not in technology.

### 2. **Application Layer**
- **Purpose**: This layer orchestrates the core domain logic by coordinating tasks and managing the flow of data in and out of the domain. It does not contain business rules itself but implements application-specific use cases.
- **Components**:
    - **Application Services**: Use cases that orchestrate domain logic and workflows. They call domain services, repositories, and other components as needed.
    - **Commands and Queries**: If using a Command Query Responsibility Segregation (CQRS) pattern, commands are used to perform actions, and queries are used to retrieve data.
    - **DTOs (Data Transfer Objects)**: Simple data structures for transferring data between layers.

- **Characteristics**:
    - **Thin layer** that mainly acts as a coordinator for domain operations.
    - Defines the application's use cases and how different domain objects interact.
    - No business logic should live here; that belongs in the domain layer.

### 3. **Infrastructure Layer**
- **Purpose**: Provides technical capabilities and acts as an adapter between the domain/application layers and external systems like databases, messaging systems, APIs, and third-party services.
- **Components**:
    - **Repositories (Implementations)**: The actual implementations of repository interfaces defined in the domain layer, usually involving database access via ORM tools (e.g., using JPA, Hibernate, etc.).
    - **External Services Adapters**: Adapters that integrate with external systems like REST APIs, message queues, etc.
    - **Persistence/ORM**: Database mappings and transaction management.
    - **Email, Logging, Caching, etc.**: Any other cross-cutting concerns that interact with external systems.

- **Characteristics**:
    - Should be kept **as decoupled from the domain as possible**.
    - Implements interfaces from the domain layer, allowing the domain to remain isolated from infrastructure concerns.
    - Handles persistence, messaging, and other I/O tasks.

### 4. **User Interface (UI) Layer / Presentation Layer (Outermost)**
- **Purpose**: Handles interaction with the user or external systems. This can be a web-based interface, desktop application, mobile interface, or an API.
- **Components**:
    - **Controllers**: Handle incoming requests (typically HTTP in a web application) and route them to the appropriate application services.
    - **Views**: Templates or components responsible for rendering the user interface.
    - **View Models**: Data structures that are specifically tailored for the UI, often derived from DTOs or domain objects.
    - **API Gateways**: If the UI is an API, this layer will provide endpoints for external systems to interact with.

- **Characteristics**:
    - **Interacts with the user** or external systems.
    - **Delegates most of the work** to the application layer.
    - Converts user input into commands or queries that the application layer processes.

---

### Visualizing the Layers

```
+--------------------------+
|     User Interface (UI)   |  <-- Outermost: Controllers, Views, APIs
+--------------------------+
|   Application Layer       |  <-- Use Cases, DTOs, Orchestration
+--------------------------+
|   Domain Layer (Core)     |  <-- Entities, Aggregates, Domain Services
+--------------------------+
| Infrastructure Layer      |  <-- Persistence, Repositories, External Adapters
+--------------------------+
```

### Summary of Responsibilities

- **Domain Layer**: Core business logic, rules, and domain objects (Entities, Value Objects, Aggregates, etc.).
- **Application Layer**: Coordinates domain operations and implements use cases.
- **Infrastructure Layer**: Handles technical concerns like database access, messaging, APIs, etc.
- **User Interface Layer**: Responsible for the interaction between the user (or external systems) and the application.

Each layer depends on the one below it, but **the domain layer should not depend on the infrastructure or UI layers**—this ensures that the core business logic remains isolated from technical concerns, making the application more adaptable and maintainable.


## Difference between Domain Service and App Service 

In practice, it can sometimes be difficult to draw a clear boundary between a **domain service** (business logic) and an **application service** (non-business, orchestration logic). However, there are guiding principles and criteria you can use to help make this distinction.

### Key Principles to Distinguish Domain and Application Services:

#### 1. **Focus on Business Logic vs. Orchestration**
- **Domain Services** should encapsulate **core business logic** that is central to the problem domain.
    - If the service is answering **"what does the business do?"** or **"what rules govern this process?"**, it likely belongs in the **domain layer**.
    - Domain Services should have a direct impact on the business rules or entities that represent the core of the business.
- **Application Services**, on the other hand, are responsible for **orchestrating workflows** and **coordinating tasks**.
    - If the service is responsible for **"how things happen"** (e.g., coordinating multiple domain services, managing transactions, or handling external interactions), it likely belongs in the **application layer**.
    - Application Services typically do not contain core business logic but instead manage how different pieces of the system (such as domain objects, repositories, external services, etc.) interact.

#### 2. **Interaction with Domain Models**
- **Domain Services** operate **within the domain model**. They often manipulate or interact closely with **domain entities**, **value objects**, and **aggregates**.
    - Domain Services are invoked when a business rule or process needs to be enforced (e.g., calculating discounts, validating business rules, handling domain events).
- **Application Services** often act as **mediators** between the outside world (e.g., UI, external systems) and the domain logic. They may call **domain services** or **repositories** but are not concerned with the internal rules of the domain. Instead, they coordinate actions across multiple services or layers.

#### 3. **Reusability Across Use Cases**
- **Domain Services** are typically **reusable across multiple application use cases** because they represent core business logic. They should be agnostic of specific workflows or use cases.
    - For example, a service that calculates the total price of an order, including discounts, taxes, and shipping costs, is a **domain service** because it represents a core business rule.
- **Application Services** are often **specific to a particular use case** or workflow. They may invoke domain services, handle external communication, or manage transaction boundaries, but they are tied to a specific application operation.
    - For example, a service that handles the **checkout process** for a user would be an **application service** because it orchestrates multiple steps (e.g., calculating the order total, checking inventory, creating an order, processing payment) but does not represent a single business concept.

#### 4. **Dependencies**
- **Domain Services** should depend **only on the domain model** or other domain services. They should not have dependencies on infrastructure concerns (e.g., databases, external APIs, message queues).
    - If a service depends on infrastructure, external systems, or other non-business components, it likely belongs in the **application layer**.
- **Application Services** can depend on **external systems**, **repositories**, or **infrastructure services**. They can also handle things like **transactions**, **messaging**, or **error handling** that are not part of the core business logic.

#### 5. **Responsibility**
- **Domain Services** are responsible for **business decisions**. They enforce business rules, perform calculations, or manage workflows that are central to the operation of the business.
- **Application Services** are responsible for **coordination**. They handle the execution of use cases, manage the flow of data between layers, and ensure that the right domain services are invoked at the right time.

---

### Practical Criteria for Identifying Domain Services:

Ask the following questions to determine if a service belongs in the **domain layer**:

- **Does the service encapsulate business logic?**
    - If the service is implementing a business rule (e.g., "calculate discount," "validate an order," "approve a loan"), it belongs in the **domain layer**.
- **Is the service central to the domain model?**
    - If the service manipulates or enforces rules on **domain entities**, **aggregates**, or **value objects**, it is a domain service.
- **Can this service be used across different application workflows?**
    - If the service is reusable across multiple use cases or scenarios (e.g., "calculate tax" or "apply a loyalty discount"), it is likely a domain service because it represents core business behavior.

Example of a **Domain Service**:

```python
# Domain Service (Business logic)
class DiscountService:
    def calculate_discount(self, customer, order):
        if customer.is_loyal_customer():
            return order.total_price * 0.10  # 10% discount
        return 0
```

This service represents a **business rule** about how discounts are calculated. It belongs in the domain layer because it encapsulates core business logic.

---

### Practical Criteria for Identifying Application Services:

Ask the following questions to determine if a service belongs in the **application layer**:

- **Is the service responsible for orchestrating or coordinating tasks across multiple layers or services?**
    - If the service manages the interaction between different services (e.g., fetching data from one service, processing it, and sending it to another), it likely belongs in the **application layer**.
- **Does the service handle external dependencies or infrastructure concerns?**
    - If the service interacts with external systems (e.g., databases, message queues, external APIs) or manages transactions, it's an application service.
- **Is the service tied to a specific use case or workflow?**
    - If the service is narrowly focused on implementing a use case (e.g., "checkout process," "customer registration"), it likely belongs in the application layer.

Example of an **Application Service**:

```python
# Application Service (Orchestration)
class OrderService:
    def __init__(self, discount_service, inventory_service, payment_service):
        self.discount_service = discount_service
        self.inventory_service = inventory_service
        self.payment_service = payment_service

    def place_order(self, customer, order):
        # Check inventory
        if not self.inventory_service.is_in_stock(order):
            raise OutOfStockException()

        # Apply discount
        discount = self.discount_service.calculate_discount(customer, order)
        order.apply_discount(discount)

        # Process payment
        self.payment_service.process_payment(customer, order.total_price)

        # Save order to the database
        order_repository.save(order)
```

This service orchestrates the workflow of placing an order: checking inventory, applying discounts, processing payment, and saving the order. It coordinates multiple services, but **doesn't implement business rules itself**—those are delegated to the domain services (like `DiscountService`). Therefore, this service belongs in the **application layer**.

---

### Summary of Practical Differences:

| **Aspect**                      | **Domain Service (Business Logic)**                         | **Application Service (Non-Business Logic)**              |
|----------------------------------|-------------------------------------------------------------|-----------------------------------------------------------|
| **Focus**                        | Implements core business rules                              | Orchestrates workflows and coordinates tasks               |
| **Reusability**                  | Reusable across multiple use cases                          | Often specific to a particular use case                    |
| **Dependencies**                 | No dependencies on external systems or infrastructure       | May depend on external systems, repositories, or infrastructure |
| **Interaction with Domain Models** | Directly manipulates domain entities, aggregates, or value objects | Calls domain services and repositories to complete use cases |
| **Example**                      | `DiscountService`, `LoanApprovalService`                    | `OrderService`, `UserRegistrationService`                  |

By applying these criteria, you can more easily determine whether a service should be categorized as a **domain service** or an **application service**.


## Security Related

In Domain-Driven Design (DDD), there are different layers that organize the application's structure based on responsibility:

1. **Domain Layer**: Contains the business logic and core entities.
2. **Application Layer**: Coordinates the domain objects to execute business processes.
3. **Infrastructure Layer**: Deals with technical concerns such as database access, messaging, and security.
4. **Presentation Layer**: Manages the user interface and handles incoming requests.

### Where to Place Security Configuration in DDD?

The **Infrastructure Layer** is the most appropriate place for security configuration such as Spring Security's filter chain configuration. This is because security is a cross-cutting concern that doesn't directly impact the domain logic, but rather supports the application's technical needs, such as authentication, authorization, and protection of resources.

#### Reasons to Place Security in the Infrastructure Layer:
- **Separation of Concerns**: The Infrastructure Layer deals with external systems and technical details. Security configuration, including setting up filters, user authentication, and authorization, is part of the infrastructure that enables the application to function securely.
- **Cross-Cutting Concern**: Security is not tied to any specific domain logic, but instead affects the entire application. It cuts across multiple layers, and placing it in the Infrastructure Layer ensures it does not mix with core business logic.
- **Modularity**: By placing security configuration in the Infrastructure Layer, you can easily change or update the security mechanisms without affecting the domain or application logic.

### Example of Security Configuration in a DDD Setup

Here’s how you might organize your application structure in a DDD context:

```
/src
  /main
    /java
      /com
        /example
          /domain          # Domain Layer (Entities, Value Objects, Aggregates, Repositories)
          /application      # Application Layer (Service Layer, Use Cases)
          /infrastructure   # Infrastructure Layer (Repositories, Security, Messaging, etc.)
            /security       # Security configuration lives here
              SecurityConfig.java
          /presentation     # Presentation Layer (Controllers, API endpoints)
```

### Example of Spring Security Configuration in the Infrastructure Layer

```java
package com.example.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(withDefaults())
            .httpBasic();
        return http.build();
    }
}
```

This configuration is placed in the `infrastructure.security` package within the **Infrastructure Layer**.

### Conclusion

In DDD, security configurations like Spring Security's filter chain should be placed in the **Infrastructure Layer**. This keeps your business logic clean from technical concerns and ensures good separation of responsibilities.