package at.xirado.bean.interaction.command.slash.dev

import at.xirado.bean.data.cache.MemberCacheView
import at.xirado.bean.database.table.Members
import at.xirado.bean.interaction.autoDefer
import at.xirado.bean.interaction.command.model.contexts
import at.xirado.bean.interaction.command.model.slash.Handler
import at.xirado.bean.interaction.command.model.slash.SlashCommand
import at.xirado.bean.interaction.command.model.slash.Subcommand
import at.xirado.bean.interaction.command.model.slash.dsl.subcommand.option
import at.xirado.bean.model.GuildFlag
import at.xirado.bean.model.UserFlag
import at.xirado.jdui.component.MessageComponentCallbackResult
import at.xirado.jdui.component.message.button.Button
import at.xirado.jdui.component.message.messageComponents
import at.xirado.jdui.component.modal.Modal
import at.xirado.jdui.component.modal.ModalCallbackResult
import at.xirado.jdui.component.modal.ModalInput
import at.xirado.jdui.component.modal.provideDelegate
import at.xirado.jdui.message.ChildMessageView
import at.xirado.jdui.message.MessageView
import at.xirado.jdui.message.messageBody
import at.xirado.jdui.message.triggerUpdate
import at.xirado.jdui.replyView
import at.xirado.jdui.sendView
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Single
class JDUITestCommand : SlashCommand("test-jdui", "Tests JDUI") {
    init {
        requiredGuildFlags += GuildFlag.DEV_GUILD
        requiredUserFlags += UserFlag.ADMIN
        contexts(InteractionContextType.BOT_DM, InteractionContextType.GUILD)
        subcommand(Counter(), MultiMenu(), Coroutines(), DM(), Modals(), Koin())
    }

    inner class Counter : Subcommand("counter", "Counter") {
        @Handler
        suspend fun run(event: SlashCommandInteractionEvent) {
            event.replyView(CounterView()).await()
        }
    }

    inner class MultiMenu : Subcommand("multi-menu", "Multi-Menu") {
        @Handler
        suspend fun run(event: SlashCommandInteractionEvent) {
            event.replyView(MultiMenuView()).await()
        }
    }

    inner class Coroutines : Subcommand("coroutines", "Coroutines") {
        init {
            option<Int?>("delay", "How long it should load")
        }

        @Handler
        suspend fun run(event: SlashCommandInteractionEvent, delay: Int = 3000) {
            event.replyView(LoadingMessageView(delay.toLong())).await()
        }
    }

    inner class Modals : Subcommand("modals", "Modals") {
        @Handler
        suspend fun run(event: SlashCommandInteractionEvent) {
            event.replyView(ModalTestView()).await()
        }
    }

    inner class Koin : Subcommand("koin", "Koin") {
        init {
            option<String>("guild", "The guild id")
            option<String>("user", "The user id")
        }

        @Handler
        suspend fun run(event: SlashCommandInteractionEvent, guild: String, user: String) {
            val guildId = guild.toLong()
            val userId = user.toLong()

            event.replyView(KoinTestView(userId, guildId)).await()
        }
    }

    inner class DM : Subcommand("dm", "Coroutines in DM") {
        init {
            option<Int?>("delay", "How long it should load")
        }

        @Handler
        suspend fun run(event: SlashCommandInteractionEvent, delay: Int = 3000) {
            event.autoDefer(ephemeral = true) {
                try {
                    val channel = event.user.openPrivateChannel().await()
                    channel.sendView(LoadingMessageView(delay.toLong())).await()
                    MessageCreate {
                        content = "Sent you a DM!"
                    }
                } catch (e: ErrorResponseException) {
                    MessageCreate {
                        content = "You seem to block DMs!"
                    }
                }
            }
        }
    }
}

class CounterView(var counter: Int = 0) : MessageView() {
    private val increment = Button("inc", ButtonStyle.SECONDARY, "Increment") { _, _ ->
        counter += 1
        MessageComponentCallbackResult.Noop
    }

    override suspend fun createMessage() = messageBody {
        embeds += Embed {
            color = 0xff44aa
            description = "Counter: $counter"
        }
    }

    override suspend fun defineComponents() = messageComponents {
        row {
            +increment
        }
    }
}

class MultiMenuView(var foo: Int = 0, var bar: Int = 0) : MessageView() {
    private val incrementFoo = Button("inc_foo", ButtonStyle.SECONDARY, "Increment Foo") { _, _ ->
        MessageComponentCallbackResult.ChildView(ConfirmationView("Increment Foo?")) {
            if (it == ConfirmationResult.Ok)
                foo += 1
        }
    }

    private val incrementBar = Button("inc_bar", ButtonStyle.SECONDARY, "Increment Bar") { _, _ ->
        MessageComponentCallbackResult.ChildView(ConfirmationView("Increment Bar?")) {
            if (it == ConfirmationResult.Ok)
                bar += 1
        }
    }

