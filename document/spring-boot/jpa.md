
# JPA

## PrePersist action and domain events

```java
import org.springframework.data.domain.DomainEvents;

@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private LocalDateTime creationDate;

    @Transient
    private List<Object> domainEvents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
        // Add the domain event to the list
        domainEvents.add(new OrderCreatedEvent(this));
    }

    @DomainEvents
    protected Collection<Object> domainEvents() {
        return domainEvents;
    }

    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        domainEvents.clear();
    }
}
```

```java
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        Order order = event.getOrder();
        // Handle the event (e.g., send a notification, update a status, etc.)
        System.out.println("Order created: " + order.getId() + " for customer " + order.getCustomerName());
    }
}
```


# Spring Boot

## Data

- for data jdbc, for refer to other entity , using AggregateReference<ServletSecurityConfig,Long> ss;

- for jpa, if you need publish event on persist, extend AbstractAggregateRoot rr;
