package at.xirado.bean.data

import at.xirado.simplejson.JSONArray
import at.xirado.simplejson.JSONObject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface StorableProperty {
    fun save(json: JSONObject): JSONObject
}

class JSONProperty<T>(val key: String,
                      val default: T? = null,
                      val getter: ((JSONObject, JSONProperty<T>) -> T?)? = null,
                      val setter: ((T, JSONProperty<T>, JSONObject) -> JSONObject)? = null) {

    operator fun provideDelegate(thisRef: JSONObject, property: KProperty<*>): ReadWriteProperty<JSONObject, T> {
        val result: Any = if (property.returnType.isMarkedNullable)
            NullableJSONProperty(this)
        else {
            if (default == null)
                throw IllegalArgumentException("Non-Null json properties need a default value!")
            NotNullJSONProperty(this)
        }

        @Suppress("UNCHECKED_CAST")
        return result as ReadWriteProperty<JSONObject, T>
    }

    class NotNullJSONProperty<T>(val property: JSONProperty<T>) : ReadWriteProperty<JSONObject, T>, StorableProperty {
        val key = property.key
        val default = property.default!!
        val getter = property.getter
        val setter = property.setter

        var initialized = false
        var value: T = default

        override fun save(json: JSONObject): JSONObject {
            if (!initialized) return json
            if (setter == null) {
                if (value is Collection<*>)
                    return json.put(key, JSONArray.fromCollection(value as Collection<*>))
                return json.put(key, value)
            }
            return setter.invoke(value, property, json)
        }

        @Synchronized
        override fun getValue(thisRef: JSONObject, property: KProperty<*>): T {
            if (!initialized) {
                value = if (getter == null) {
                    if (thisRef.isNull(key))
                        default
                    else
                        thisRef.get(key) as T
                } else {
                    getter.invoke(thisRef, this.property) ?: default
                }
                initialized = true
            }
            return value
        }

        @Synchronized
        override fun setValue(thisRef: JSONObject, property: KProperty<*>, value: T) {
            this.value = value
            if (!initialized) initialized = true
        }
    }

    class NullableJSONProperty<T>(val property: JSONProperty<T>) : ReadWriteProperty<JSONObject, T?>, StorableProperty {
        val key = property.key
        val default = property.default
        val getter = property.getter
        val setter = property.setter as ((T?, JSONProperty<T>, JSONObject) -> JSONObject)?

        var initialized = false
        var value: T? = default

        override fun save(json: JSONObject): JSONObject {
            if (!initialized) return json
            if (setter == null) {
                if (value is Collection<*>)
                    return json.put(key, JSONArray.fromCollection(value as Collection<*>))
                return json.put(key, value)
            }
            return setter.invoke(value, property, json)
        }

        @Synchronized
        override fun getValue(thisRef: JSONObject, property: KProperty<*>): T? {
            if (!initialized) {
                value = if (getter == null) {
                    if (thisRef.isNull(key))
                        default
                    else
                        thisRef.get(key) as T
                } else {
                    getter.invoke(thisRef, this.property) ?: default
                }
                initialized = true
            }
            return value
        }

        @Synchronized
        override fun setValue(thisRef: JSONObject, property: KProperty<*>, value: T?) {
            this.value = value
            if (!initialized) initialized = true
        }
    }
}

