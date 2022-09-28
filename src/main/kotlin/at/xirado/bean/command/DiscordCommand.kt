package at.xirado.bean.command

import at.xirado.bean.Application
import at.xirado.bean.i18n.LocalizedMessageReference
import dev.minn.jda.ktx.events.CoroutineEventListener
import net.dv8tion.jda.api.events.GenericEvent

interface DiscordCommand : CoroutineEventListener {
    val app: Application

    fun messageReference(path: String): LocalizedMessageReference {
        val manager = app.localizationManager
        val references = manager.messageReferences

        return references
            .find { it.path == path }
            ?: manager.default.get(path)
                ?.let { LocalizedMessageReference(path) }
                ?.also { references += it }
                ?: throw NoSuchFieldException("Default-Locale does not contain path $path!")
    }

    override suspend fun onEvent(event: GenericEvent) {}
}