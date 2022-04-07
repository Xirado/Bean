package at.xirado.bean.util

import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

private const val ERROR_EMOTE = "<:error:943524725487968298>"
private fun ERROR_EMBED(message: String) = Embed { description = "$ERROR_EMOTE $message"; color = 0x8b0000 }

fun GenericCommandInteractionEvent.replyError(message: String) = replyEmbeds(ERROR_EMBED(message))

fun GenericCommandInteractionEvent.sendErrorMessage(message: String, ephemeral: Boolean = true) = hook.sendMessageEmbeds(ERROR_EMBED(message))