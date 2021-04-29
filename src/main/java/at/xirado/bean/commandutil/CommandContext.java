package at.xirado.bean.commandutil;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.JSON;
import at.xirado.bean.objects.Command;
import at.xirado.bean.translation.LanguageLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
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

    private final GuildMessageReceivedEvent event;
    private final at.xirado.bean.objects.Command command;
    private final Member member;
    private final CommandArgument commandArgument;


    public CommandContext(GuildMessageReceivedEvent event, CommandArgument commandArgument, at.xirado.bean.objects.Command command, Member member)
    {
        this.event = event;
        this.commandArgument = commandArgument;
        this.command = command;
        this.member = member;
    }

    public GuildMessageReceivedEvent getEvent()
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
        this.event.getChannel().sendMessage(
                new EmbedBuilder()
                        .setColor(Color.red)
                        .setDescription(ERROR_EMOTE+" "+message)
                        .build()
        ).queue();
    }

    public void deleteInvokeMessage()
    {
        this.event.getMessage().delete().queue();
    }

    public void replyInLogChannel(String message)
    {
        TextChannel logchannel = Bean.getInstance().logChannelManager.getLogChannel(this.event.getGuild().getIdLong());
        if(logchannel != null) logchannel.sendMessage(message).queue();
    }


    public TextChannel getLogChannel()
    {
        return Bean.getInstance().logChannelManager.getLogChannel(this.event.getGuild().getIdLong());
    }

    public boolean hasLogChannel()
    {
        return Bean.getInstance().logChannelManager.getLogChannel(this.event.getGuild().getIdLong()) != null;
    }

    public void replyInLogChannel(Message message)
    {
        TextChannel logchannel = Bean.getInstance().logChannelManager.getLogChannel(this.event.getGuild().getIdLong());
        if(logchannel != null) logchannel.sendMessage(message).queue();
    }
    public void replyInLogChannel(MessageEmbed message)
    {
        TextChannel logchannel = Bean.getInstance().logChannelManager.getLogChannel(this.event.getGuild().getIdLong());
        if(logchannel != null) logchannel.sendMessage(message).queue();
    }
    public void replyErrorUsage()
    {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.red)
                .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getEffectiveAvatarUrl())
                .setTitle(LanguageLoader.ofGuild(event.getGuild()).get("general.invalid_arguments", String.class))
                .setTimestamp(Instant.now());
        String usage = this.getCommand().getUsage();
        List<String> aliases = this.getCommand().getAliases();
        StringBuilder sb = new StringBuilder();
        String aliasesstring = null;
        if(aliases.size() > 0)
        {
            for(String alias : aliases)
            {
                sb.append(alias).append(", ");
            }
            aliasesstring = sb.toString().trim();
        }
        String description = "`"+usage+"`\n"+this.getCommand().getDescription();
        if(aliases.size() > 0 && aliasesstring != null)
        {
            description+="\n"+ LanguageLoader.ofGuild(event.getGuild()).get("general.aliases", String.class)+": `"+aliasesstring+"`";
        }
        builder.setDescription(description);
        event.getChannel().sendMessage(builder.build()).queue();

    }
    public void replyWarning(String message)
    {
        this.event.getChannel().sendMessage(
                new EmbedBuilder()
                        .setColor(Color.yellow)
                        .setTimestamp(Instant.now())
                        .setDescription(WARNING_EMOTE+" "+message)
                        .build()
        ).queue();
    }
    public boolean isDJ()
    {
        Member m = this.event.getMember();
        GuildVoiceState guildVoiceState = m.getVoiceState();
        if(guildVoiceState != null)
        {
            if(guildVoiceState.getChannel().getMembers().size() <= 2) return true;
        }

        if(m.hasPermission(Permission.MANAGE_CHANNEL)) return true;
        Role djRole = m.getRoles().stream()
                .filter(role -> role.getName().equalsIgnoreCase("DJ")) // filter by role name
                .findFirst() // take first result
                .orElse(null); // else return null
        return djRole != null;
    }
    public void replySuccess(String message)
    {
        this.event.getChannel().sendMessage(
                new EmbedBuilder()
                        .setColor(Color.green)
                        .setTimestamp(Instant.now())
                        .setDescription(SUCCESS_EMOTE+" "+message)
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
    //1
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
        event.getChannel().sendMessage(embed).queue(success, failure);
    }
    public void reply(MessageEmbed embed, Consumer<Message> success)
    {
        event.getChannel().sendMessage(embed).queue(success);
    }
    public void reply(MessageEmbed embed)
    {
        event.getChannel().sendMessage(embed).queue();
    }
    // DM
    public void replyInDM(Message message, Consumer<Message> success, Consumer<Throwable> failure)
    {
        User user = this.event.getMember().getUser();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(message).queue(success,failure);
                }
        );
    }
    public void replyInDM(Message message, Consumer<Message> success)
    {
        User user = this.event.getMember().getUser();
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
        User user = this.event.getMember().getUser();
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
        User user = this.event.getMember().getUser();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(embed).queue(success,failure);
                }
        );
    }
    public void replyInDM(MessageEmbed embed, Consumer<Message> success)
    {
        User user = this.event.getMember().getUser();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(embed).queue(success, new ErrorHandler()
                            .ignore(EnumSet.allOf(ErrorResponse.class)));
                }
        );
    }
    public void replyInDM(MessageEmbed embed)
    {
        User user = this.event.getMember().getUser();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(embed).queue(null, new ErrorHandler()
                            .ignore(EnumSet.allOf(ErrorResponse.class)));
                }
        );
    }
    public void replyInDM(String message, Consumer<Message> success, Consumer<Throwable> failure)
    {
        User user = this.event.getMember().getUser();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(message).queue(success,failure);
                }
        );
    }

    public JSON getLanguage()
    {
        Guild g = event.getGuild();
        return LanguageLoader.ofGuild(g);
    }

    public void replyInDM(String message, Consumer<Message> success)
    {
        User user = this.event.getMember().getUser();
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
        return String.format(LanguageLoader.ofGuild(event.getGuild()).get(query, String.class), objects);
    }

    public String parseDuration(long seconds, String delimiter)
    {
        return LanguageLoader.parseDuration(seconds, getLanguage(), delimiter);
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
