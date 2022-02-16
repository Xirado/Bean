package at.xirado.bean.command.slashcommands

import at.xirado.bean.command.SlashCommand
import at.xirado.bean.command.SlashCommandContext
import at.xirado.bean.translation.Attribute
import at.xirado.bean.translation.I18n
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.data.DataObject

class TestCommandKt() : SlashCommand() {

    init {
        setCommandData(Commands.slash("test", "this is a test"))
        isGlobal = false
        setEnabledGuilds(815597207617142814)
    }

    override fun executeCommand(event: SlashCommandInteractionEvent, ctx: SlashCommandContext) {
        val dataObject = DataObject.empty()
                .put("commands", DataObject.empty()
                        .put("ban", DataObject.empty()
                                .put("reason", "{name} was banned for {reason}!")
                        )
                )

        val i18n = I18n("test", dataObject)

        val userTag = event.user!!.asTag
        val reason = "being sus"

        event.reply(i18n.getString("commands.ban.reason", Attribute("name", userTag), Attribute("reason", reason))).queue()
    }
}
