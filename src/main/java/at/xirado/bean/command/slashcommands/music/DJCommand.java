package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.GuildData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DJCommand extends SlashCommand
{
    public DJCommand()
    {
        setCommandData(Commands.slash("dj", "Set up users/roles who can use DJ-commands.")
                .addSubcommands(new SubcommandData("add", "Adds a DJ. (role or member)")
                        .addOption(OptionType.MENTIONABLE, "role_or_member", "Role or member", true)
                )
                .addSubcommands(new SubcommandData("remove", "Removes a DJ. (role or member)")
                        .addOption(OptionType.MENTIONABLE, "role_or_member", "Role or member", true)
                )
                .addSubcommands(new SubcommandData("list", "Lists all DJs."))
        );
        setRequiredUserPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx)
    {
        GuildData guildData = ctx.getGuildData();
        switch (event.getSubcommandName().toLowerCase())
        {
        case "add" -> {
            IMentionable mentionable = event.getOption("role_or_member").getAsMentionable();
            if (mentionable instanceof Role role)
            {
                if (guildData.isDJ(role))
                {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(role.getColor())
                            .setDescription(role.getAsMention() + " is already a DJ!");
                    ctx.reply(builder.build()).queue();
                    return;
                }
                guildData.addDJRoles(role).update();
                EmbedBuilder builder = new EmbedBuilder()
                        .setColor(role.getColor())
                        .setDescription(role.getAsMention() + " is now a DJ!");
                ctx.reply(builder.build()).queue();
            }
            else if (mentionable instanceof Member member)
            {
                if (guildData.getDJMembers().contains(member.getIdLong()))
                {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(member.getColor())
                            .setDescription(member.getAsMention() + " is already a DJ!");
                    ctx.reply(builder.build()).queue();
                    return;
                }
                guildData.addDJMembers(member).update();
                EmbedBuilder builder = new EmbedBuilder()
                        .setColor(member.getColor())
                        .setDescription(member.getAsMention() + " is now a DJ!");
                ctx.reply(builder.build()).queue();
            }
            else
            {
                ctx.replyError("Invalid argument! Only roles and members are allowed!").queue();
            }
        }
        case "remove" -> {
            IMentionable mentionable = event.getOption("role_or_member").getAsMentionable();
            if (mentionable instanceof Role role)
            {
                if (!guildData.isDJ(role))
                {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(role.getColor())
                            .setDescription(role.getAsMention() + " is already not a DJ!");
                    ctx.reply(builder.build()).queue();
                    return;
                }
                guildData.removeDJRoles(role).update();
                EmbedBuilder builder = new EmbedBuilder()
                        .setColor(role.getColor())
                        .setDescription(role.getAsMention() + " is no longer a DJ!");
                ctx.reply(builder.build()).queue();
            }
            else if (mentionable instanceof Member member)
            {
                if (!guildData.getDJMembers().contains(member.getIdLong()))
                {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(member.getColor())
                            .setDescription(member.getAsMention() + " is already not a DJ!");
                    ctx.reply(builder.build()).queue();
                    return;
                }
                guildData.removeDJMembers(member).update();
                EmbedBuilder builder = new EmbedBuilder()
                        .setColor(member.getColor())
                        .setDescription(member.getAsMention() + " is no longer a DJ!");
                ctx.reply(builder.build()).queue();
            }
            else
            {
                ctx.replyError("Invalid argument! Only roles and members are allowed!").queue();
            }
        }
        case "list" -> {
            List<Role> djRoles = guildData.getDJRoles(false);
            List<Long> djMembers = guildData.getDJMembers();
            if (djRoles.isEmpty() && djMembers.isEmpty())
            {
                ctx.sendSimpleEmbed("There are no DJs!");
                return;
            }
            StringBuilder builder = new StringBuilder();
            builder.append("**DJs**:\n\n");
            djRoles.forEach(x -> builder.append(x.getAsMention()).append("\n"));
            builder.append("\n");
            djMembers.forEach(x -> builder.append("<@").append(x).append(">\n"));
            ctx.sendSimpleEmbed(builder.toString().trim());
        }
        }
    }
}
