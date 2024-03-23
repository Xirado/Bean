package at.xirado.bean.command.slashcommands.moderation;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.database.entity.DiscordGuild;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ModeratorCommand extends SlashCommand {
    public ModeratorCommand() {
        setCommandData(Commands.slash("moderator", "Sets up moderator roles.")
                .addSubcommands(new SubcommandData("add", "Adds a moderator role.")
                        .addOption(OptionType.ROLE, "role", "Role to add.", true)
                )
                .addSubcommands(new SubcommandData("remove", "Removes a moderator role.")
                        .addOption(OptionType.ROLE, "role", "Role to remove.", true)
                )
                .addSubcommands(new SubcommandData("list", "Lists all moderator roles."))
        );
        addRequiredUserPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        String subcommand = event.getSubcommandName();
        switch (subcommand) {
            case "add" -> {
                Role role = event.getOption("role").getAsRole();
                Set<Long> moderatorRoles = ctx.getGuildData().getModeratorRoles();

                if (moderatorRoles.contains(role.getIdLong())) {
                    ctx.sendSimpleEmbed(ctx.getLocalized("commands.moderator.already_added"));
                    return;
                }

                ctx.getGuildData().addModeratorRole(role.getIdLong());
                EmbedBuilder builder = new EmbedBuilder()
                        .setColor(role.getColor())
                        .setDescription(ctx.getLocalized("commands.moderator.added", role.getAsMention()));
                ctx.reply(builder.build()).queue();
            }
            case "remove" -> {
                Role role = event.getOption("role").getAsRole();
                Set<Long> allowedRoles = ctx.getGuildData().getModeratorRoles();

                if (!allowedRoles.contains(role.getIdLong())) {
                    ctx.sendSimpleEmbed(ctx.getLocalized("commands.moderator.not_added"));
                    return;
                }

                ctx.getGuildData().removeModeratorRole(role.getIdLong());
                EmbedBuilder builder = new EmbedBuilder()
                        .setColor(role.getColor())
                        .setDescription(ctx.getLocalized("commands.moderator.removed", role.getAsMention()));
                ctx.reply(builder.build()).queue();
            }
            case "list" -> {
                DiscordGuild guildData = ctx.getGuildData();
                Set<Long> roleIds = guildData.getModeratorRoles();
                List<Role> roles = roleIds.stream()
                        .map(id -> event.getGuild().getRoleById(id))
                        .filter(Objects::nonNull)
                        .toList();

                if (roles.isEmpty()) {
                    ctx.sendSimpleEmbed(ctx.getLocalized("commands.listmods.no_roles_found"));
                    return;
                }

                StringBuilder sb = new StringBuilder();
                Color firstColor = null;

                if (roles.size() > 1)
                    roles = roles.stream().sorted(Comparator.comparingInt(Role::getPosition).reversed()).toList();

                for (Role r : roles) {
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
