package at.xirado.bean.interaction

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.internal.utils.Checks
import java.util.*

abstract class SlashCommand(name: String, description: String) : GenericCommand {

    override val commandData = Commands.slash(name, description)
    override val requiredUserPermissions = EnumSet.noneOf(Permission::class.java)
    override val requiredBotPermissions = EnumSet.noneOf(Permission::class.java)
    override val enabledGuilds = HashSet<Long>()
    override val commandFlags = EnumSet.noneOf(CommandFlag::class.java)

    override val type: Command.Type
        get() = Command.Type.SLASH

    override val global: Boolean
        get() = enabledGuilds.isEmpty()

    fun option(
        type: OptionType,
        name: String,
        description: String,
        required: Boolean = false,
        autoComplete: Boolean = false,
        vararg choices: Command.Choice
    ) {
        val optionData = OptionData(type, name, description, required, autoComplete)
        choices.forEach { optionData.addChoices(it) }
        commandData.addOptions(optionData)
    }

    override fun addRequiredUserPermissions(permission: Permission, vararg permissions: Permission) {
        Checks.notNull(permission, "Permission")
        Checks.noneNull(permissions, "Permission")

        requiredUserPermissions.add(permission)
        requiredUserPermissions.addAll(permissions.asList())
    }

    override fun addRequiredBotPermissions(permission: Permission, vararg permissions: Permission) {
        Checks.notNull(permission, "Permission")
        Checks.noneNull(permissions, "Permission")

        requiredBotPermissions.add(permission)
        requiredBotPermissions.addAll(permissions.asList())
    }

    override fun setEnabledGuilds(vararg guildIds: Long) {
        enabledGuilds.clear()
        enabledGuilds.addAll(guildIds.toList())
    }

    override fun addCommandFlags(vararg commandFlags: CommandFlag) {
        Checks.noneNull(commandFlags, "CommandFlags")

        this.commandFlags.addAll(commandFlags.toList())
    }

    override fun hasCommandFlag(commandFlag: CommandFlag): Boolean {
        return this.commandFlags.contains(commandFlag)
    }

    abstract suspend fun execute(event: SlashCommandInteractionEvent)

    open suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}


}