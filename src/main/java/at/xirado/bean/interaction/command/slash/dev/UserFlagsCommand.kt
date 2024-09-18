package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.data.cache.UserCacheView
import at.xirado.bean.interaction.command.model.slash.Handler
import at.xirado.bean.interaction.command.model.slash.SlashCommand
import at.xirado.bean.interaction.command.model.slash.Subcommand
import at.xirado.bean.interaction.command.model.slash.dsl.subcommand.option
import at.xirado.bean.model.GuildFlag
import at.xirado.bean.model.UserFlag
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.koin.core.annotation.Single

@Single
class UserFlagsCommand(
    private val userCacheView: UserCacheView,
) : SlashCommand("flags", "Read or modify user flags") {
    init {
        requiredGuildFlags += GuildFlag.DEV_GUILD
        requiredUserFlags += UserFlag.ADMIN
        subcommand(AddUserFlags(), ListUserFlags(), RemoveUserFlags())
    }

    inner class ListUserFlags : Subcommand("list", "List a users' flags") {
        init {
            option<User>("user", "The user")
        }

        @Handler
        suspend fun run(event: SlashCommandInteractionEvent, user: User) {

        }
    }

    inner class AddUserFlags : Subcommand("add", "Add flag to a user") {
        init {
            option<String>("flag", "The flag")
            option<User>("user", "The user")
        }

        @Handler
        suspend fun run(event: SlashCommandInteractionEvent, user: User, flag: String) {

        }
    }

    inner class RemoveUserFlags : Subcommand("remove", "Remove flag from user") {
        init {
            option<User>("user", "The user")
            option<String>("flag", "The flag")
        }

        @Handler
        suspend fun run(event: SlashCommandInteractionEvent, user: User, flag: String) {

        }
    }
}