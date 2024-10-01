package at.xirado.bean.util

import at.xirado.bean.interaction.command.model.AppCommand
import at.xirado.bean.interaction.command.model.embedService
import at.xirado.bean.interaction.command.model.localizationService
import at.xirado.bean.model.toMessageEmbed
import dev.minn.jda.ktx.interactions.commands.optionType
import dev.minn.jda.ktx.interactions.components.getOption
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.reflect.*
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

inline fun <reified T> createOption(
    name: String,
    description: String,
    autocomplete: Boolean = false,
    builder: OptionData.() -> Unit = {}
): OptionData {
    val type = optionType<T>()
    if (type == OptionType.UNKNOWN)
        throw IllegalArgumentException("Cannot resolve type " + T::class.java.simpleName + " to OptionType!")

    val required = !typeOf<T>().isMarkedNullable

    return OptionData(type, name, description, required, autocomplete).apply(builder)
}

val OptionType.targetClass: KClass<*>
    get() = when (this) {
        OptionType.STRING -> String::class
        OptionType.BOOLEAN -> Boolean::class
        OptionType.INTEGER -> Long::class
        OptionType.NUMBER -> Double::class
        OptionType.MENTIONABLE -> IMentionable::class
        OptionType.USER -> User::class
        OptionType.ATTACHMENT -> Message.Attachment::class
        OptionType.CHANNEL -> GuildChannel::class
        OptionType.ROLE -> Role::class

        else -> throw IllegalStateException("No mapping for OptionType ${this.name}")
    }

fun checkCommandFunctionParameters(function: KFunction<*>, options: Collection<OptionData>) {
    val functionParameters = function.valueParameters

    val eventParameter = functionParameters.firstOrNull()
        ?: throw IllegalStateException("Missing event parameter")

    if (eventParameter.type.classifier != SlashCommandInteractionEvent::class)
        throw IllegalStateException("function needs `SlashCommandInteractionEvent` as first argument")

    val optionCount = options.size
    val expectedArgumentCount = optionCount + 1
    val parameterCount = functionParameters.size

    if (parameterCount != expectedArgumentCount)
        throw IllegalStateException("Function has invalid amount of parameters. (Expected: $expectedArgumentCount, Actual: $parameterCount)")

    options.forEach { optionData ->
        val optionName = optionData.name
        val expectedFunctionName = optionName.snakeCaseToCamelCase()
        val functionParameter = functionParameters.find { it.name == expectedFunctionName }
            ?: throw IllegalStateException("No function parameter found for option $optionName")

        val optionType = optionData.type
        val expectedClass = optionType.targetClass

        val functionParameterClass = functionParameter.type.classifier as KClass<*>

        if (functionParameterClass != expectedClass)
            throw IllegalStateException("Expected function argument for option $optionName of type ${expectedClass.simpleName}, but got ${functionParameterClass.simpleName}")

        if (!optionData.isRequired && !functionParameter.type.isMarkedNullable && !functionParameter.isOptional)
            throw IllegalStateException("Option \"${optionData.name}\" is optional, but function parameter is non-null. Consider using optional parameters")
    }
}

fun getPopulatedCommandParameters(
    instance: Any,
    event: SlashCommandInteractionEvent,
    function: KFunction<*>
): Map<KParameter, Any?> {
    val parameterMap = mutableMapOf<KParameter, Any?>()

    val functionParameters = function.parameters

    val instanceParameter = function.instanceParameter!!
    val eventParameter = functionParameters[1]

    parameterMap[instanceParameter] = instance
    parameterMap[eventParameter] = event

    val optionParameters = functionParameters - instanceParameter - eventParameter

    optionParameters.forEach { parameter ->
        val parameterName = parameter.name!!
        val snakeCased = parameterName.snakeCase()
        val type = parameter.type

        val option = event.getOptionByType(type, snakeCased)

        if (option == null && parameter.isOptional)
            return@forEach

        parameterMap[parameter] = option
    }

    return parameterMap
}

fun SlashCommandInteractionEvent.getOptionByType(type: KType, name: String): Any? {
    return when (type.classifier) {
        String::class -> getOption<String>(name)
        Boolean::class -> getOption<Boolean>(name)
        Int::class, Long::class -> getOption<Int>(name)
        Double::class -> getOption<Double>(name)
        IMentionable::class -> getOption<IMentionable>(name)
        User::class -> getOption<User>(name)
        Member::class -> getOption<Member>(name)
        Message.Attachment::class -> getOption<Message.Attachment>(name)
        GuildChannel::class -> getOption<GuildChannel>(name)
        Role::class -> getOption<Role>(name)
        else -> throw IllegalStateException("Invalid parameter of type ${type.classifier}")
    }
}

context(AppCommand<*>)
fun Interaction.getLocalizedEmbed(name: String, useGuildLocale: Boolean = true, params: Map<String, Any?>): MessageEmbed {
    val locale = if (useGuildLocale) guild?.locale ?: userLocale else userLocale
    return embedService.getLocalizedMessageEmbed(name, locale, params).toMessageEmbed()
}

context(AppCommand<*>)
fun Interaction.getLocalizedEmbed(name: String, useGuildLocale: Boolean = true, vararg params: Pair<String, Any?>): MessageEmbed {
    return getLocalizedEmbed(name, useGuildLocale, params.toMap())
}

context(AppCommand<*>)
fun Interaction.getLocalizedString(key: String, useGuildLocale: Boolean = true, vararg params: Any): String {
    val locale = if (useGuildLocale) guild?.locale ?: userLocale else userLocale
    return localizationService.getString(locale, key, *params)
}