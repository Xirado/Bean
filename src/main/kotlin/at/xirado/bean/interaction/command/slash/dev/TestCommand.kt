package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.interaction.EventCondition
import at.xirado.bean.interaction.EventListener
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.util.postHaste
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class TestCommand(override val application: Application) : SlashCommand("test", "Tests some stuff") {
    init {
        devCommand()
    }

    override suspend fun baseCommand(event: SlashCommandInteractionEvent) {
        event.reply(postHaste("Bruh")).queue()
    }

    @EventListener(MessageReceivedEvent::class)
    @EventCondition("message.contentRaw == `check` && !author.isBot")
    suspend fun onSecond(event: MessageReceivedEvent) {
        event.message.addReaction(Emoji.fromUnicode("✅")).queue()
    }

    @EventListener(MessageReceivedEvent::class)
    @EventCondition("message.contentRaw == `x` && !author.isBot")
    suspend fun onFirst(event: MessageReceivedEvent) {
        event.message.addReaction(Emoji.fromUnicode("❌")).queue()
    }
}