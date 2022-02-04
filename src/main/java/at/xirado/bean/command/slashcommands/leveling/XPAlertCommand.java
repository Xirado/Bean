package at.xirado.bean.command.slashcommands.leveling;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.LinkedDataObject;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.misc.Util;
import at.xirado.bean.translation.LocaleLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;

public class XPAlertCommand extends SlashCommand
{
    public XPAlertCommand()
    {
        setCommandData(Commands.slash("setxpalerts", "Changes XP levelup alert behaviour.")
                .addSubcommands(new SubcommandData("none", "Disables xp alerts entirely."))
                .addSubcommands(new SubcommandData("dm", "Notifies the user via DM when they level up."))
                .addSubcommands(new SubcommandData("current", "Notifies the user in the current channel when they level up."))
                .addSubcommands(new SubcommandData("channel", "Notifies the user in a specified channel when they level up.")
                        .addOption(OptionType.CHANNEL, "targetchannel", "Channel where level-ups should get logged.", true)
                )
        );
        setRequiredUserPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Guild guild = event.getGuild();
        switch (event.getSubcommandName())
        {
            case "none":
                boolean a = setXPAlert(guild, "none");
                if (a)
                    ctx.reply("XP alerts have been successfully disabled!").setEphemeral(true).queue();
                else
                    ctx.replyError("Could not disable XP alerts!").setEphemeral(true).queue();
                return;
            case "dm":
                boolean b = setXPAlert(guild, "dm");
                if (b)
                    ctx.reply("XP alerts have been set to **DM**").setEphemeral(true).queue();
                else
                    ctx.replyError("Could not set XP alert mode!").setEphemeral(true).queue();
                return;
            case "current":
                boolean c = setXPAlert(guild, "current");
                if (c)
                    ctx.reply("XP alerts have been set to **current channel**").setEphemeral(true).queue();
                else
                    ctx.replyError("Could not set XP alert mode!").setEphemeral(true).queue();
                return;
            case "channel":
                GuildChannel channel = event.getOption("targetchannel").getAsGuildChannel();
                if (channel.getType() != ChannelType.TEXT)
                {
                    ctx.replyError("Can only use text-channels as XP alert target!").setEphemeral(true).queue();
                    return;
                }
                boolean d = setXPAlert(guild, channel.getId());
                if (d)
                    ctx.reply("XP alerts are now sent in **" + channel.getName() + "**").setEphemeral(true).queue();
                else
                    ctx.reply("Could not set XP alert mode!").setEphemeral(true).queue();
                return;
            default:
                ctx.replyError("Unknown target!").setEphemeral(true).queue();
        }
    }


    public static String getXPAlert(@Nonnull Guild guild)
    {
        Connection connection = Database.getConnectionFromPool();
        try (var ps = connection.prepareStatement("SELECT mode FROM xpAlerts WHERE guildID = ?"))
        {
            ps.setLong(1, guild.getIdLong());
            var rs = ps.executeQuery();
            if (rs.next()) return rs.getString("mode");
            return "current";
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            return "none";
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public static void sendXPAlert(@Nonnull Member member, int level, MessageChannel current)
    {
        LinkedDataObject json = LocaleLoader.ofGuild(member.getGuild());
        String mode = getXPAlert(member.getGuild());
        switch (mode)
        {
            case "none":
                return;
            case "dm":
                String message = "**" + member.getGuild().getName() + "**: " + "Hey, you just ranked up to level **" + level + "**!";
                Util.sendDM(member.getIdLong(), message);
                return;
            case "current":
                if (current == null) return;
                current.sendMessage(json.getString("commands.xp.ranked", member.getAsMention(), String.valueOf(level))).queue();
                return;
            default:
                TextChannel channel = member.getGuild().getTextChannelById(mode);
                if (channel == null) return;
                channel.sendMessage(json.getString("commands.xp.ranked", member.getAsMention(), String.valueOf(level))).queue();
        }
    }

    public static boolean setXPAlert(@Nonnull Guild guild, String modeOrChannelID)
    {
        Connection connection = Database.getConnectionFromPool();
        try (var ps = connection.prepareStatement("INSERT INTO xpAlerts (guildID, mode) VALUES (?,?) ON DUPLICATE KEY UPDATE mode = ?"))
        {
            ps.setLong(1, guild.getIdLong());
            ps.setString(2, modeOrChannelID);
            ps.setString(3, modeOrChannelID);
            ps.execute();
            return true;
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            return false;
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

}
