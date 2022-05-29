package at.xirado.bean.util

import at.xirado.bean.getUserI18n
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

fun GenericCommandInteractionEvent.replyError(message: String, ephemeral: Boolean = false) = replyEmbeds(errorEmbed(message))
    .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.sendErrorMessage(message: String, ephemeral: Boolean = false) = hook.sendMessageEmbeds(errorEmbed(message))
    .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.replyErrorLocalized(path: String, vararg attributes: Pair<String, Any>, ephemeral: Boolean = false)
    = replyError(getUserI18n().get(path, *attributes), ephemeral = ephemeral)

fun GenericCommandInteractionEvent.sendErrorLocalized(path: String, vararg attributes: Pair<String, Any>, ephemeral: Boolean = false) =
    hook.sendMessageEmbeds(errorEmbed(getUserI18n().get(path, *attributes))).setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.replySuccess(message: String, ephemeral: Boolean = false) = replyEmbeds(successEmbed(message))
    .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.sendSuccessMessage(message: String, ephemeral: Boolean = false) = hook.sendMessageEmbeds(successEmbed(message))
    .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.replySuccessLocalized(path: String, vararg attributes: Pair<String, Any>, ephemeral: Boolean = false)
        = replySuccess(getUserI18n().get(path, *attributes), ephemeral = ephemeral)

fun GenericCommandInteractionEvent.sendSuccessLocalized(path: String, vararg attributes: Pair<String, Any>, ephemeral: Boolean = false) =
    hook.sendMessageEmbeds(successEmbed(getUserI18n().get(path, *attributes))).setEphemeral(ephemeral)