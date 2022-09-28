package at.xirado.bean.interaction.slash

import at.xirado.bean.interaction.DiscordInteractionCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.util.*
import kotlin.reflect.KFunction

abstract class SlashCommand(name: String, description: String = "<No description set>") : DiscordInteractionCommand {
    override val commandData = Commands.slash(name.lowercase(Locale.getDefault()), description)
    open var description: String = ""

    override val requiredUserPermissions: EnumSet<Permission> = EnumSet.noneOf(Permission::class.java)
    override val requiredBotPermissions: EnumSet<Permission> = EnumSet.noneOf(Permission::class.java)
    override val enabledGuilds = mutableSetOf<Long>()
    override var disabled: Boolean = false
    override val type = Command.Type.SLASH

    var baseCommandFunction: KFunction<*>? = null

    fun devCommand(requiresAdmin: Boolean = true) {
        enabledGuilds.addAll(app.config.devGuilds)
        if (requiresAdmin)
            requiredUserPermissions.add(Permission.ADMINISTRATOR)
    }

    fun options(block: SlashCommandOptionBuilder.() -> Unit) {
        SlashCommandOptionBuilder(commandData, app).apply(block)
    }

    fun subCommands(block: SlashCommandSubCommandBuilder.() -> Unit) {
        SlashCommandSubCommandBuilder(commandData, app).apply(block)
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BaseCommand

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubCommand(val name: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoComplete(val option: String)