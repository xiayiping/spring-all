# Null and NotProvided

# Distinguishing Between Null (Omitted) vs Explicitly Set Null in Spring Boot

This is a common problem! When JSON is deserialized, both omitted fields and explicitly `null` fields result in `null` in Java. Here are several solutions:

---

## Solution 1: Use `Optional<T>` (Recommended)

```java
public class Cmd {
    private String name;
    private Optional<Integer> age = null; // null = not provided, Optional.empty() = explicitly null
    
    // Getters and setters
    public Optional<Integer> getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = Optional.ofNullable(age);
    }
}
```

**Usage:**
```java
@PostMapping("/user")
public void createUser(@RequestBody Cmd cmd) {
    if (cmd.getAge() == null) {
        // Field was NOT provided (omitted)
    } else if (cmd.getAge().isEmpty()) {
        // Field was explicitly set to null
    } else {
        // Field has a value
        Integer age = cmd.getAge().get();
    }
}
```

---

## Solution 2: Use a Wrapper Class with "Set" Flag

```java
public class Cmd {
    private String name;
    private Integer age;
    private boolean ageSet = false;  // Track if setter was called
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
        this.ageSet = true;  // Jackson calls setter even for null
    }
    
    public boolean isAgeProvided() {
        return ageSet;
    }
}
```

---

## Solution 3: Use JsonNode for Dynamic Checking

```java
@PostMapping("/user")
public void createUser(@RequestBody JsonNode json) {
    if (!json.has("age")) {
        // Field was NOT provided
    } else if (json.get("age").isNull()) {
        // Field was explicitly set to null
    } else {
        // Field has a value
        Integer age = json.get("age").asInt();
    }
}
```

---

## Solution 4: Custom Wrapper Type (Most Elegant)

```java
// Generic wrapper
public class OptionalField<T> {
    private T value;
    private boolean present = false;
    
    public static <T> OptionalField<T> of(T value) {
        OptionalField<T> field = new OptionalField<>();
        field.value = value;
        field.present = true;
        return field;
    }
    
    public boolean isPresent() { return present; }
    public boolean isAbsent() { return !present; }
    public T getValue() { return value; }
    public boolean isExplicitlyNull() { return present && value == null; }
}
```

```java
// Custom deserializer
public class OptionalFieldDeserializer extends JsonDeserializer<OptionalField<?>> {
    @Override
    public OptionalField<?> deserialize(JsonParser p, DeserializationContext ctxt) {
        // Value is present (even if null)
        return OptionalField.of(p.readValueAs(Object.class));
    }
    
    @Override
    public OptionalField<?> getNullValue(DeserializationContext ctxt) {
        return OptionalField.of(null);  // Explicitly null
    }
}
```

---

## Quick Comparison

| JSON Input | Solution 1 (Optional) | Solution 2 (Flag) |
|------------|----------------------|-------------------|
| `{}` (omitted) | `age = null` | `ageSet = false` |
| `{"age": null}` | `age = Optional.empty()` | `ageSet = true, age = null` |
| `{"age": 25}` | `age = Optional.of(25)` | `ageSet = true, age = 25` |

---

## My Recommendation

**For simple cases:** Use **Solution 2** (boolean flag) - straightforward and no extra dependencies.

**For many fields:** Use **Solution 4** (custom wrapper) - cleaner and reusable.

# Sample

# Solution 4 Adapted to Your Sample

Here's a complete implementation for your `Cmd` class:

---

## Step 1: Create the `OptionalField<T>` Wrapper

```java
package com.example.common;

public class OptionalField<T> {
    private T value;
    private boolean present = false;
    
    // Private constructor
    private OptionalField() {}
    
    // Factory method for present value (including null)
    public static <T> OptionalField<T> of(T value) {
        OptionalField<T> field = new OptionalField<>();
        field.value = value;
        field.present = true;
        return field;
    }
    
    // Factory method for absent field
    public static <T> OptionalField<T> absent() {
        return new OptionalField<>();
    }
    
    // Check methods
    public boolean isPresent() {
        return present;
    }
    
    public boolean isAbsent() {
        return !present;
    }
    
    public boolean isExplicitlyNull() {
        return present && value == null;
    }
    
    public boolean hasValue() {
        return present && value != null;
    }
    
    // Get value
    public T getValue() {
        return value;
    }
    
    public T orElse(T defaultValue) {
        return hasValue() ? value : defaultValue;
    }
    
    @Override
    public String toString() {
        if (!present) {
            return "OptionalField.absent";
        }
        return "OptionalField[" + value + "]";
    }
}
```

---

## Step 2: Create Custom Jackson Deserializer

