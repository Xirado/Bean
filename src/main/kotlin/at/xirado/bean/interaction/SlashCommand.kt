package at.xirado.bean.interaction

import dev.minn.jda.ktx.interactions.Subcommand
import dev.minn.jda.ktx.interactions.optionType
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.util.*

abstract class SlashCommand(name: String, description: String) : GenericCommand {
    override val commandData = Commands.slash(name, description)
    override val requiredUserPermissions: EnumSet<Permission> = EnumSet.noneOf(Permission::class.java)
    override val requiredBotPermissions: EnumSet<Permission> = EnumSet.noneOf(Permission::class.java)
    override val enabledGuilds = HashSet<Long>()
    override val commandFlags: EnumSet<CommandFlag> = EnumSet.noneOf(CommandFlag::class.java)

    override val type: Command.Type
        get() = Command.Type.SLASH

    override val global: Boolean
        get() = enabledGuilds.isEmpty()

    fun devCommand() {
        enabledGuilds.addAll(application.config.devGuilds)
        commandFlags.add(CommandFlag.DEVELOPER_ONLY)
    }

    inline fun <reified T> option(name: String, description: String, required: Boolean = false, autocomplete: Boolean = false, builder: OptionData.() -> Unit = {}) {
        val type = optionType<T>()
        if (type == OptionType.UNKNOWN)
            throw IllegalArgumentException("Cannot resolve type " + T::class.java.simpleName + " to OptionType!")

        commandData.addOptions(OptionData(type, name, description).setRequired(required).setAutoComplete(autocomplete).apply(builder))
    }

    inline fun subcommand(name: String, description: String, builder: SubcommandData.() -> Unit = {}) = commandData.addSubcommands(
        Subcommand(name, description, builder)
    )

    abstract suspend fun execute(event: SlashCommandInteractionEvent)

    open suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}


}