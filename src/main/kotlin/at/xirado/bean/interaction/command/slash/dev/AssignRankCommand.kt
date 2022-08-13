package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.Application
import at.xirado.bean.interaction.AutoComplete
import at.xirado.bean.interaction.SlashCommand
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class AssignRankCommand(override val application: Application) : SlashCommand("assign-rank", "Assigns a rank to a user.") {
    init {
        disabled = true
        devCommand()
        option<User>(name = "user", description = "The user to assign the rank to", required = true)
        option<String>(name = "rank", description = "The new rank to assign", required = true, autocomplete = true)
    }

    override suspend fun baseCommand(event: SlashCommandInteractionEvent) {

    }

    @AutoComplete(option = "rank")
    suspend fun onAutocomplete(event: CommandAutoCompleteInteractionEvent) {

    }
}