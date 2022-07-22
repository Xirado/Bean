package at.xirado.bean.data

import at.xirado.simplejson.JSONObject
import kotlin.reflect.KProperty

class JSONDelegate<T>(val key: String,
                      val default: T,
                      val getter: ((JSONDelegate<T>) -> T)? = null,
                      val setter: ((T, JSONDelegate<T>) -> Any?)? = null) : ReadWriteDelegate<JSONObject, T> {

    override operator fun getValue(thisRef: JSONObject, property: KProperty<*>): T {
        return if (getter != null && !thisRef.isNull(key)) {
            getter.invoke(this)
        } else {
            if (thisRef.isNull(key))
                default
            else
                thisRef.get(key) as T
        }
    }

    override operator fun setValue(thisRef: JSONObject, property: KProperty<*>, value: T) {
        if (setter != null)
            setter.invoke(value, this)
        else
            thisRef.put(key, value)
    }
}

interface ReadWriteDelegate<T, R> {
    operator fun getValue(thisRef: T, property: KProperty<*>): R
    operator fun setValue(thisRef: T, property: KProperty<*>, value: R)
}