```java
package com.example.config;

import com.example.common.OptionalField;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;

public class OptionalFieldDeserializer extends JsonDeserializer<OptionalField<?>> 
        implements ContextualDeserializer {
    
    private JavaType valueType;
    
    public OptionalFieldDeserializer() {}
    
    public OptionalFieldDeserializer(JavaType valueType) {
        this.valueType = valueType;
    }
    
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        // Get the generic type parameter (e.g., Integer from OptionalField<Integer>)
        JavaType wrapperType = property.getType();
        JavaType valueType = wrapperType.containedType(0);
        return new OptionalFieldDeserializer(valueType);
    }
    
    @Override
    public OptionalField<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // Field is present with a non-null value
        Object value = ctxt.readValue(p, valueType);
        return OptionalField.of(value);
    }
    
    @Override
    public OptionalField<?> getNullValue(DeserializationContext ctxt) {
        // Field is present but explicitly set to null
        return OptionalField.of(null);
    }
    
    @Override
    public OptionalField<?> getAbsentValue(DeserializationContext ctxt) {
        // Field is not present in JSON
        return OptionalField.absent();
    }
}
```

---

## Step 3: Register the Deserializer

```java
package com.example.config;

import com.example.common.OptionalField;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OptionalField.class, new OptionalFieldDeserializer());
        mapper.registerModule(module);
        
        return mapper;
    }
}
```

---

## Step 4: Your `Cmd` Class

```java
package com.example.dto;

import com.example.common.OptionalField;

public class Cmd {
    
    private OptionalField<String> name = OptionalField.absent();
    private OptionalField<Integer> age = OptionalField.absent();
    
    // Getters and Setters
    public OptionalField<String> getName() {
        return name;
    }
    
    public void setName(OptionalField<String> name) {
        this.name = name;
    }
    
    public OptionalField<Integer> getAge() {
        return age;
    }
    
    public void setAge(OptionalField<Integer> age) {
        this.age = age;
    }
}
```

---

## Step 5: Controller Usage

```java
package com.example.controller;

import com.example.dto.Cmd;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {
    
    @PostMapping("/user")
    public String createUser(@RequestBody Cmd cmd) {
        StringBuilder result = new StringBuilder();
        
        // Check name field
        if (cmd.getName().isAbsent()) {
            result.append("name: not provided (omitted)\n");
        } else if (cmd.getName().isExplicitlyNull()) {
            result.append("name: explicitly set to null\n");
        } else {
            result.append("name: ").append(cmd.getName().getValue()).append("\n");
        }
        
        // Check age field
        if (cmd.getAge().isAbsent()) {
            result.append("age: not provided (omitted)\n");
        } else if (cmd.getAge().isExplicitlyNull()) {
            result.append("age: explicitly set to null\n");
        } else {
            result.append("age: ").append(cmd.getAge().getValue()).append("\n");
        }
        
        return result.toString();
    }
    
    @PatchMapping("/user/{id}")
    public String updateUser(@PathVariable Long id, @RequestBody Cmd cmd) {
        // Perfect for PATCH operations - only update fields that were provided
        
        if (cmd.getName().isPresent()) {
            // Update name (even if null - means clear the name)
            System.out.println("Updating name to: " + cmd.getName().getValue());
        }
        
        if (cmd.getAge().isPresent()) {
            // Update age (even if null - means clear the age)
            System.out.println("Updating age to: " + cmd.getAge().getValue());
        }
        
        return "Updated user " + id;
    }
}
```

---

## Testing Results

| JSON Request | `name` Result | `age` Result |
|--------------|---------------|--------------|
| `{}` | `isAbsent() = true` | `isAbsent() = true` |
| `{"name": null}` | `isExplicitlyNull() = true` | `isAbsent() = true` |
| `{"name": "John", "age": null}` | `hasValue() = true, getValue() = "John"` | `isExplicitlyNull() = true` |
| `{"name": "John", "age": 25}` | `hasValue() = true, getValue() = "John"` | `hasValue() = true, getValue() = 25` |

---

## Test with cURL

```bash
# All fields omitted
curl -X POST http://localhost:8080/api/user \
  -H "Content-Type: application/json" \
  -d '{}'

# Explicitly null
curl -X POST http://localhost:8080/api/user \
  -H "Content-Type: application/json" \
  -d '{"name": null, "age": null}'

# With values
curl -X POST http://localhost:8080/api/user \
  -H "Content-Type: application/json" \
  -d '{"name": "John", "age": 25}'

# Mixed
curl -X POST http://localhost:8080/api/user \
  -H "Content-Type: application/json" \
  -d '{"name": "John"}'
```

This solution gives you complete control to distinguish between omitted fields and explicitly null fields! 🎯

# Serializer

# Adding Serializer for OptionalField

Great catch! We need a custom serializer to properly convert `OptionalField<T>` back to JSON. Here's the complete solution:

---

## Step 1: Create Custom Serializer

