package at.xirado.bean.data

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import java.util.*
import kotlin.reflect.KClass

interface BitField {
    val offset: Int
}

inline fun <reified E : Enum<E>> enumSetOf(): EnumSet<E> = EnumSet.noneOf(E::class.java)

// Helper function to cast a Long to the correct Number type
@Suppress("UNCHECKED_CAST")
fun <T : Number> Long.toNumber(type: KClass<T>): T = when (type) {
    Byte::class -> this.toByte()
    Short::class -> this.toShort()
    Int::class -> this.toInt()
    Long::class -> this
    else -> throw IllegalArgumentException("Unsupported Number type: ${type.simpleName}")
} as T

context(Table)
inline fun <reified T, reified E> Column<T>.bitField(): Column<EnumSet<E>>
        where T : Number, E : Enum<E>, E : BitField {

    return transform(
        wrap = { number ->
            val longNumber = number.toLong()
            enumValues<E>().filterTo(enumSetOf<E>()) { bitFieldEnum ->
                longNumber and (1L shl bitFieldEnum.offset) != 0L
            }
        },
        unwrap = { enumSet ->
            val longValue = enumSet.fold(0L) { acc, bitFieldEnum ->
                acc or (1L shl bitFieldEnum.offset)
            }
            longValue.toNumber(T::class)
        }
    )
}