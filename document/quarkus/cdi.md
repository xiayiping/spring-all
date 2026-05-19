# conditional injection

[https://quarkus.io/guides/cdi-reference#enable_build_profile](https://quarkus.io/guides/cdi-reference#enable_build_profile)

Yes. Quarkus has several ways to choose among multiple implementations of the same interface, somewhat like Spring Boot, but the mechanism is **CDI/ArC-based**, not Spring’s `@Conditional...` model. ([quarkus.io](https://quarkus.io/guides/cdi?utm_source=openai))

## 1. The basic CDI way: qualifiers

If you have two implementations of the same interface, the standard Quarkus/CDI way is to distinguish them with **qualifiers**. Quarkus’ CDI guide explains that qualifiers are how the container resolves multiple beans of the same type. ([quarkus.io](https://quarkus.io/guides/cdi?utm_source=openai))

```kotlin
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION
)
annotation class EmailChannel

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION
)
annotation class SmsChannel

interface Notifier {
    fun send(msg: String)
}

@EmailChannel
@ApplicationScoped
class EmailNotifier : Notifier {
    override fun send(msg: String) = println("email: $msg")
}

@SmsChannel
@ApplicationScoped
class SmsNotifier : Notifier {
    override fun send(msg: String) = println("sms: $msg")
}

@ApplicationScoped
class Service(
    @Inject @EmailChannel private val notifier: Notifier
) {
    fun work() = notifier.send("hello")
}
```

This is best when the choice is **semantic** rather than environment-based. ([quarkus.io](https://quarkus.io/guides/cdi?utm_source=openai))

## 2. Environment/profile-based selection: `@IfBuildProfile` / `@UnlessBuildProfile`

Quarkus supports **conditional beans** based on the **build profile**, via `@IfBuildProfile` and `@UnlessBuildProfile`. The CDI reference documents these annotations specifically as Quarkus extensions to CDI. ([ja.quarkus.io](https://ja.quarkus.io/guides/cdi-reference.html?utm_source=openai))

Example:

```kotlin
import io.quarkus.arc.DefaultBean
import io.quarkus.arc.profile.IfBuildProfile
import jakarta.enterprise.context.ApplicationScoped

interface PaymentGateway {
    fun pay(): String
}

@IfBuildProfile("prod")
@ApplicationScoped
class RealPaymentGateway : PaymentGateway {
    override fun pay() = "real payment"
}

@DefaultBean
@ApplicationScoped
class NoopPaymentGateway : PaymentGateway {
    override fun pay() = "noop payment"
}
```

This means:

- in **prod** build profile, `RealPaymentGateway` is enabled
- otherwise, the `@DefaultBean` fallback can be used. ([ja.quarkus.io](https://ja.quarkus.io/guides/cdi-reference.html?utm_source=openai))

## 3. Property-based selection: `@IfBuildProperty` / `@UnlessBuildProperty`

Quarkus also supports conditional beans based on **build-time properties**, using `@IfBuildProperty` and `@UnlessBuildProperty`. The CDI reference describes these as a way to enable beans when a Quarkus build-time property has or does not have a given value. ([ja.quarkus.io](https://ja.quarkus.io/guides/cdi-reference.html?utm_source=openai))

Conceptually:

```kotlin
import io.quarkus.arc.properties.IfBuildProperty
import jakarta.enterprise.context.ApplicationScoped

@IfBuildProperty(name = "feature.mock-payment", stringValue = "false")
@ApplicationScoped
class RealPaymentGateway : PaymentGateway {
    override fun pay() = "real payment"
}
```

That is the closest Quarkus equivalent to a Spring conditional annotation. But note the important limitation: these are **build-time conditions**, not arbitrary runtime switching. The Quarkus docs explicitly frame them as build-profile/build-property bean enablement. ([ja.quarkus.io](https://ja.quarkus.io/guides/cdi-reference.html?utm_source=openai))

## 4. Alternatives

Quarkus also supports CDI **alternatives**. The CDI reference says alternatives can be selected using `@Priority`, and Quarkus also provides the `quarkus.arc.selected-alternatives` configuration property to select them application-wide. ([quarkus.io](https://quarkus.io/guides/cdi-reference?utm_source=openai))

This is useful when you want one implementation to override another without changing every injection point.

## 5. Programmatic lookup for runtime choice

If you need to choose implementation at **runtime**, inject `Instance<YourInterface>` and decide yourself. Quarkus’ CDI guide explicitly shows `Instance<T>` as the way to resolve multiple beans programmatically. ([quarkus.io](https://quarkus.io/guides/cdi?utm_source=openai))

Example:

```kotlin
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import jakarta.inject.Inject

@ApplicationScoped
class NotificationService(
    @Inject private val notifiers: Instance<Notifier>
) {
    fun sendAll(msg: String) {
        for (notifier in notifiers) {
            notifier.send(msg)
        }
    }
}
```

Or combine that with qualifiers and config-based branching in your own code.

## Important difference from Spring Boot

Spring often does this with runtime condition annotations like:

- `@Profile`
- `@ConditionalOnProperty`
- `@ConditionalOnMissingBean`

In Quarkus, the nearest equivalents are:

- **qualifiers** for disambiguation
- **`@IfBuildProfile` / `@UnlessBuildProfile`**
- **`@IfBuildProperty` / `@UnlessBuildProperty`**
- **`@DefaultBean`**
- **alternatives**
- **`Instance<T>` for runtime selection**. ([quarkus.io](https://quarkus.io/guides/cdi-reference?utm_source=openai))

Also, Quarkus documentation notes that build-profile annotations are about **build-time** bean resolution, and runtime profiles do not affect bean resolution through those annotations. ([ja.quarkus.io](https://ja.quarkus.io/guides/cdi-reference.html?utm_source=openai))

## Practical recommendation

Use:

- **qualifiers** when both implementations may coexist
- **`@IfBuildProfile` / `@IfBuildProperty` + `@DefaultBean`** when you want one implementation enabled for an environment/config
- **`Instance<T>`** if the choice must happen dynamically at runtime. ([quarkus.io](https://quarkus.io/guides/cdi?utm_source=openai))

## Short answer

So: **yes**, Quarkus absolutely supports this, but usually through **CDI qualifiers and Quarkus conditional bean annotations**, rather than Spring Boot’s exact annotation set. ([quarkus.io](https://quarkus.io/guides/cdi?utm_source=openai))

If you want, I can show you the **exact Quarkus/Kotlin equivalent** of one of these Spring patterns:

- `@Profile`
- `@ConditionalOnProperty`
- `@ConditionalOnMissingBean`
- multiple implementations with `@Qualifier`

---
Learn more:
1. [Introduction to Contexts and Dependency Injection (CDI) - Quarkus](https://quarkus.io/guides/cdi?utm_source=openai)
2. [コンテキストと依存性インジェクション - Quarkus](https://ja.quarkus.io/guides/cdi-reference.html?utm_source=openai)
3. [Contexts and Dependency Injection - Quarkus](https://quarkus.io/guides/cdi-reference?utm_source=openai)