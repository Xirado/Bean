package at.xirado.bean.command.slashcommands.leveling;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.RoleReward;
import at.xirado.bean.data.database.entity.DiscordGuild;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class XPRoleRewardCommand extends SlashCommand {
    public XPRoleRewardCommand() {
        setCommandData(Commands.slash("xprolereward", "Rewards a member with a role when they reach a certain level.")
                .addSubcommands(new SubcommandData("create", "Creates a role reward.")
                        .addOption(OptionType.INTEGER, "level", "Level a member needs to reach to get the role.", true)
                        .addOption(OptionType.ROLE, "role", "Role to receive upon reaching specified level.", true)
                        .addOption(OptionType.BOOLEAN, "persist", "Whether this role should be added back after a member leaves and rejoins. Default: true", false)
                        .addOption(OptionType.BOOLEAN, "remove_on_next_reward", "Whether this role should be removed after a member reached the next reward. Default: False", false)
                )
                .addSubcommands(new SubcommandData("remove", "Removes a role reward.")
                        .addOption(OptionType.INTEGER, "level", "Level to remove a role reward from.", true))
                .addSubcommands(new SubcommandData("list", "Lists all role rewards."))
        );
        addRequiredUserPermissions(Permission.ADMINISTRATOR);
        addRequiredBotPermissions(Permission.MANAGE_ROLES);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        DiscordGuild guildData = ctx.getGuildData();
        switch (event.getSubcommandName()) {

            case "create" -> {
                long level = event.getOption("level").getAsLong();
                Role role = event.getOption("role").getAsRole();
                boolean persist = event.getOption("persist") == null || event.getOption("persist").getAsBoolean();
                boolean removeOnNextReward = event.getOption("remove_on_next_reward") != null && event.getOption("remove_on_next_reward").getAsBoolean();
                if (level < 1) {
                    event.replyEmbeds(EmbedUtil.errorEmbed("Cannot create role wards on levels below 1!")).setEphemeral(true).queue();
                    return;
                }
                if (level > 200) {
                    event.replyEmbeds(EmbedUtil.errorEmbed("Can only create role rewards on levels upto 200!")).setEphemeral(true).queue();
                    return;
                }
                if (guildData.hasRoleReward((int) level)) {
                    guildData.addRoleReward((int) level, role.getIdLong(), persist, removeOnNextReward);
                    event.replyEmbeds(EmbedUtil.successEmbed("Role reward has been successfully updated!")).setEphemeral(true).queue();
                    return;
                }
                guildData.addRoleReward((int) level, role.getIdLong(), persist, removeOnNextReward);
                event.replyEmbeds(EmbedUtil.successEmbed("Role reward has been successfully created!")).setEphemeral(true).queue();
            }

            case "remove" -> {
                long level = event.getOption("level").getAsLong();
                if (!guildData.hasRoleReward((int) level)) {
                    event.replyEmbeds(EmbedUtil.errorEmbed("I couldn't find a role reward with that level!")).setEphemeral(true).queue();
                    return;
                }
                guildData.removeRoleReward((int) level);
                event.replyEmbeds(EmbedUtil.successEmbed("Role reward has been successfully removed!")).setEphemeral(true).queue();
            }

            case "list" -> {
                List<RoleReward> rewards = new ArrayList<>(guildData.getRoleRewards());
                if (rewards.isEmpty()) {
                    event.replyEmbeds(EmbedUtil.warningEmbed("Your server does not appear to have any role rewards!")).setEphemeral(true).queue();
                    return;
                }
                rewards.sort(Comparator.comparingInt(RoleReward::getLevel));
                Collections.reverse(rewards);

                StringBuilder builder = new StringBuilder();
                builder.append("`P - Persists on rejoin`\n`R - Gets removed on next reward`\n\n");
                for (RoleReward reward : rewards) {
                    String properties = "";
                    if (reward.getPersistant()) {
                        properties = " - P";
                    }
                    if (reward.getRemoveOnNextReward()) {
                        if (properties.isEmpty()) {
                            properties = " - R";
                        } else {
                            properties += "R";
                        }
                    }
                    builder.append("`Level ").append(reward.getLevel()).append("` - <@&").append(reward.getRoleId()).append(">").append(properties).append("\n");
                }
                event.replyEmbeds(new EmbedBuilder()
                        .setColor(EmbedUtil.DEFAULT_COLOR)
                        .setDescription(builder.toString().trim())
                        .setTitle("Role rewards").build()).setEphemeral(true).queue();
            }
        }
    }
}
