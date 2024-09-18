package at.xirado.bean.database.exposed

import at.xirado.bean.data.cache.CacheManager
import at.xirado.bean.data.cache.CacheView
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

context(R)
infix fun <C : Entity<ID>, ID : Comparable<ID>, R : Entity<*>, T : Any> Column<T>.provideUsing(
    cacheView: CacheView<ID, C>
): CachedEntityDelegate<C, ID, R> {
    return CachedEntityDelegate(this as Column<Any>, cacheView)
}

context(R)
infix fun <C : Entity<ID>, ID : Comparable<ID>, R : Entity<*>, T : Any> Column<T?>.provideUsing(
    cacheView: CacheView<ID, C>
): NullableCachedEntityDelegate<C, ID, R> {
    return NullableCachedEntityDelegate(this as Column<Any?>, cacheView)
}

class CachedEntityDelegate<C : Entity<ID>, ID : Comparable<ID>, R : Entity<*>>(
    private val column: Column<Any>,
    private val cacheView: CacheView<ID, C>,
) : ReadWriteProperty<R, C>, CacheManager {
    var cached: C? = null
    var isInitialized: Boolean = false
    var id: ID? = null

    override fun getValue(thisRef: R, property: KProperty<*>): C {
        if (isInitialized)
            return cached!!

        val id = unwrapEntityID<ID>(thisRef.readValues[column])

        this.id = id
        cached = cacheView.getByIdBlocking(id)
            ?: throw IllegalStateException("Expected non-null value for ${property.name}, but got null instead. (Id: $id)")
        isInitialized = true
        return cached!!
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: C) {
        thisRef as Entity<Comparable<Any>>
        with (thisRef) {
            column.setValue(thisRef, property, value.id)
        }

        val id = unwrapEntityID<ID>(value.id)

        if (id !in cacheView)
            cacheView[id] = value

        cached = value
        isInitialized = true
    }
}

class NullableCachedEntityDelegate<C : Entity<ID>, ID : Comparable<ID>, R : Entity<*>>(
    private val column: Column<Any?>,
    private val cacheView: CacheView<ID, C>,
) : ReadWriteProperty<R, C?>, CacheManager {
    var cached: C? = null
    var isInitialized: Boolean = false
    var id: ID? = null

    override fun getValue(thisRef: R, property: KProperty<*>): C? {
        if (isInitialized)
            return cached

        val id = thisRef.readValues[column]?.let { unwrapEntityID<ID>(it) } ?: return null

        this.id = id
        cached = cacheView.getByIdBlocking(id)
        isInitialized = true
        return cached
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: C?) {
        thisRef as Entity<Comparable<Any>>
        with (thisRef) {
            column.setValue(thisRef, property, value?.id)
        }

        val id = value?.id?.let { unwrapEntityID<ID>(it) }

        if (value != null && id!! !in cacheView)
            cacheView[id] = value

        cached = value
        isInitialized = true
    }
}

// Helper function to recursively unwrap EntityID
@Suppress("UNCHECKED_CAST")
tailrec fun <ID : Comparable<ID>> unwrapEntityID(value: Any?): ID {
    return when (value) {
        is EntityID<*> -> unwrapEntityID(value.value)
        is Comparable<*> -> (value as ID)
        else -> throw IllegalArgumentException("Unsupported value type: ${value?.javaClass}")
    }
}