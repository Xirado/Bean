package at.xirado.bean.command;

import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.data.LinkedDataObject;
import at.xirado.bean.translation.LocalizationManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public class CommandContext
{
    public static final String LOADING_EMOTE = "<a:loading:779763323821359104>";
    public static final String WARNING_EMOTE = "⚠";
    public static final String ERROR_EMOTE = "❌";
    public static final String SUCCESS_EMOTE = "✅";

    private final MessageReceivedEvent event;
    private final Command command;
    private final Member member;
    private final CommandArgument commandArgument;


    public CommandContext(MessageReceivedEvent event, CommandArgument commandArgument, Command command, Member member)
    {
        this.event = event;
        this.commandArgument = commandArgument;
        this.command = command;
        this.member = member;
    }

    public GuildData getGuildData()
    {
        return GuildManager.getGuildData(event.getGuild());
    }

    public MessageReceivedEvent getEvent()
    {
        return event;
    }

    public CommandArgument getArguments()
    {
        return commandArgument;
    }

    public Command getCommand()
    {
        return command;
    }

    public Member getMember()
    {
        return member;
    }

    public void replyError(String message)
    {
        this.event.getChannel().sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.red)
                        .setDescription(ERROR_EMOTE + " " + message)
                        .build()
        ).queue();
    }

    public void deleteInvokeMessage()
    {
        this.event.getMessage().delete().queue(s ->
        {
        }, e ->
        {
        });
    }

    public void replyInLogChannel(String message)
    {
        TextChannel logChannel = getGuildData().getLogChannel();
        if (logChannel != null) logChannel.sendMessage(message).queue();
    }


    public TextChannel getLogChannel()
    {
        return getGuildData().getLogChannel();
    }

    public boolean hasLogChannel()
    {
        return getGuildData().getLogChannel() != null;
    }

    public void replyInLogChannel(Message message)
    {
        TextChannel logChannel = getGuildData().getLogChannel();
        if (logChannel != null) logChannel.sendMessage(message).queue();
    }

    public void replyInLogChannel(MessageEmbed message)
    {
        TextChannel logChannel = getGuildData().getLogChannel();
        if (logChannel != null) logChannel.sendMessageEmbeds(message).queue();
    }

    public void replyErrorUsage()
    {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.red)
                .setTitle(LocalizationManager.ofGuild(event.getGuild()).get("general.invalid_arguments", String.class))
                .setTimestamp(Instant.now());
        String usage = getGuildData().getPrefix() + getCommand().getUsage();
        List<String> aliases = this.getCommand().getAliases();
        StringBuilder sb = new StringBuilder();
        String aliasesstring = null;
        if (aliases.size() > 0)
        {
            for (String alias : aliases)
            {
                sb.append(alias).append(", ");
            }
            aliasesstring = sb.toString().trim();
        }
        String description = "`" + usage + "`\n" + this.getCommand().getDescription();
        if (aliases.size() > 0 && aliasesstring != null)
        {
            description += "\n" + LocalizationManager.ofGuild(event.getGuild()).get("general.aliases", String.class) + ": `" + aliasesstring + "`";
        }
        builder.setDescription(description);
        event.getChannel().sendMessageEmbeds(builder.build()).queue();

    }

    public void replyWarning(String message)
    {
        this.event.getChannel().sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.yellow)
                        .setTimestamp(Instant.now())
                        .setDescription(WARNING_EMOTE + " " + message)
                        .build()
        ).queue();
    }

    public void replySuccess(String message)
    {
        this.event.getChannel().sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.green)
                        .setDescription(SUCCESS_EMOTE + " " + message)
                        .build()
        ).queue();
    }

    public void reply(Message message, Consumer<Message> success, Consumer<Throwable> failure)
    {
        event.getChannel().sendMessage(message).queue(success, failure);
    }

    public void reply(Message message, Consumer<Message> success)
    {
        event.getChannel().sendMessage(message).queue(success);
    }

    public void reply(Message message)
    {
        event.getChannel().sendMessage(message).queue();
    }

    public void reply(String message, Consumer<Message> success, Consumer<Throwable> failure)
    {
        event.getChannel().sendMessage(message).queue(success, failure);
    }

    public void reply(String message, Consumer<Message> success)
    {
        event.getChannel().sendMessage(message).queue(success);
    }

    public void reply(String message)
    {
        event.getChannel().sendMessage(message).queue();
    }

    public void reply(MessageEmbed embed, Consumer<Message> success, Consumer<Throwable> failure)
    {
        event.getChannel().sendMessageEmbeds(embed).queue(success, failure);
    }

    public void reply(MessageEmbed embed, Consumer<Message> success)
    {
        event.getChannel().sendMessageEmbeds(embed).queue(success);
    }

    public void reply(MessageEmbed embed)
    {
        event.getChannel().sendMessageEmbeds(embed).queue();
    }

    public void replyInDM(Message message, Consumer<Message> success, Consumer<Throwable> failure)
    {
        User user = this.event.getAuthor();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(message).queue(success, failure);
                }
        );
    }

    public void replyInDM(Message message, Consumer<Message> success)
    {
        User user = this.event.getAuthor();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(message).queue(success, new ErrorHandler()
                            .ignore(EnumSet.allOf(ErrorResponse.class)));
                }
        );
    }

    public void replyInDM(Message message)
    {
        User user = this.event.getAuthor();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(message).queue(null, new ErrorHandler()
                            .ignore(EnumSet.allOf(ErrorResponse.class)));
                }
        );
    }

    public void replyInDM(MessageEmbed embed, Consumer<Message> success, Consumer<Throwable> failure)
    {
        User user = this.event.getAuthor();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessageEmbeds(embed).queue(success, failure);
                }
        );
    }

    public void replyInDM(MessageEmbed embed, Consumer<Message> success)
    {
        User user = this.event.getAuthor();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessageEmbeds(embed).queue(success, new ErrorHandler()
                            .ignore(EnumSet.allOf(ErrorResponse.class)));
                }
        );
    }

    public void replyInDM(MessageEmbed embed)
    {
        User user = this.event.getAuthor();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessageEmbeds(embed).queue(null, new ErrorHandler()
                            .ignore(EnumSet.allOf(ErrorResponse.class)));
                }
        );
    }

    public void replyInDM(String message, Consumer<Message> success, Consumer<Throwable> failure)
    {
        User user = this.event.getAuthor();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(message).queue(success, failure);
                }
        );
    }

    public LinkedDataObject getLanguage()
    {
        Guild g = event.getGuild();
        return LocalizationManager.ofGuild(g);
    }

    public void replyInDM(String message, Consumer<Message> success)
    {
        User user = this.event.getAuthor();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(message).queue(success, new ErrorHandler()
                            .ignore(EnumSet.allOf(ErrorResponse.class)));
                }
        );
    }

    public String getLocalized(String query, Object... objects)
    {
        return String.format(LocalizationManager.ofGuild(event.getGuild()).get(query, String.class), objects);
    }

    public String parseDuration(long seconds, String delimiter)
    {
        return LocalizationManager.parseDuration(seconds, getLanguage(), delimiter);
    }

    public void replyInDM(String message)
    {
        User user = this.event.getMember().getUser();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(message).queue(null, new ErrorHandler()
                            .ignore(EnumSet.allOf(ErrorResponse.class)));
                }
        );
    }
}
