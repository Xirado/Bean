package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.interaction.command.AppCommandHandler
import at.xirado.bean.interaction.command.model.slash.Handler
import at.xirado.bean.interaction.command.model.slash.SlashCommand
import at.xirado.bean.interaction.command.model.slash.Subcommand
import at.xirado.bean.interaction.command.model.slash.dsl.subcommand.option
import at.xirado.bean.jda.JDAService
import at.xirado.bean.model.GuildFlag
import at.xirado.bean.model.UserFlag
import dev.minn.jda.ktx.messages.MessageCreate
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import org.koin.core.annotation.Single
import org.koin.core.component.inject

private val log = KotlinLogging.logger { }

@Single
class ForceUpdateCommandsCommand : SlashCommand("force-command-update", "Force command updates") {
    init {
        requiredGuildFlags += GuildFlag.DEV_GUILD
        requiredUserFlags += UserFlag.ADMIN
        subcommand(Global(), Guild())
    }

    private val appCommandHandler by inject<AppCommandHandler>()
    private val jdaService by inject<JDAService>()

    inner class Global : Subcommand("global", "Force update global commands") {
        @Handler(ephemeral = true)
        suspend fun run(event: SlashCommandInteractionEvent): MessageCreateData {
            return try {
                appCommandHandler.updateGlobalCommands(event.jda, force = true)
                MessageCreate(content = "Successfully updated global commands")
            } catch (e: Exception) {
                log.error(e) { "Failed to update global commands" }
                MessageCreate(content = "Failed to update global commands")
            }
        }
    }

    inner class Guild : Subcommand("guild", "Force update guild commands") {
        init {
            option<String>("guild_id", "id of the guild to update commands on")
        }

        @Handler(ephemeral = true)
        suspend fun run(event: SlashCommandInteractionEvent, guildId: String): MessageCreateData {
            val guildIdLong = guildId.toLongOrNull()
                ?: return MessageCreate(content = "Not a valid guild id!")

            val guild = jdaService.shardManager.getGuildById(guildIdLong)
                ?: return MessageCreate("No guild found with id $guildIdLong!")

            return try {
                appCommandHandler.updateGuildCommands(guild, force = true)
                MessageCreate(content = "Successfully updated commands of guild ${guild.name} (${guild.id})")
            } catch (e: Exception) {
                log.error(e) { "Failed to update commands of guild ${guild.name} (${guild.id})" }
                MessageCreate(content = "Failed to update commands of guild ${guild.name} (${guild.id})")
            }
        }
    }
}