package at.xirado.bean.command.slashcommands.moderation;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.GuildData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ModeratorCommand extends SlashCommand
{
    public ModeratorCommand()
    {
        setCommandData(new CommandData("moderator", "add/remove/list moderator roles")
                .addSubcommands(new SubcommandData("add", "add a moderator role")
                        .addOption(OptionType.ROLE, "role", "the role to add", true)
                )
                .addSubcommands(new SubcommandData("remove", "remove a moderator role")
                        .addOption(OptionType.ROLE, "role", "the role to remove", true)
                )
                .addSubcommands(new SubcommandData("list", "list all moderator roles"))
        );
        setRequiredUserPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        String subcommand = event.getSubcommandName();
        switch (subcommand)
        {
            case "add" -> {
                Role role = event.getOption("role").getAsRole();
                Set<Role> moderatorRoles = ctx.getGuildData().getModeratorRoles();
                if (moderatorRoles.contains(role))
                {
                    ctx.sendSimpleEmbed(ctx.getLocalized("commands.moderator.already_added"));
                    return;
                }
                ctx.getGuildData().addModeratorRoles(role).update();
                EmbedBuilder builder = new EmbedBuilder()
                        .setColor(role.getColor())
                        .setDescription(ctx.getLocalized("commands.moderator.added", role.getAsMention()));
                ctx.reply(builder.build()).queue();
            }
            case "remove" -> {
                Role role = event.getOption("role").getAsRole();
                Set<Role> allowedRoles = ctx.getGuildData().getModeratorRoles();
                if (!allowedRoles.contains(role))
                {
                    ctx.sendSimpleEmbed(ctx.getLocalized("commands.moderator.not_added"));
                    return;
                }
                ctx.getGuildData().removeModeratorRoles(role).update();
                EmbedBuilder builder = new EmbedBuilder()
                        .setColor(role.getColor())
                        .setDescription(ctx.getLocalized("commands.moderator.removed", role.getAsMention()));
                ctx.reply(builder.build()).queue();
            }
            case "list" -> {
                GuildData guildData = ctx.getGuildData();
                Set<Role> allowedRoles = guildData.getModeratorRoles();
                if (allowedRoles == null || allowedRoles.isEmpty())
                {
                    ctx.sendSimpleEmbed(ctx.getLocalized("commands.listmods.no_roles_found"));
                    return;
                }
                StringBuilder sb = new StringBuilder();
                Color firstColor = null;
                if (allowedRoles.size() > 1)
                {
                    allowedRoles = allowedRoles.stream().sorted(Comparator.comparingInt(Role::getPosition)).collect(Collectors.toCollection(LinkedHashSet::new));
                    allowedRoles = allowedRoles.stream().sorted(Collections.reverseOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
                }
                for (Role r : allowedRoles)
                {
                    if (firstColor == null) firstColor = r.getColor();
                    sb.append(r.getAsMention()).append(", ");
                }
                String description = sb.toString();
                description = description.substring(0, description.length() - 2);
                EmbedBuilder builder = new EmbedBuilder()
                        .setColor(firstColor == null ? Color.green : firstColor)
                        .setDescription(ctx.getLocalized("commands.listmods.all_mod_roles") + ":\n" + description);
                ctx.reply(builder.build()).queue();
            }
        }
    }
}
