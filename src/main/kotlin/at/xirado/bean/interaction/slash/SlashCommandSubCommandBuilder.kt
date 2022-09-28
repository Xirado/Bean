package at.xirado.bean.interaction.slash

import at.xirado.bean.Application
import dev.minn.jda.ktx.interactions.commands.Subcommand
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class SlashCommandSubCommandBuilder(val commandData: SlashCommandData, val application: Application) {
    inline fun subCommand(name: String, description: String, builder: SubcommandData.() -> Unit = {}) {
        commandData.addSubcommands(Subcommand(name, description, builder))
    }
}