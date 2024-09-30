package at.xirado.bean.interaction.command.model.slash

import at.xirado.bean.interaction.command.AppCommandHandler
import at.xirado.bean.util.checkCommandFunctionParameters
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData

abstract class SubcommandGroup(name: String, description: String) {
    val groupData = SubcommandGroupData(name, description)
    private val subcommands = mutableMapOf<String, Subcommand>()

    context(AppCommandHandler)
    fun initialize() {
        subcommands.values.forEach {
            checkCommandFunctionParameters(it.handler.second, it.subcommandData.options)
        }
    }

    fun subcommand(vararg subcommands: Subcommand) {
        subcommand(subcommands.toList())
    }

    fun subcommand(subcommands: Collection<Subcommand>) {
        subcommands.forEach {
            groupData.addSubcommands(it.subcommandData)
            this.subcommands[it.subcommandData.name] = it
        }
    }

    fun getSubcommand(name: String): Subcommand = subcommands[name]
        ?: throw IllegalStateException("Subcommand group ${groupData.name} does not have subcommand $name")
}