    override suspend fun createMessage() = messageBody {
        embeds += Embed {
            color = 0xff55aa
            description = "Foo: $foo\nBar: $bar"
        }
    }

    override suspend fun defineComponents() = messageComponents {
        row {
            +incrementFoo
            +incrementBar
        }
    }
}

class KoinTestView(private val userId: Long, private val guildId: Long) : MessageView(), KoinComponent {
    private val memberCacheView: MemberCacheView by inject()
    private val updateButton = Button("update", ButtonStyle.SECONDARY, "Update") { _, _ ->
        MessageComponentCallbackResult.Noop
    }

    override suspend fun createMessage() = messageBody {
        val start = System.currentTimeMillis()
        val id = CompositeID {
            it[Members.user] = userId
            it[Members.guild] = guildId
        }

        newSuspendedTransaction {
            val member = memberCacheView.getById(id)

            embeds += Embed {
                color = 0xff55aa

                if (member == null) {
                    title = "Guild: $guildId, User: $userId"
                    description = "No such member found :("
                } else {
                    field("Member", member.toString(true), false)
                    field("Experience", member.experience.toString(), false)
                }
            }
            content = "Took ${System.currentTimeMillis() - start}ms!"
        }
    }

    override suspend fun defineComponents() = messageComponents {
        row {
            add(updateButton)
        }
    }
}

class ConfirmationView(val description: String) : ChildMessageView<ConfirmationResult>() {
    private val cancel = Button("cancel", ButtonStyle.SECONDARY, "Cancel") { _, _ ->
        MessageComponentCallbackResult.GoBack(ConfirmationResult.Cancel)
    }

    private val ok = Button("ok", ButtonStyle.SUCCESS, "Ok") { _, _ ->
        MessageComponentCallbackResult.GoBack(ConfirmationResult.Ok)
    }

    override suspend fun createMessage() = messageBody {
        embeds += Embed {
            description = this@ConfirmationView.description
        }
    }

    override suspend fun defineComponents() = messageComponents {
        row {
            +cancel
            +ok
        }
    }
}

enum class ConfirmationResult {
    Ok,
    Cancel
}

class LoadingMessageView(private val delay: Long) : MessageView() {
    private var loading = false
    private var data: String? = null
    private var counter = 0

    private val loadButton = Button("load", ButtonStyle.DANGER, "Load") { _, _ ->
        MessageComponentCallbackResult.ChildView(ConfirmationView("Load this very slow resource?")) {
            if (it == ConfirmationResult.Ok) {
                loading = true
                coroutineScope.launch {
                    delay(delay)
                    withLock {
                        data = "Very interesting data! ${++counter}"
                        loading = false
                    }
                    triggerUpdate()
                }
            }
        }
    }

    override suspend fun createMessage() = messageBody {
        embeds += Embed {
            description = if (loading) {
                "${LOADING_EMOJI.asMention} Loading resource..."
            } else if (data != null) {
                "Data: $data"
            } else {
                "Data not loaded yet :("
            }
        }
    }

    override suspend fun defineComponents() = messageComponents {
        loadButton.disabled = loading
        row {
            +loadButton
        }
    }

    companion object {
        private val LOADING_EMOJI = Emoji.fromCustom("loading", 1333551024371138660, true)
    }
}

class ModalTestView : MessageView() {
    private var dataOne: String? = null
    private var dataTwo: String? = null
    private var modal = TestModal("Modify data")
    private var button = Button("edit", ButtonStyle.SECONDARY, "Edit data") { _, _ ->
        MessageComponentCallbackResult.Modal(modal) {
            dataOne = it.fieldOne.ifEmpty { "N/A" }
            dataTwo = it.fieldTwo.ifEmpty { "N/A" }
            ModalCallbackResult.UpdateMessage
        }
    }

    override suspend fun createMessage() = messageBody {
        embeds += Embed {
            field("Field 1", dataOne ?: "N/A")
            field("Field 2", dataTwo ?: "N/A")
        }
    }

    override suspend fun defineComponents() = messageComponents {
        row {
            +button
        }
    }
}

class TestModal(override var title: String) : Modal<TestModalInput>(TestModalInput::class) {
    val fieldOne = textInput("fieldone", TextInputStyle.SHORT, "Field 1")
    val fieldTwo = textInput("fieldtwo", TextInputStyle.PARAGRAPH, "Field 2")
}

object StaticTestModal : Modal<TestModalInput>(TestModalInput::class) {
    val fieldOne = textInput("fieldone", TextInputStyle.SHORT, "Field 1")
    val fieldTwo = textInput("fieldtwo", TextInputStyle.PARAGRAPH, "Field 2")
    override var title = "Some title"
}

class TestModalInput : ModalInput() {
    val fieldOne by TestModal::fieldOne
    val fieldTwo by TestModal::fieldTwo
}

class StaticTestModalInput : ModalInput() {
    val fieldOne by StaticTestModal::fieldOne
    val fieldTwo by StaticTestModal::fieldTwo
}