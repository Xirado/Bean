package at.xirado.bean.util

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent




fun GenericCommandInteractionEvent.replyError(message: String, ephemeral: Boolean = false) = replyEmbeds(errorEmbed(message))
    .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.sendErrorMessage(message: String, ephemeral: Boolean = false) = hook.sendMessageEmbeds(errorEmbed(message))
    .setEphemeral(ephemeral)