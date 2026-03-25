# Multi Modules

I'll help you migrate from Maven to Gradle (Kotlin DSL) for your multi-module Spring Boot project. Here's how to achieve all your requirements:

## Project Structure
```
root-project/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── module-a/
│   └── build.gradle.kts
├── module-b/
│   └── build.gradle.kts
└── module-c/
    └── build.gradle.kts
```

## 1. Root `settings.gradle.kts`
```kotlin
rootProject.name = "my-project"

include("module-a")
include("module-b")
include("module-c")
```

## 2. Root `build.gradle.kts`
```kotlin
plugins {
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.spring") version "1.9.22" apply false
    id("org.springframework.boot") version "3.2.1" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

// Define versions in one place
extra["springCloudVersion"] = "2023.0.0"
extra["kotlinLoggingVersion"] = "3.0.5"

allprojects {
    group = "com.example"
    version = "1.0.0-SNAPSHOT"
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")
    
    dependencies {
        // Common dependencies for all modules
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
    
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }
    
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
```

## 3. Sub-module `build.gradle.kts` examples

### module-a/build.gradle.kts (Library module - no Spring Boot plugin)
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.github.microutils:kotlin-logging-jvm:${rootProject.extra["kotlinLoggingVersion"]}")
}
```

### module-b/build.gradle.kts (Depends on module-a)
```kotlin
dependencies {
    // Depend on other sub-module
    implementation(project(":module-a"))
    
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
}
```

### module-c/build.gradle.kts (Main application module)
```kotlin
plugins {
    id("org.springframework.boot")
}

