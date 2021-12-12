package at.xirado.bean.command.commands;

import at.xirado.bean.command.Command;
import at.xirado.bean.command.CommandCategory;
import at.xirado.bean.command.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.Checks;

public class Settings extends Command
{
    public Settings()
    {
        super("settings", "changes guild-settings", "settings (subcommand)");
        setCommandCategory(CommandCategory.ADMIN);
        setRequiredPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(MessageReceivedEvent event, CommandContext context)
    {
        String[] args = context.getArguments().toStringArray();
        String prefix = context.getGuildData().getPrefix();
        if (args.length == 0)
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(0x551A8B)
                    .setFooter("Settings")
                    .setTitle("Settings")
                    .setDescription(getHelp(prefix));
            context.reply(builder.build());
            return;
        }
        String subCommand = args[0].toLowerCase();
        switch (subCommand)
        {
            case "prefix":
                if (args.length < 2)
                {
                    context.reply("My prefix is `" + prefix + "`!");
                    return;
                }
                String newPrefix = args[1];
                try
                {
                    Checks.notEmpty(newPrefix, "Prefix");
                } catch (Exception ex)
                {
                    context.replyError("The prefix you entered is invalid!");
                    return;
                }
                if (newPrefix.length() > 6)
                {
                    context.replyError("The prefix can only be 6 characters long!");
                    return;
                }
                context.getGuildData().put("command_prefix", newPrefix).update();
                context.replySuccess("The command-prefix has been updated to `" + newPrefix + "`!");
                break;
            case "logchannel":
                if (args.length < 2)
                {
                    Long currentChannelId = context.getGuildData().getLong("log_channel");
                    if (currentChannelId == null)
                    {
                        context.replyWarning("There is no log-channel set!");
                        return;
                    }
                    TextChannel currentChannel = event.getGuild().getTextChannelById(currentChannelId);
                    if (currentChannel == null)
                    {
                        context.replyError("The chosen log-channel no longer exists!");
                        return;
                    }
                    context.reply("Events get logged in " + currentChannel.getAsMention());
                    return;
                }
                String newChannelId = args[1].replaceAll("[^0-9]", "");
                TextChannel newChannel = event.getGuild().getTextChannelById(newChannelId);
                if (newChannel == null)
                {
                    context.replyError("The ID or channel you have entered is invalid!");
                    return;
                }
                context.getGuildData().put("log_channel", Long.parseLong(newChannelId)).update();
                context.replySuccess("Updated log-channel to " + newChannel.getAsMention() + "!");
                break;
            default:
                context.replyError("Invalid subcommand!");
        }
    }


    private static String getHelp(String prefix)
    {
        return "`" + prefix + "settings prefix` - updates the command-prefix\n" +
                "`" + prefix + "settings logchannel` - updates the channel for logs";
    }
}
