package at.xirado.bean.interaction

import at.xirado.bean.Application
import at.xirado.bean.coroutineScope
import at.xirado.bean.util.getLog
import dev.minn.jda.ktx.events.CoroutineEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.events.GenericEvent
import javax.script.*
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

private val log = getLog<ListenerManager>()

class ListenerManager(val application: Application) : CoroutineEventListener {

    private val listeners = mutableMapOf<Class<out GenericEvent>, MutableList<Listener>>()
    private val mutex = Mutex()

    val engine: ScriptEngine = ScriptEngineManager().getEngineByName("kotlin")

    override suspend fun onEvent(event: GenericEvent) {
        val listeners = listeners[event.javaClass] ?: return
        val jdaEvent = event.javaClass.cast(event)

        listeners.forEach { listener ->
            coroutineScope.launch {
                if (listener.condition == null)
                    return@launch kotlin.run { listener.method.callSuspend(listener.instance, jdaEvent) }
                val context = SimpleScriptContext()
                val result = mutex.withLock {
                    listener.compiledScript!!.eval(context)
                    engine.context = context
                    (engine as Invocable).invokeFunction("check", jdaEvent)
                }
                if (result is Boolean && result)
                    listener.method.callSuspend(listener.instance, jdaEvent).also { log.info("END") }
            }
        }
    }

    private fun register(clazz: Class<out GenericEvent>, listener: Listener) {
        listeners.computeIfAbsent(clazz) { mutableListOf() }.add(listener)
    }

    fun registerListeners(instance: Any) {
        val methods = instance::class.members
            .filter { it.hasAnnotation<EventListener>() }

        methods.forEach {
            val eventListener = it.findAnnotation<EventListener>()!!
            if (it.parameters.size > 2 || it.parameters[1].type != eventListener.clazz.createType()) {
                log.error("${instance.javaClass.simpleName}#${it.name}: Invalid method parameters.")
            }
            val condition = it.findAnnotation<EventCondition>()
            val compiled = if (condition != null) {
                try {
                    synchronized(engine) {
                        val parsed = parse(eventListener.clazz, condition.condition)
                        (engine as Compilable).compile(parsed)
                    }
                } catch (throwable: Throwable) {
                    log.error("An error occurred while compiling listener condition: ${instance.javaClass.simpleName}#${it.name}", throwable)
                    return@forEach
                }
            } else {
                null
            }

            val listener = Listener(it, instance, eventListener.clazz, condition, compiled)

            register(eventListener.clazz.java, listener)
        }
    }
}