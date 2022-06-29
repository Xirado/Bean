package at.xirado.bean.interaction

import net.dv8tion.jda.api.events.GenericEvent
import javax.script.CompiledScript
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

@Suppress("unused")
annotation class EventListener(val clazz: KClass<out GenericEvent>)

annotation class EventCondition(val condition: String)

data class Listener(val method: KCallable<*>,
                    val instance: Any,
                    val event: KClass<out GenericEvent>,
                    val condition: EventCondition?,
                    val compiledScript: CompiledScript?)

fun parse(clazz: KClass<out GenericEvent>, condition: String): String {
    val simpleName = clazz.simpleName
    return """
        import ${clazz.java.name}
                
        val condition: $simpleName.() -> Boolean = { ${trimQuotes(condition)} }
        
        fun check(event: $simpleName) = condition.invoke(event) 
            
    """.trimIndent()
}

fun trimQuotes(input: String): String {
    var current = input
    current = current.replace("\\\\`".toRegex(), "\uFFFF")
    current = current.replace("`".toRegex(), "\"")
    current = current.replace("\uFFFF".toRegex(), "`")
    return current
}