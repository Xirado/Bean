package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.CommandPermission

class SecretCommand(override val application: Application) : SlashCommand("secret", "testing perms v2") {

    init {
        commandData.defaultPermissions = CommandPermission.DISABLED
        devCommand()
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        event.reply("Nice").queue()
    }
}