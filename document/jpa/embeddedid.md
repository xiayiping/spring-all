# Embedded Id

Jpa doesn't support 'IdGenerator' for an embedded id.

so if you want to generated value for an embedded , you need to put generator annotation inside embedded.

```kotlin


@Embeddable
data class AppPersonId(
    @CustomizedEmbeddedId
    var id: UUID? = null,  //
)
```

in the owner class:
```kotlin
@Entity
@Table
data class AppPerson(
    @EmbeddedId
    var psid: AppPersonId = AppPersonId(),
) : Persistable<AppPersonId> {
    
    @JsonIgnore
    override fun getId(): AppPersonId? {
        return psid
    }

    @JsonIgnore
    override fun isNew(): Boolean {
        return null == psid.id
    }
}
```

Any DIFFERENT attempt will fail.

but due to the id itself is not null, JPA will query existence before insert.

In order to get better performance , owner need to implement Persistable like the examples shows.