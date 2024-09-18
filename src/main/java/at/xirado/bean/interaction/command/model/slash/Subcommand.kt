package at.xirado.bean.interaction.command.model.slash

import at.xirado.bean.interaction.command.AppCommandHandler
import at.xirado.bean.util.checkCommandFunctionParameters
import at.xirado.bean.util.findFunctionWithAnnotation
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import kotlin.reflect.KFunction

abstract class Subcommand(name: String, description: String) {
    val subcommandData = SubcommandData(name, description)
    val function: KFunction<*> = findFunctionWithAnnotation<Handler>()
        ?: throw IllegalStateException("Missing handler function")

    context(AppCommandHandler)
    fun initialize() {
        checkCommandFunctionParameters(function, subcommandData.options)
    }
}