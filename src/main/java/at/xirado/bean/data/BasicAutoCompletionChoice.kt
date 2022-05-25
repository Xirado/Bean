package at.xirado.bean.data

import net.dv8tion.jda.api.interactions.commands.Command

class BasicAutoCompletionChoice(
    override val name: String,
    override val value: String
) : IAutoCompleteChoice {
    override fun toChoice() = Command.Choice(name, value)
}
