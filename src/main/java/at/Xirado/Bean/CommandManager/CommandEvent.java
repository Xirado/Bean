package at.Xirado.Bean.CommandManager;

import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;
import java.util.function.Consumer;

public class CommandEvent extends GuildMessageReceivedEvent
{


    public static final String LOADING_EMOTE = "<a:loading:779763323821359104>";
    public static final String WARNING_EMOTE = "⚠";
    public static final String ERROR_EMOTE = "❌";
    public static final String SUCCESS_EMOTE = "✅";
    public final GuildMessageReceivedEvent event;
    public final CommandArgument arg;
    public Command command;

    public CommandEvent(CommandArgument arg, GuildMessageReceivedEvent e)
    {
        super(e.getJDA(), e.getResponseNumber(), e.getMessage());
        this.arg = arg;
        this.event = e;

    }
    public Member getSelfMember()
    {
        return this.event.getGuild().getMember(DiscordBot.instance.jda.getSelfUser());
    }
    public void replyError(String message)
    {
        this.event.getChannel().sendMessage(
                new EmbedBuilder()
                .setColor(Color.red)
                .setTimestamp(Instant.now())
                .setDescription(ERROR_EMOTE+" "+message)
                .build()
        ).queue();
    }
    public Command getCommand()
    {
        return this.command;
    }
    public void deleteInvokeMessage()
    {
        this.event.getMessage().delete().queue();
    }
    public void setCommand(Command command)
    {
        this.command = command;
    }
    public void replyinLogChannel(String message)
    {
        TextChannel logchannel = Util.getLogChannel(this.event.getGuild());
        if(logchannel != null) logchannel.sendMessage(message).queue();
    }
    public void replyinLogChannel(Message message)
    {
        TextChannel logchannel = Util.getLogChannel(this.event.getGuild());
        if(logchannel != null) logchannel.sendMessage(message).queue();
    }
    public void replyinLogChannel(MessageEmbed message)
    {
        TextChannel logchannel = Util.getLogChannel(this.event.getGuild());
        if(logchannel != null) logchannel.sendMessage(message).queue();
    }
    public void replyErrorUsage()
    {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.red)
                .setAuthor(this.event.getMember().getUser().getAsTag(), null, this.event.getMember().getUser().getEffectiveAvatarUrl())
                .setTitle("Invalid arguments!")
                .setTimestamp(Instant.now())
                .setFooter("Developed by Xirado");
        String usage = this.getCommand().usage;
        String[] aliases = this.getCommand().aliases;
        StringBuilder sb = new StringBuilder();
        String aliasesstring = null;
        if(aliases.length > 0)
        {
            for(String alias : aliases)
            {
                sb.append(alias).append(", ");
            }
            aliasesstring = sb.toString();
            aliasesstring = aliasesstring.substring(0,aliasesstring.length()-2);
        }
        String description = "`"+usage+"`\n"+this.getCommand().description;
        if(aliases.length > 0 && aliasesstring != null)
        {
            description+="\nAliases: `"+aliasesstring+"`";
        }
        builder.setDescription(description);
        this.getChannel().sendMessage(builder.build()).queue();

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
        if(guildVoiceState != null && guildVoiceState.getChannel().getMembers().size() <= 2)
        {
            return true;
        }
        if(m.hasPermission(Permission.MANAGE_CHANNEL))
            return true;
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
                    pc.sendMessage(message).queue(success);
                }
        );
    }
    public void replyInDM(Message message)
    {
        User user = this.event.getMember().getUser();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(message).queue();
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
                    pc.sendMessage(embed).queue(success);
                }
        );
    }
    public void replyInDM(MessageEmbed embed)
    {
        User user = this.event.getMember().getUser();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(embed).queue();
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
    public void replyInDM(String message, Consumer<Message> success)
    {
        User user = this.event.getMember().getUser();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(message).queue(success);
                }
        );
    }
    public void replyInDM(String message)
    {
        User user = this.event.getMember().getUser();
        user.openPrivateChannel().queue(
                (pc) ->
                {
                    pc.sendMessage(message).queue();
                }
        );
    }
    public CommandArgument getArguments()
    {
        return this.arg;
    }
}
