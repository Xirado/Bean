package at.xirado.bean.interaction

import at.xirado.bean.APPLICATION
import at.xirado.bean.util.replyError
import at.xirado.bean.util.replyWarningLocalized
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

enum class CommandFlag(val filter: suspend (GenericCommandInteractionEvent) -> Boolean) {
    DEVELOPER_ONLY({ event ->
        val user = event.user
        val devUsers = APPLICATION.config.devUsers

        (user.idLong in devUsers).ifFalse { event.replyError("This maze isn't meant for you!", ephemeral = true).queue() }
    }),

    USER_MUST_JOIN_VC({ event ->
        val member = event.member!!
        val voiceState = member.voiceState!!

        (voiceState.channel != null).ifFalse {
            event.replyWarningLocalized("general.must_be_listening_in_vc", ephemeral = true).queue()
        }
    }),

    MUST_JOIN_BOT_VC({ event ->
        val member = event.member!!
        val guild = event.guild!!
        val userVoiceState = member.voiceState!!
        val botVoiceState = guild.selfMember.voiceState!!
        (botVoiceState.channel == null || userVoiceState.channel == botVoiceState.channel)
            .ifFalse {
                event.replyWarningLocalized(
                    "general.must_be_listening_in_bot_vc",
                    "channel" to botVoiceState.channel!!.asMention,
                    ephemeral = true).queue()
            }
    })
}

inline fun Boolean.ifFalse(block: () -> Unit): Boolean {
    if (!this)
        block.invoke()
    return this
}

