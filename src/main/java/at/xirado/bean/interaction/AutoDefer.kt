package at.xirado.bean.interaction

import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.*
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

suspend fun IReplyCallback.autoDefer(
    ephemeral: Boolean = false,
    block: suspend () -> MessageCreateData
) {
    suspend fun getElement() = currentCoroutineContext()[AutoDeferCoroutineContextElement.Key]

    coroutineScope {
        withContext(AutoDeferCoroutineContextElement(ephemeral)) {
            val resultDeferred = async { block() }

            val result = withTimeoutOrNull(2.seconds) {
                resultDeferred.await()
            }

            if (result == null) {
                val loadingMessage = getElement()?.loadingMessage
                val isEphemeral = getElement()?.ephemeral ?: ephemeral

                if (loadingMessage != null) {
                    reply(loadingMessage).setEphemeral(isEphemeral).queue()
                    val message = resultDeferred.await()

                    hook.editOriginal(MessageEditData.fromCreateData(message)).await()
                } else {
                    deferReply(isEphemeral).queue()
                    val message = resultDeferred.await()
                    hook.sendMessage(message).await()
                }
            } else {
                val isEphemeral = getElement()?.ephemeral ?: ephemeral
                reply(result).setEphemeral(isEphemeral).await()
            }
        }
    }
}

class AutoDeferCoroutineContextElement(var ephemeral: Boolean) : AbstractCoroutineContextElement(Key) {
    var loadingMessage: MessageCreateData? = null

    companion object Key : CoroutineContext.Key<AutoDeferCoroutineContextElement>
}

suspend fun GenericCommandInteractionEvent.onDefer(edit: suspend AutoDeferCoroutineContextElement.() -> Unit) {
    val element = currentCoroutineContext()[AutoDeferCoroutineContextElement.Key]
        ?: throw IllegalStateException("Current coroutine has no AutoDeferCoroutineContextElement!")

    element.edit()
}