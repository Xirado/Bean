package at.xirado.bean.database.exposed

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <ID : Comparable<ID>, R : Entity<ID>, E : Entity<ID>, C : EntityClass<ID, E>> R.related(target: C): ReadWriteProperty<R, E>
    = NonNullRelatedEntity(id, target)

fun <ID : Comparable<ID>, R : Entity<ID>, E : Entity<ID>, C : EntityClass<ID, E>> R.optionalRelated(target: C): ReadWriteProperty<R, E?>
    = NullableRelatedEntity(id, target)

class NullableRelatedEntity<ID : Comparable<ID>, R : Entity<ID>, E : Entity<ID>, C : EntityClass<ID, E>>(
    private val id: EntityID<ID>,
    private val entityClass: C,
) : ReadWriteProperty<R, E?> {
    private var value: E? = null
    private var wasSetToNull = false

    override fun getValue(thisRef: R, property: KProperty<*>): E? {
        value?.let { return it }
        if (wasSetToNull)
            return null

        val fetched = entityClass.findById(id)
        value = fetched

        return fetched
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: E?) {
        wasSetToNull = value == null
        this.value = value
    }
}

class NonNullRelatedEntity<ID : Comparable<ID>, R : Entity<ID>, E : Entity<ID>, C : EntityClass<ID, E>>(
    private val id: EntityID<ID>,
    private val entityClass: C,
) : ReadWriteProperty<R, E> {
    private var value: E? = null

    override fun getValue(thisRef: R, property: KProperty<*>): E {
        value?.let { return it }

        val fetched = entityClass.findById(id)
        value = fetched

        if (fetched == null)
            throw IllegalStateException("Expected non-null entity for receiver ${property.name}")

        return fetched
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: E) {
        this.value = value
    }
}