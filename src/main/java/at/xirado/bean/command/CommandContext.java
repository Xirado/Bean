package at.xirado.bean.command;

import at.xirado.bean.Bean;
import at.xirado.bean.data.database.entity.DiscordGuild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.function.Consumer;

public class CommandContext {
    public static final String LOADING_EMOTE = "<a:loading:779763323821359104>";
    public static final String WARNING_EMOTE = "⚠";
    public static final String ERROR_EMOTE = "❌";
    public static final String SUCCESS_EMOTE = "✅";

    private final MessageReceivedEvent event;
    private final Command command;
    private final Member member;
    private final CommandArgument commandArgument;

    public CommandContext(MessageReceivedEvent event, CommandArgument commandArgument, Command command, Member member) {
        this.event = event;
        this.commandArgument = commandArgument;
        this.command = command;
        this.member = member;
    }

    public DiscordGuild getGuildData() {
        return Bean.getInstance().getRepository()
                .getGuildRepository()
                .getGuildDataBlocking(event.getGuild().getIdLong());
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    public CommandArgument getArguments() {
        return commandArgument;
    }

    public Command getCommand() {
        return command;
    }

    public Member getMember() {
        return member;
    }

    public void reply(Message message, Consumer<Message> success, Consumer<Throwable> failure) {
        event.getChannel().sendMessage(MessageCreateData.fromMessage(message)).queue(success, failure);
    }

    public void reply(Message message, Consumer<Message> success) {
        event.getChannel().sendMessage(MessageCreateData.fromMessage(message)).queue(success);
    }

    public void reply(Message message) {
        event.getChannel().sendMessage(MessageCreateData.fromMessage(message)).queue();
    }

    public void reply(String message, Consumer<Message> success, Consumer<Throwable> failure) {
        event.getChannel().sendMessage(message).queue(success, failure);
    }

    public void reply(String message, Consumer<Message> success) {
        event.getChannel().sendMessage(message).queue(success);
    }

    public void reply(String message) {
        event.getChannel().sendMessage(message).queue();
    }

    public void reply(MessageEmbed embed, Consumer<Message> success, Consumer<Throwable> failure) {
        event.getChannel().sendMessageEmbeds(embed).queue(success, failure);
    }

    public void reply(MessageEmbed embed, Consumer<Message> success) {
        event.getChannel().sendMessageEmbeds(embed).queue(success);
    }

    public void reply(MessageEmbed embed) {
        event.getChannel().sendMessageEmbeds(embed).queue();
    }
}