```java
package com.example.config;

import com.example.common.OptionalField;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class OptionalFieldSerializer extends JsonSerializer<OptionalField<?>> {
    
    @Override
    public void serialize(OptionalField<?> value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        
        if (value == null || value.isAbsent()) {
            // Option 1: Don't write anything (field will be omitted)
            // This requires special handling - see isEmpty() below
            gen.writeNull();
        } else if (value.getValue() == null) {
            // Explicitly null
            gen.writeNull();
        } else {
            // Has actual value - write it directly
            gen.writeObject(value.getValue());
        }
    }
    
    @Override
    public boolean isEmpty(SerializerProvider provider, OptionalField<?> value) {
        // Return true to omit this field from JSON output when absent
        return value == null || value.isAbsent();
    }
}
```

---

## Step 2: Updated Jackson Configuration

```java
package com.example.config;

import com.example.common.OptionalField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Important: This makes isEmpty() work - omits fields where isEmpty() returns true
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OptionalField.class, new OptionalFieldDeserializer());
        module.addSerializer(OptionalField.class, new OptionalFieldSerializer());
        mapper.registerModule(module);
        
        return mapper;
    }
}
```

---

## Step 3: Updated OptionalField Class (with Builder Pattern)

```java
package com.example.common;

public class OptionalField<T> {
    private T value;
    private boolean present = false;
    
    private OptionalField() {}
    
    public static <T> OptionalField<T> of(T value) {
        OptionalField<T> field = new OptionalField<>();
        field.value = value;
        field.present = true;
        return field;
    }
    
    public static <T> OptionalField<T> absent() {
        return new OptionalField<>();
    }
    
    // Check methods
    public boolean isPresent() {
        return present;
    }
    
    public boolean isAbsent() {
        return !present;
    }
    
    public boolean isExplicitlyNull() {
        return present && value == null;
    }
    
    public boolean hasValue() {
        return present && value != null;
    }
    
    // Get value
    public T getValue() {
        return value;
    }
    
    public T orElse(T defaultValue) {
        return hasValue() ? value : defaultValue;
    }
    
    @Override
    public String toString() {
        if (!present) {
            return "OptionalField.absent";
        }
        return "OptionalField[" + value + "]";
    }
}
```

---

## Serialization Results

| OptionalField State | JSON Output (with NON_EMPTY) | JSON Output (without NON_EMPTY) |
|---------------------|------------------------------|--------------------------------|
| `OptionalField.absent()` | Field omitted | `"field": null` |
| `OptionalField.of(null)` | `"field": null` | `"field": null` |
| `OptionalField.of("John")` | `"field": "John"` | `"field": "John"` |
| `OptionalField.of(25)` | `"field": 25` | `"field": 25` |

---

## Complete Test Example

```java
package com.example.controller;

import com.example.common.OptionalField;
import com.example.dto.Cmd;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {
    
    @PostMapping("/echo")
    public Cmd echo(@RequestBody Cmd cmd) {
        // Simply return what was received - demonstrates serialization
        return cmd;
    }
    
    @GetMapping("/test")
    public Cmd test() {
        // Manually create response to show serialization
        Cmd cmd = new Cmd();
        cmd.setName(OptionalField.of("John"));
        cmd.setAge(OptionalField.of(25));
        return cmd;
    }
    
    @GetMapping("/test-null")
    public Cmd testNull() {
        Cmd cmd = new Cmd();
        cmd.setName(OptionalField.of("John"));
        cmd.setAge(OptionalField.of(null));  // Explicitly null
        return cmd;
    }
    
    @GetMapping("/test-absent")
    public Cmd testAbsent() {
        Cmd cmd = new Cmd();
        cmd.setName(OptionalField.of("John"));
        cmd.setAge(OptionalField.absent());  // Will be omitted
        return cmd;
    }
}
```

---

## Test Results

```bash
# Test with values
curl http://localhost:8080/api/test
# Output: {"name":"John","age":25}

# Test with explicit null
curl http://localhost:8080/api/test-null
# Output: {"name":"John","age":null}

# Test with absent field (omitted)
curl http://localhost:8080/api/test-absent
# Output: {"name":"John"}

# Echo test - send and receive
curl -X POST http://localhost:8080/api/echo \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice", "age": 30}'
# Output: {"name":"Alice","age":30}

curl -X POST http://localhost:8080/api/echo \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice"}'
# Output: {"name":"Alice"}
```

---

## Alternative: Control Serialization Per-Field

If you want different behavior per field, use annotations:

```java
public class Cmd {
    
    // Always include, even if absent
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private OptionalField<String> name = OptionalField.absent();
    
    // Only include if not empty (default with our config)
    private OptionalField<Integer> age = OptionalField.absent();
    
    // Getters and setters...
}
```

---

## Summary

| Component | Purpose |
|-----------|---------|
| `OptionalFieldSerializer` | Converts `OptionalField<T>` → JSON value |
| `OptionalFieldDeserializer` | Converts JSON value → `OptionalField<T>` |
| `JsonInclude.Include.NON_EMPTY` | Enables omitting absent fields |
| `isEmpty()` method | Tells Jackson when to omit the field |

Now your `OptionalField` works both ways - deserializing from JSON and serializing back to JSON! 🎯
