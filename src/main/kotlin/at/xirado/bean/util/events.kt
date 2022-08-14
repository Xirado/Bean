package at.xirado.bean.util

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

fun GenericCommandInteractionEvent.replyError(message: String, ephemeral: Boolean = false) = replyEmbeds(errorEmbed(message))
    .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.sendErrorMessage(message: String, ephemeral: Boolean = false) = hook.sendMessageEmbeds(errorEmbed(message))
    .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.replyErrorLocalized(path: String, vararg attributes: Pair<String, Any>, ephemeral: Boolean = false)
    = replyError(getUserI18n().getValue(path, *attributes), ephemeral = ephemeral)

fun GenericCommandInteractionEvent.sendErrorLocalized(path: String, vararg attributes: Pair<String, Any>, ephemeral: Boolean = false) =
    hook.sendMessageEmbeds(errorEmbed(getUserI18n().getValue(path, *attributes))).setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.replySuccess(message: String, ephemeral: Boolean = false) = replyEmbeds(successEmbed(message))
    .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.sendSuccessMessage(message: String, ephemeral: Boolean = false) = hook.sendMessageEmbeds(successEmbed(message))
    .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.replySuccessLocalized(path: String, vararg attributes: Pair<String, Any>, ephemeral: Boolean = false)
        = replySuccess(getUserI18n().getValue(path, *attributes), ephemeral = ephemeral)

fun GenericCommandInteractionEvent.sendSuccessLocalized(path: String, vararg attributes: Pair<String, Any>, ephemeral: Boolean = false) =
    hook.sendMessageEmbeds(successEmbed(getUserI18n().getValue(path, *attributes))).setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.replyDefault(message: String, ephemeral: Boolean = false) = replyEmbeds(defaultEmbed(message))
    .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.sendDefaultMessage(message: String, ephemeral: Boolean = false) =
    hook.sendMessageEmbeds(defaultEmbed(message))
        .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.replyDefaultLocalized(path: String, vararg attributes: Pair<String, Any>, ephemeral: Boolean = false)
        = replyDefault(getUserI18n().getValue(path, *attributes), ephemeral = ephemeral)

fun GenericCommandInteractionEvent.sendDefaultLocalized(path: String, vararg attributes: Pair<String, Any>, ephemeral: Boolean = false) =
    hook.sendMessageEmbeds(defaultEmbed(getUserI18n().getValue(path, *attributes))).setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.replyWarning(message: String, ephemeral: Boolean = false) = replyEmbeds(warningEmbed(message))
    .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.sendWarningMessage(message: String, ephemeral: Boolean = false) =
    hook.sendMessageEmbeds(warningEmbed(message))
        .setEphemeral(ephemeral)

fun GenericCommandInteractionEvent.replyWarningLocalized(path: String, vararg attributes: Pair<String, Any>, ephemeral: Boolean = false)
        = replyWarning(getUserI18n().getValue(path, *attributes), ephemeral = ephemeral)

fun GenericCommandInteractionEvent.sendWarningLocalized(path: String, vararg attributes: Pair<String, Any>, ephemeral: Boolean = false) =
    hook.sendMessageEmbeds(warningEmbed(getUserI18n().getValue(path, *attributes))).setEphemeral(ephemeral)