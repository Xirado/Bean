package at.xirado.bean.data

import at.xirado.simplejson.FileType
import at.xirado.simplejson.JSONObject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

abstract class DataContainer<T : Any>(json: String, fileType: FileType) : JSONObject(json, fileType)  {
    abstract val onUpdate: suspend (JSONObject) -> Unit
    private val mutex = Mutex()
    private val instance: T = this as T

    @Suppress("UNCHECKED_CAST")
    suspend fun update(block: T.() -> Unit) {
        mutex.withLock {
            instance.apply(block)
            instance::class.memberProperties
                .filter { it is KMutableProperty<*> }
                .map { (it as KProperty1<T, *>).also { if (!it.isAccessible) it.isAccessible = true } }
                .filter { it.getDelegate(instance) is StorableProperty }
                .forEach { (it.getDelegate(instance) as StorableProperty).save(this) }
            onUpdate.invoke(this)
        }
    }
}