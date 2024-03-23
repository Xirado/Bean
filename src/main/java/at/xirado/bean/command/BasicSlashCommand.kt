package at.xirado.bean.command

import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData

@Serializable
data class BasicSlashCommand(
    val name: String,
    val description: String,
    val options: List<BasicCommandOption>,
    val subcommands: List<BasicSubCommand>,
    val subcommandGroups: List<BasicSubCommandGroup>
)

@Serializable
data class BasicSubCommand(
    val name: String,
    val description: String,
    val options: List<BasicCommandOption>,
)

@Serializable
data class BasicSubCommandGroup(
    val name: String,
    val description: String,
    val subcommands: List<BasicSubCommand>,
)

@Serializable
data class BasicCommandOption(
    val name: String,
    val type: Int,
    val description: String,
    val required: Boolean,
)

fun SlashCommandData.toBasicCommand() = BasicSlashCommand(
    name, description,
    options.map(OptionData::toBasicOption),
    subcommands.map(SubcommandData::toBasicSubcommand),
    subcommandGroups.map(SubcommandGroupData::toBasicSubcommandGroup),
)

fun OptionData.toBasicOption() = BasicCommandOption(
    name, type.key, description, isRequired
)

fun SubcommandData.toBasicSubcommand() = BasicSubCommand(
    name, description, options.map(OptionData::toBasicOption)
)

fun SubcommandGroupData.toBasicSubcommandGroup() = BasicSubCommandGroup(
    name, description, subcommands.map(SubcommandData::toBasicSubcommand)
)



