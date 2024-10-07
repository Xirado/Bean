/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.xirado.bean.command.slashcommands.leveling;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.Util;
import at.xirado.bean.translation.LocaleLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.SQLException;

public class XPAlertCommand extends SlashCommand {
    private static Logger logger = LoggerFactory.getLogger(XPAlertCommand.class);

    public XPAlertCommand() {
        setCommandData(Commands.slash("setxpalerts", "Changes XP levelup alert behaviour.")
                .addSubcommands(new SubcommandData("none", "Disables xp alerts entirely."))
                .addSubcommands(new SubcommandData("dm", "Notifies the user via DM when they level up."))
                .addSubcommands(new SubcommandData("current", "Notifies the user in the current channel when they level up."))
                .addSubcommands(new SubcommandData("channel", "Notifies the user in a specified channel when they level up.")
                        .addOption(OptionType.CHANNEL, "targetchannel", "Channel where level-ups should get logged.", true)
                )
        );
        addRequiredUserPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        Guild guild = event.getGuild();

        switch (event.getSubcommandName()) {

            case "none" -> {
                boolean success = setXPAlert(guild, "none");
                if (success)
                    ctx.reply("XP alerts have been successfully disabled!").setEphemeral(true).queue();
                else
                    ctx.replyError("Could not disable XP alerts!").setEphemeral(true).queue();
            }

            case "dm" -> {
                boolean success = setXPAlert(guild, "dm");
                if (success)
                    ctx.reply("XP alerts have been set to **DM**").setEphemeral(true).queue();
                else
                    ctx.replyError("Could not set XP alert mode!").setEphemeral(true).queue();
            }

            case "current" -> {
                boolean success = setXPAlert(guild, "current");
                if (success)
                    ctx.reply("XP alerts have been set to **current channel**").setEphemeral(true).queue();
                else
                    ctx.replyError("Could not set XP alert mode!").setEphemeral(true).queue();
            }

            case "channel" -> {
                GuildChannel channel = event.getOption("targetchannel").getAsChannel();
                if (channel == null) {
                    ctx.replyError("Invalid channel").setEphemeral(true).queue();
                    return;
                }

                if (channel.getType() != ChannelType.TEXT) {
                    ctx.replyError("Can only use text-channels as XP alert target!").setEphemeral(true).queue();
                    return;
                }
                boolean success = setXPAlert(guild, channel.getId());
                if (success)
                    ctx.reply("XP alerts are now sent in **" + channel.getName() + "**").setEphemeral(true).queue();
                else
                    ctx.reply("Could not set XP alert mode!").setEphemeral(true).queue();
            }

            default -> ctx.replyError("Unknown target!").setEphemeral(true).queue();

        }
    }

    public static String getXPAlert(@Nonnull Guild guild) {
        try (var connection = Bean.getInstance().getDatabase().getConnectionFromPool();
             var ps = connection.prepareStatement("SELECT mode FROM xpAlerts WHERE guildID = ?")) {
            ps.setLong(1, guild.getIdLong());
            var rs = ps.executeQuery();
            if (rs.next()) return rs.getString("mode");
            return "current";
        } catch (SQLException ex) {
            logger.error("Could not get level-up message mode of guild " + guild.getIdLong(), ex);
            return "none";
        }
    }

    public static void sendXPAlert(@Nonnull Member member, int level, MessageChannel current) {
        DataObject json = LocaleLoader.ofGuild(member.getGuild());
        String mode = getXPAlert(member.getGuild());
        switch (mode) {
            case "dm" -> {
                String message = "**" + member.getGuild().getName() + "**: " + "Hey, you just ranked up to level **" + level + "**!";
                Util.sendDM(member.getIdLong(), message);
            }
            case "current" -> {
                if (current == null) return;
                String localized = Util.getRecursive(json, "commands.xp.ranked");
                current.sendMessage(Util.format(localized, member.getAsMention(), level)).queue();
            }
            case "none" -> { }
            default -> {
                TextChannel channel = member.getGuild().getTextChannelById(mode);
                if (channel == null) return;
                String localized = Util.getRecursive(json, "commands.xp.ranked");
                channel.sendMessage(Util.format(localized, member.getAsMention(), level)).queue();
            }
        }
    }

    public static boolean setXPAlert(@Nonnull Guild guild, String modeOrChannelID) {
        try (var connection = Bean.getInstance().getDatabase().getConnectionFromPool();
             var ps = connection.prepareStatement("INSERT INTO xpAlerts (guildID, mode) VALUES (?,?) ON DUPLICATE KEY UPDATE mode = ?")) {
            ps.setLong(1, guild.getIdLong());
            ps.setString(2, modeOrChannelID);
            ps.setString(3, modeOrChannelID);
            ps.execute();
            return true;
        } catch (SQLException ex) {
            logger.error("Could not set level-up message mode for guild " + guild.getIdLong(), ex);
            return false;
        }
    }

}
