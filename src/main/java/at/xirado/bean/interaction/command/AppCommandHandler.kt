package at.xirado.bean.interaction.command

import at.xirado.bean.Config
import at.xirado.bean.data.DBContext
import at.xirado.bean.data.cache.GuildCacheView
import at.xirado.bean.data.cache.UserCacheView
import at.xirado.bean.database.table.GuildCommands
import at.xirado.bean.database.table.HashedCommands
import at.xirado.bean.interaction.command.model.*
import at.xirado.bean.interaction.command.model.message.MessageContextCommand
import at.xirado.bean.interaction.command.model.slash.SlashCommand
import at.xirado.bean.jda.JDAEventListener
import dev.minn.jda.ktx.coroutines.await
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import java.util.concurrent.atomic.AtomicBoolean

private val log = KotlinLogging.logger { }

@Single(createdAtStart = true)
class AppCommandHandler(
    private val db: DBContext,
    private val guildCacheView: GuildCacheView,
    private val userCacheView: UserCacheView,
    private val config: Config,
) : JDAEventListener {
    private val defaultFeatures = config.defaultGuildFeaturesParsed
    private val ready = AtomicBoolean(false)
    private val commands: Map<String, AppCommand<*>> = loadCommands()
    private val hashes: Map<String, String> = computeCommandHashes()

    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is GuildReadyEvent -> onReady(event)
            is GuildJoinEvent -> onGuildJoin(event)
            is GenericCommandInteractionEvent -> onCommand(event)
        }
    }

    private suspend fun onReady(event: GuildReadyEvent) {
        if (ready.compareAndSet(false, true)) {
            updateGlobalCommands(event.jda)
        }
       updateGuildCommands(event.guild)
    }

    private suspend fun onGuildJoin(event: GuildJoinEvent) {
        updateGuildCommands(event.guild)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun onCommand(event: GenericCommandInteractionEvent) {
        val identifier = event.getIdentifier()
        val command = commands[identifier]
            ?: return log.warn { "Got interaction with unknown identifier $identifier" }

        if (!command.isGlobal()) {
            val jdaGuild = event.guild
                ?: return log.warn { "Execution of non-global command $identifier outside guild" }
            val guild = guildCacheView.save(jdaGuild)

            if (!command.isGuildCommandFor(guild, defaultFeatures))
                return
        }

        val requiredUserFlags = command.requiredUserFlags
        if (requiredUserFlags.isNotEmpty()) {
            val jdaUser = event.user
            val user = userCacheView.save(jdaUser)

            if (!command.canExecute(user)) {
                return event.reply("You cannot use this command!")
                    .setEphemeral(true)
                    .queue()
            }
        }

        when (event) {
            is SlashCommandInteractionEvent -> {
                command as AppCommand<SlashCommandInteractionEvent>
                command.execute(event)
            }
            is MessageContextInteractionEvent -> {
                command as AppCommand<MessageContextInteractionEvent>
                command.execute(event)
            }
        }
    }

    private fun loadCommands(): Map<String, AppCommand<*>> {
        return getKoin().let {
            it.getAll<SlashCommand>() + it.getAll<MessageContextCommand>()
        }.associateBy { it.identifier }
    }

    private fun computeCommandHashes(): Map<String, String> {
        return commands.values.associate {
            it.identifier to it.computeHash()
        }
    }

    suspend fun updateGlobalCommands(jda: JDA, force: Boolean = false) {
        val globalCommands = commands.values.filter(AppCommand<*>::isGlobal)

        if (!force) {
            val shouldUpdate = shouldUpdateGlobalCommands(globalCommands)

            if (!shouldUpdate)
                return log.info { "Global commands are up to date. Not updating" }

            log.info { "Change in global commands detected. Updating" }
        } else {
            log.info { "Force updating global commands" }
        }

        jda.updateCommands()
            .addCommands(globalCommands.map { it.commandData })
            .await()

        updateGlobalCommandHashes(globalCommands)
    }

    private suspend fun shouldUpdateGlobalCommands(commands: List<AppCommand<*>>) = db.doTransaction {
        val existing = commands.associate { it.identifier to hashes[it.identifier]!! }

        val stored = HashedCommands.selectAll().associate {
            it[HashedCommands.id].value to it[HashedCommands.hash]
        }

        existing != stored
    }

    private suspend fun updateGlobalCommandHashes(commands: List<AppCommand<*>>) = db.doTransaction {
        HashedCommands.deleteAll()
        HashedCommands.batchInsert(commands) { command ->
            this[HashedCommands.id] = command.identifier
            this[HashedCommands.hash] = hashes[command.identifier]!!
        }
    }

    suspend fun updateGuildCommands(jdaGuild: Guild, force: Boolean = false) {
        val guild = guildCacheView.save(jdaGuild)
        val guildId = jdaGuild.idLong
        val guildCommands = commands.values.filter { it.isGuildCommandFor(guild, defaultFeatures) }

        if (!force) {
            val shouldUpdate = shouldUpdateGuildCommands(guildId, guildCommands)
            if (!shouldUpdate)
                return log.debug { "Not updating guild commands for ${guild.name} (${guild.id}), up to date." }

            log.debug { "Updating guild commands for ${guild.name} (${guild.id})" }
        } else {
            log.info { "Force updating guild commands for ${guild.name} (${guild.id})" }
        }

        jdaGuild.updateCommands()
            .addCommands(guildCommands.map { it.commandData })
            .await()

        updateGuildCommandHashes(guildId, guildCommands)
    }

    private suspend fun shouldUpdateGuildCommands(guildId: Long, commands: List<AppCommand<*>>) = db.doTransaction {
        val existing = commands.associate { it.identifier to hashes[it.identifier]!! }
        val stored = GuildCommands.selectAll()
            .where { GuildCommands.guild eq guildId }
            .associate { it[GuildCommands.identifier] to it[GuildCommands.commandHash] }

        existing != stored
    }

    private suspend fun updateGuildCommandHashes(guildId: Long, commands: List<AppCommand<*>>) = db.doTransaction {
        GuildCommands.deleteWhere { guild eq guildId }
        GuildCommands.batchInsert(commands) { cmd ->
            val identifier = cmd.identifier
            val hash = hashes[cmd.identifier]!!

            this[GuildCommands.guild] = guildId
            this[GuildCommands.identifier] = identifier
            this[GuildCommands.commandHash] = hash
        }
    }
}