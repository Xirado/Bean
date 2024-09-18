package at.xirado.bean.interaction

import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import kotlin.time.Duration.Companion.seconds

suspend fun IReplyCallback.autoDefer(
    ephemeral: Boolean = false,
    block: suspend () -> MessageCreateData
): Unit = coroutineScope {
    val resultDeferred = async { block() }

    val result = withTimeoutOrNull(2.seconds) {
        resultDeferred.await()
    }

    if (result == null) {
        deferReply(ephemeral).queue()
        hook.sendMessage(resultDeferred.await()).await()
    } else {
        reply(result).setEphemeral(ephemeral).await()
    }
}