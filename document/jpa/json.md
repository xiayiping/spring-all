# Difference Between `@JsonFormat` and `@JsonSerialize`

## Quick Comparison

| Aspect | `@JsonFormat` | `@JsonSerialize` |
|--------|---------------|------------------|
| **Package** | `com.fasterxml.jackson.annotation` | `com.fasterxml.jackson.databind.annotation` |
| **Purpose** | Configure formatting options | Specify custom serializer class |
| **Flexibility** | Limited to predefined options | Fully customizable |
| **Complexity** | Simple, declarative | More powerful, requires serializer class |
| **Use Case** | Simple format changes | Complex custom logic |

---

## `@JsonFormat`

### Characteristics
- **High-level annotation** for simple formatting
- Uses predefined shapes and patterns
- No custom code required
- Part of core annotations package

### Common Use Cases

```java
import com.fasterxml.jackson.annotation.JsonFormat;

public class Example {
    
    // Long to String
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    
    // Date formatting
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date createdAt;
    
    // Number formatting
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "#,###.00")
    private Double price;
    
    // Boolean as number
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Boolean active;
    
    // Enum as object
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private Status status;
}
```

### Available Shapes

```java
public enum Shape {
    ANY,           // Default
    NATURAL,       // Natural representation
    SCALAR,        // Scalar value
    ARRAY,         // JSON array
    OBJECT,        // JSON object
    NUMBER,        // JSON number
    NUMBER_FLOAT,  // Floating point
    NUMBER_INT,    // Integer
    STRING,        // JSON string
    BOOLEAN        // JSON boolean
}
```

---

## `@JsonSerialize`

### Characteristics
- **Low-level annotation** for custom serialization
- Requires a serializer class
- Full control over output
- Part of databind package

### Common Use Cases

```java
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class Example {
    
    // Using built-in serializer
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    // Using custom serializer
    @JsonSerialize(using = CustomMoneySerializer.class)
    private BigDecimal amount;
    
    // Serialize collection content
    @JsonSerialize(contentUsing = CustomItemSerializer.class)
    private List<Item> items;
    
    // Serialize map keys
    @JsonSerialize(keyUsing = CustomKeySerializer.class)
    private Map<CustomKey, String> data;
}
```

### Custom Serializer Example

```java
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CustomMoneySerializer extends JsonSerializer<BigDecimal> {
    
    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, 
                         SerializerProvider serializers) throws IOException {
        if (value != null) {
            // Custom formatting logic
            String formatted = "$" + value.setScale(2, RoundingMode.HALF_UP);
            gen.writeString(formatted);
        } else {
            gen.writeNull();
        }
    }
}
```

---

## Side-by-Side Comparison

### Same Result, Different Approach

```java
public class User {
    
    // Using @JsonFormat - Simple, declarative
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id1;
    
    // Using @JsonSerialize - More explicit
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id2;
}

// Both produce: "id1": "123", "id2": "123"
```

### Date Formatting

```java
public class Event {
    
    // @JsonFormat - Simple pattern
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date1;
    
    // @JsonSerialize - Custom logic possible
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDate date2;
}
```

---

## When to Use Which?

### Use `@JsonFormat` When:

```java
// ✅ Simple type conversion
@JsonFormat(shape = JsonFormat.Shape.STRING)
private Long id;

// ✅ Standard date patterns
@JsonFormat(pattern = "yyyy-MM-dd")
private LocalDate date;

// ✅ Timezone handling
@JsonFormat(timezone = "UTC")
private Date timestamp;

// ✅ Locale-specific formatting
@JsonFormat(locale = "en_US")
private Double number;
```

### Use `@JsonSerialize` When:

```java
// ✅ Complex custom logic
@JsonSerialize(using = MaskingSerializer.class)  // Hide sensitive data
private String creditCard;

// ✅ Conditional formatting
@JsonSerialize(using = ConditionalSerializer.class)
private Object dynamicField;

// ✅ Collection item serialization
@JsonSerialize(contentUsing = ItemSerializer.class)
private List<Item> items;

// ✅ Null handling with custom logic
@JsonSerialize(nullsUsing = CustomNullSerializer.class)
private String nullableField;
```

---

## Combined Usage

You can use both annotations together:

```java
public class Product {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = ToStringSerializer.class)  // Redundant but valid
    private Long id;
    
    // @JsonSerialize takes precedence if both define serialization
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonSerialize(using = CustomDateSerializer.class)  // This wins
    private LocalDate date;
}
```

---

## Summary

| Choose | When |
|--------|------|
| **`@JsonFormat`** | Simple formatting, dates, basic type shapes |
| **`@JsonSerialize`** | Custom logic, complex transformations, full control |
| **Both** | `@JsonSerialize` takes precedence for serialization |

> **Recommendation:** Start with `@JsonFormat` for simplicity. Use `@JsonSerialize` only when you need custom logic that `@JsonFormat` cannot handle.
