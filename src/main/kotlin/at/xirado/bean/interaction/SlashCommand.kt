package at.xirado.bean.interaction

import dev.minn.jda.ktx.interactions.Subcommand
import dev.minn.jda.ktx.interactions.optionType
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.util.*

abstract class SlashCommand(name: String, description: String) : GenericCommand {
    override val commandData = Commands.slash(name.lowercase(Locale.getDefault()), description)
    override val requiredUserPermissions: EnumSet<Permission> = EnumSet.noneOf(Permission::class.java)
    override val requiredBotPermissions: EnumSet<Permission> = EnumSet.noneOf(Permission::class.java)
    override val enabledGuilds = HashSet<Long>()
    override val commandFlags: EnumSet<CommandFlag> = EnumSet.noneOf(CommandFlag::class.java)
    override var disabled: Boolean = false

    override val type: Command.Type
        get() = Command.Type.SLASH

    fun devCommand(requiresAdmin: Boolean = true) {
        enabledGuilds.addAll(application.config.devGuilds)
        commandFlags.add(CommandFlag.DEVELOPER_ONLY)
        if (requiresAdmin)
            requiredUserPermissions.add(Permission.ADMINISTRATOR)
    }

    inline fun <reified T> option(name: String, description: String, required: Boolean = false, autocomplete: Boolean = false, builder: OptionData.() -> Unit = {}) {
        val type = optionType<T>()
        if (type == OptionType.UNKNOWN)
            throw IllegalArgumentException("Cannot resolve type " + T::class.java.simpleName + " to OptionType!")

        val nameLocalizations = application.localizationManager.getDiscordLocalizations("commands.slash.${commandData.name}.options.$name.name")
        val descriptionLocalizations = application.localizationManager.getDiscordLocalizations("commands.slash.${commandData.name}.options.$name.description")

        commandData.addOptions(
            OptionData(type, name, description)
                .setRequired(required)
                .setAutoComplete(autocomplete)
                .setNameLocalizations(nameLocalizations)
                .setDescriptionLocalizations(descriptionLocalizations)
                .apply(builder)
        )
    }

    inline fun subCommand(name: String, description: String, builder: SubcommandData.() -> Unit = {}) = commandData.addSubcommands(
        Subcommand(name, description, builder)
    )

    open suspend fun baseCommand(event: SlashCommandInteractionEvent) {}

}