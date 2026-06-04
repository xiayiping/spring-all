# Persist Layer

for java/kotlin, Always use spring data jpa as persist layer. 

# Id obsession

Always use id wrapper for single value id,  (compound is always wrapped).

Meanwhile, use `@JsonValue` to erase the wrapper in output json.

so normally the constructor of entity should accept an idGenerator

# Versioning and new entity detection

Always use `@Version` as the new entity detection.

Always create the ID for new created entity. (id not managed by JPA, JPA check new by @Version decorated field).
So that ID is always non-null value.

# One to Many

If proper oneToMany decorated, means the one is the aggregate-root.
So one to many always managed by one side, join by a field on many side.

The reference FK also need to be wrapped using `one` side's ID, with `@AttributeOverride`.

Fetch should be always lazy, explicitly write `@Query` with fetch join in Repository if need eager fetch many side.

# Example

For a plain normal persisting an aggregate root plus it's child entity.

```kotlin


import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.*

@Entity
@Table(name = "e_person")
class Person(
    // for a long value if not re 'real' number, use string to avoid javascript overflow.
    @EmbeddedId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val id: PersonId,

    var name: String? = null,

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = false)
    @JoinColumn(name = "person_id")
    var addresses: MutableList<Address> = mutableListOf(),

    // default null value can also be used to check new-entity
    @Version
    var optimisticVersion: Long? = null,
) {

    constructor(idGenerator: IdGenerator) : this(id = PersonId(idGenerator.generateId()))
}

@Embeddable
data class PersonId(
    // use JsonValue to erase the wrapper in output json
    @JsonValue
    val id: Long
)


@Entity
@Table(name = "e_address")
class Address(

    // for a long value if not re 'real' number, use string to avoid javascript overflow.
    @EmbeddedId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val id: AddressId,

    var name: String? = null,

    @Embedded
    @AttributeOverrides(value = [
        AttributeOverride(name = "id", column = Column(name = "person_id"))
    ])
    val personId: PersonId? = null,

    // default null value can also be used to check new-entity
    @Version
    var optimisticVersion: Long? = null,

    ) {

    constructor(idGenerator: IdGenerator) : this(id = AddressId(idGenerator.generateId()))
}

@Embeddable
data class AddressId(
    // use JsonValue to erase the wrapper in output json
    @JsonValue
    val id: Long
)

```
