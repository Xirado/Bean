package at.xirado.bean.data

import net.dv8tion.jda.api.interactions.commands.Command

interface IAutoCompleteChoice {
    val name: String
    val value: String

    fun toChoice(): Command.Choice
}