dependencies {
    // Depend on other sub-modules
    implementation(project(":module-a"))
    implementation(project(":module-b"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

// Only the main application needs this
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}
```

## 4. Optional: `gradle.properties`
```properties
kotlin.code.style=official
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
```

## Alternative: Using Version Catalog (Modern Approach)

Create `gradle/libs.versions.toml`:
```toml
[versions]
kotlin = "1.9.22"
springBoot = "3.2.1"
springDependencyManagement = "1.1.4"
kotlinLogging = "3.0.5"

[libraries]
kotlin-logging = { module = "io.github.microutils:kotlin-logging-jvm", version.ref = "kotlinLogging" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-spring = { id = "org.jetbrains.kotlin.plugin.spring", version.ref = "kotlin" }
spring-boot = { id = "org.springframework.boot", version.ref = "springBoot" }
spring-dependency-management = { id = "io.spring.dependency-management", version.ref = "springDependencyManagement" }
```

Then in root `build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}
```

And in sub-modules:
```kotlin
dependencies {
    implementation(libs.kotlin.logging)
}
```

This setup gives you:
1. ✅ Centralized dependency management (versions in root or version catalog)
2. ✅ Shared version across all modules (via `allprojects` block)
3. ✅ Inter-module dependencies (via `project(":module-name")`)

# questions:

Great questions! Let me explain both:

## 1. `apply false` meaning

```kotlin
plugins {
    kotlin("jvm") version "1.9.22" apply false
}
```

- **`apply false`** means: "Make this plugin available to subprojects, but DON'T apply it to the root project itself"
- The root project just **declares** the plugin and its version
- Subprojects can then apply it without specifying the version again

**Without `apply false`:**
```kotlin
// Root build.gradle.kts
plugins {
    kotlin("jvm") version "1.9.22"  // Applied to root project (usually unnecessary)
}

// Sub-module build.gradle.kts
plugins {
    kotlin("jvm") version "1.9.22"  // Must repeat the version ❌
}
```

**With `apply false`:**
```kotlin
// Root build.gradle.kts
plugins {
    kotlin("jvm") version "1.9.22" apply false  // Just declare, don't apply to root
}

// Sub-module build.gradle.kts
plugins {
    kotlin("jvm")  // Version inherited from root ✅
}
```

Or in the `subprojects` block:
```kotlin
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")  // Applied to all subprojects
}
```

## 2. Where are dependency versions?

When you use **Spring Boot's dependency management plugin**, you don't need to specify versions for Spring-managed dependencies:

```kotlin
subprojects {
    apply(plugin = "io.spring.dependency-management")  // This is the key!
    
    dependencies {
        // NO version needed - managed by Spring Boot BOM
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
}
```

The `io.spring.dependency-management` plugin imports the **Spring Boot BOM (Bill of Materials)**, which defines versions for:
- All Spring Boot starters
- Jackson libraries
- Kotlin libraries
- Many other common dependencies

**If you need to override or add versions:**

```kotlin
subprojects {
    apply(plugin = "io.spring.dependency-management")
    
    dependencyManagement {
        imports {
            // Already imported by the plugin, but you can be explicit:
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.1")
        }
        
        dependencies {
            // Override a managed version
            dependency("com.h2database:h2:2.2.224")
            
            // Add a new managed dependency
            dependency("io.github.microutils:kotlin-logging-jvm:${rootProject.extra["kotlinLoggingVersion"]}")
        }
    }
    
    dependencies {
        // Now these use the versions from dependencyManagement
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("com.h2database:h2")  // Uses overridden version
        implementation("io.github.microutils:kotlin-logging-jvm")  // Uses managed version
    }
}
```

**Or specify versions explicitly when needed:**

```kotlin
dependencies {
    // Managed by Spring Boot BOM - no version needed
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // Not managed by Spring Boot - specify version
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    
    // Or use rootProject.extra
    implementation("io.github.microutils:kotlin-logging-jvm:${rootProject.extra["kotlinLoggingVersion"]}")
}
```

**Summary:**
- Spring Boot's dependency management plugin manages versions for most common libraries
- You only need to specify versions for libraries NOT in the Spring Boot BOM
- You can override managed versions in the `dependencyManagement` block if needed

# Version Ref

Good question! The answer is **no** - it's not that flexible.

## How `version.ref` works

The `.ref` suffix is a **specific API** provided by Gradle's version catalog feature. It's hardcoded to look in the `[versions]` section only.

```toml
[versions]
kotlin = "1.9.22"
springBoot = "3.2.1"

[libraries]
# version.ref ALWAYS looks in [versions] section
kotlin-logging = { module = "io.github.microutils:kotlin-logging-jvm", version.ref = "kotlin" }
#                                                                      ^^^^^^^^^^^
#                                                                      This looks for "kotlin" in [versions]
```

## What happens with `abc.ref`?

```toml
[libraries]
my-lib = { module = "com.example:my-lib", abc.ref = "someVersion" }
#                                          ^^^^^^^
#                                          ERROR! "abc" is not a recognized property
```

**Result:** Gradle will throw an error because `abc` is not a valid property. Only specific properties are supported:
- `version.ref` - reference a version from `[versions]`
- `version` - specify version directly
- `module` - specify the module coordinates
- `group` and `name` - alternative to `module`

## Valid ways to define library versions:

### 1. Using `version.ref` (references `[versions]`)
```toml
[versions]
myVersion = "1.0.0"

[libraries]
my-lib = { module = "com.example:my-lib", version.ref = "myVersion" }
```

### 2. Using `version` directly (inline)
```toml
[libraries]
my-lib = { module = "com.example:my-lib", version = "1.0.0" }
```

### 3. Using `group`, `name`, `version.ref`
```toml
[versions]
myVersion = "1.0.0"

[libraries]
my-lib = { group = "com.example", name = "my-lib", version.ref = "myVersion" }
```

### 4. Using `group`, `name`, `version` directly
```toml
[libraries]
my-lib = { group = "com.example", name = "my-lib", version = "1.0.0" }
```

## Why this design?

The version catalog API is intentionally structured with fixed sections:
- `[versions]` - stores version strings
- `[libraries]` - defines library coordinates
- `[bundles]` - groups multiple libraries
- `[plugins]` - defines plugin coordinates

Each section has its own predefined properties and APIs. You can't create custom sections like `[abcs]` and reference them with `abc.ref`.

**Think of it like this:**
```kotlin
// Conceptually, the version catalog is like:
object VersionCatalog {
    object versions {
        val kotlin = "1.9.22"
    }
    
    object libraries {
        val kotlinLogging = Library(
            module = "io.github.microutils:kotlin-logging-jvm",
            version = versions.kotlin  // version.ref points here
        )
    }
}
```

So `version.ref` is a specific pointer to the `versions` object - you can't create arbitrary references to other sections.


# Another sample

Here's your refined multi-module Gradle configuration:

## Project Structure
```
demo/
├── build.gradle.kts (root)
├── settings.gradle.kts
├── gradle.properties
├── app/
│   ├── build.gradle.kts
│   └── src/main/kotlin/...
├── domain/
│   ├── build.gradle.kts
│   └── src/main/kotlin/...
├── infrastructure/
│   ├── build.gradle.kts
│   └── src/main/kotlin/...
└── api/
    ├── build.gradle.kts
    └── src/main/kotlin/...
```

## Root `settings.gradle.kts`
```kotlin
rootProject.name = "demo"

include("app")
include("domain")
include("infrastructure")
include("api")
```

## Root `build.gradle.kts`
```kotlin
plugins {
    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.spring") version "2.2.21" apply false
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

extra["springModulithVersion"] = "2.0.1"

allprojects {
    group = "org.xyp.project"
    version = "0.0.1-SNAPSHOT"
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")
    
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
    
    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }
    
    dependencyManagement {
        imports {
            mavenBom("org.springframework.modulith:spring-modulith-bom:${rootProject.extra["springModulithVersion"]}")
        }
    }
    
    dependencies {
        // Common dependencies for all modules
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        annotationProcessor("org.projectlombok:lombok")
        
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
    
    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
        }
    }
    
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
```

## Module `domain/build.gradle.kts` (Pure domain logic, no Spring Boot)
```kotlin
description = "Domain module - business logic and entities"

dependencies {
    // Domain-specific dependencies (if needed)
    // No Spring Boot dependencies here to keep domain clean
}
```

## Module `infrastructure/build.gradle.kts` (Data access, external services)
```kotlin
description = "Infrastructure module - persistence and external integrations"

dependencies {
    implementation(project(":domain"))
    
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webclient")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    
    runtimeOnly("com.h2database:h2") // or your database driver
    
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webclient-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}
```

## Module `api/build.gradle.kts` (REST controllers, web layer)
```kotlin
description = "API module - REST controllers and web configuration"

dependencies {
    implementation(project(":domain"))
    implementation(project(":infrastructure"))
    
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}
```

## Module `app/build.gradle.kts` (Main application - executable)
```kotlin
plugins {
    id("org.springframework.boot")
}

description = "Application module - main entry point"

dependencies {
    implementation(project(":domain"))
    implementation(project(":infrastructure"))
    implementation(project(":api"))
    
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    mainClass.set("org.xyp.project.ApplicationKt")
}

tasks.getByName<Jar>("jar") {
    enabled = false
}
```

## Optional: `gradle.properties`
```properties
kotlin.code.style=official
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
```

## Key Changes:

1. **Centralized plugin management** - All plugins declared in root with `apply false`
2. **Shared configuration** - Common dependencies, Kotlin options, and Java toolchain in `subprojects` block
3. **Module separation**:
    - `domain` - Pure business logic (no Spring dependencies)
    - `infrastructure` - Data access, external APIs
    - `api` - Web controllers and endpoints
    - `app` - Main application (only module with `spring-boot` plugin applied)
4. **Dependency management** - Spring Modulith BOM configured once in root
5. **Inter-module dependencies** - Modules depend on each other via `project(":module-name")`
6. **Only `app` module is executable** - `bootJar` enabled only in app module

## To build and run:
```bash
# Build all modules
./gradlew build

# Run the application
./gradlew :app:bootRun

# Build only specific module
./gradlew :domain:build
```

This structure follows clean architecture principles and allows you to keep concerns separated while maintaining all the benefits of centralized dependency management.


Usually by checking the plugin’s **ID** and its **DSL alias/shorthand**.

## In your example

These are the same plugin:

```kotlin
kotlin("jvm")
```

and

```kotlin
id("org.jetbrains.kotlin.jvm")
```

So the **plugin name/shorthand** is `kotlin("jvm")`, and the **apply ID** is `"org.jetbrains.kotlin.jvm"`.

---

## How to find the relationship

### 1. Check the plugin documentation
The official docs usually show both:

- the **plugins block syntax**
- the **full plugin ID**

For Kotlin plugins, JetBrains documents things like:

```kotlin
plugins {
    kotlin("jvm") version "..."
}
```

which maps to:

```kotlin
id("org.jetbrains.kotlin.jvm") version "..."
```

Other common Kotlin mappings:

- `kotlin("jvm")` → `org.jetbrains.kotlin.jvm`
- `kotlin("plugin.spring")` → `org.jetbrains.kotlin.plugin.spring`
- `kotlin("plugin.jpa")` → `org.jetbrains.kotlin.plugin.jpa`
- `kotlin("kapt")` → `org.jetbrains.kotlin.kapt`

---

### 2. Recognize that `kotlin("...")` is Kotlin DSL sugar
In Gradle Kotlin DSL, `kotlin("jvm")` is just a convenience function for Kotlin plugins.

So if you see:

```kotlin
kotlin("something")
```

it is usually shorthand for a plugin ID under:

```kotlin
org.jetbrains.kotlin.*
```

For example:

```kotlin
kotlin("plugin.spring")
```

maps to:

```kotlin
org.jetbrains.kotlin.plugin.spring
```

---

### 3. Look at the Gradle Plugin Portal
Search the plugin in the **Gradle Plugin Portal**. It will show the canonical plugin ID.

For example, you’d find the plugin ID:

```text
org.jetbrains.kotlin.jvm
```

That is the string you use in:

```kotlin
apply(plugin = "org.jetbrains.kotlin.jvm")
```

---

### 4. Check IntelliJ/IDE auto-completion
If you type in a `plugins` block:

```kotlin
plugins {
    kotlin(
}
```

or

```kotlin
plugins {
    id(
}
```

IDE suggestions often reveal the available shorthand or full IDs.

---

### 5. Read the plugin source/docs if unclear
Some plugins have no shorthand like `kotlin("...")`. In those cases, the only reliable thing is the documented plugin ID, e.g.:

```kotlin
id("org.springframework.boot")
```

There is no built-in shorthand like:

```kotlin
springBoot(...)
```

So the plugin ID itself is what you use for both declaration and apply.

---

## Rule of thumb

### If declared like this:

```kotlin
plugins {
    id("some.plugin.id") version "1.2.3" apply false
}
```

then apply it with:

```kotlin
apply(plugin = "some.plugin.id")
```

### If declared like this:

```kotlin
plugins {
    kotlin("jvm") version "2.2.21" apply false
}
```

then find its underlying plugin ID from docs, which is:

```kotlin
org.jetbrains.kotlin.jvm
```

and apply it with:

```kotlin
apply(plugin = "org.jetbrains.kotlin.jvm")
```

---

## Important distinction

There is no universal automatic conversion from arbitrary shorthand to plugin ID, except for specific DSL helpers like Kotlin’s:

```kotlin
kotlin("jvm")
```

For most plugins, what matters is the **plugin ID**.

So this is always safe:

```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.21" apply false
}
```

and then:

```kotlin
apply(plugin = "org.jetbrains.kotlin.jvm")
```

That avoids needing to remember the shorthand mapping.

---

## Practical advice

If you want the most explicit and discoverable style, use full IDs everywhere:

```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.21" apply false
    id("org.jetbrains.kotlin.plugin.spring") version "2.2.21" apply false
}
```

Then later:

```kotlin
apply(plugin = "org.jetbrains.kotlin.jvm")
apply(plugin = "org.jetbrains.kotlin.plugin.spring")
```

This makes the relationship obvious.

---

## For your current plugins

Mappings are:

```kotlin
kotlin("jvm")                    -> org.jetbrains.kotlin.jvm
kotlin("plugin.spring")          -> org.jetbrains.kotlin.plugin.spring
id("org.springframework.boot")   -> org.springframework.boot
id("io.spring.dependency-management") -> io.spring.dependency-management
```

---

If you want, I can also give you a **cheat sheet of common Kotlin Gradle plugin shorthands and their full IDs**.