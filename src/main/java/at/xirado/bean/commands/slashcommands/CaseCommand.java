package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.SlashCommandContext;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.misc.Util;
import at.xirado.bean.objects.SlashCommand;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.Punishments;
import at.xirado.bean.translation.FormattedDuration;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class CaseCommand extends SlashCommand
{
    public CaseCommand()
    {
        setCommandData(new CommandData("case", "shows/modifies moderation cases")
                .addSubcommands(new SubcommandData("show", "shows information about a modcase")
                        .addOption(OptionType.STRING, "case", "the case to show infos about", true)
                )
                .addSubcommands(new SubcommandData("reason", "changes the reason of a modcase")
                        .addOption(OptionType.STRING, "case", "the case you want to change the reason on", true)
                        .addOption(OptionType.STRING, "reason", "the new reason", true)
                )
                .addSubcommands(new SubcommandData("delete", "deletes a modcase")
                    .addOption(OptionType.STRING, "case", "the case you want to delete", true)
                )
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Guild g = event.getGuild();
        if(sender == null || g == null) return;
        PermissionCheckerManager permissionCheckerManager = Bean.getInstance().permissionCheckerManager;
        if(!permissionCheckerManager.isModerator(sender) && !sender.hasPermission(Permission.ADMINISTRATOR))
        {
            ctx.reply(SlashCommandContext.DENY+" "+ctx.getLocalized("general.no_perms")).setEphemeral(true).queue();
            return;
        }

        if(event.getSubcommandName() == null) return;
        if(event.getSubcommandName().equals("show"))
        {
            String caseID = event.getOption("case").getAsString();
            if(caseID.length() != 6)
            {
                ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("commands.casecmd.must_be_6_digit")).setEphemeral(true).queue();
                return;
            }
            Case modcase = Punishments.getCaseByID(caseID, g.getIdLong());
            if(modcase == null)
            {
                ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("commands.casecmd.not_exists", caseID.toUpperCase())).setEphemeral(true).queue();
                return;
            }
            StringBuilder builder = new StringBuilder();
            builder.append(SlashCommandContext.SUCCESS + " ").append(modcase.getType().getFriendlyName()).append(" - "+ctx.getLocalized("commands.case")+" #").append(modcase.getCaseID()).append("\n\n");
            builder.append(ctx.getLocalized("commands.issued")).append(" ").append(FormattedDuration.getDuration(modcase.getCreatedAt() / 1000, true, ctx.getLanguage())).append(" "+ctx.getLocalized("commands.by")+" <@").append(modcase.getModeratorID()).append(">").append("\n");
            builder.append(ctx.getLocalized("commands.reason")).append(": ").append(modcase.getReason()).append("\n");
            if(modcase.getDuration() > 0)
            {
                builder.append("\n").append(ctx.getLocalized("commands.duration")).append(": ").append(Util.getLength(modcase.getDuration() / 1000));
            }
            event.reply(builder.toString()).allowedMentions(Collections.emptyList()).setEphemeral(true).queue();
            return;
        }else if(event.getSubcommandName().equals("reason"))
        {
            String caseID = event.getOption("case").getAsString();
            String reason = event.getOption("reason").getAsString();
            if(caseID.length() != 6)
            {
                ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("commands.casecmd.must_be_6_digit")).setEphemeral(true).queue();
                return;
            }
            Case modcase = Punishments.getCaseByID(caseID, g.getIdLong());
            if(modcase == null)
            {
                ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("commands.casecmd.not_exists", caseID.toUpperCase())).setEphemeral(true).queue();
                return;
            }
            modcase.setReason(reason);
            ctx.reply(SlashCommandContext.SUCCESS+" "+ctx.getLocalized("commands.casecmd.reason_changed", caseID.toUpperCase(), reason)).setEphemeral(true).queue();
        }else if(event.getSubcommandName().equals("delete"))
        {
            String caseID = event.getOption("case").getAsString();
            if(caseID.length() != 6)
            {
                ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("commands.casecmd.must_be_6_digit")).setEphemeral(true).queue();
                return;
            }
            Case modcase = Punishments.getCaseByID(caseID, g.getIdLong());
            if(modcase == null)
            {
                ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("commands.casecmd.not_exists", caseID.toUpperCase())).setEphemeral(true).queue();
                return;
            }
            boolean x = modcase.deleteCase();
            if(x)
            {
                ctx.reply(SlashCommandContext.SUCCESS+" "+ctx.getLocalized("commands.casecmd.deleted", modcase.getCaseID())).setEphemeral(true).queue();

            }else
            {
                ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("commands.casecmd.err_deleted", modcase.getCaseID())).setEphemeral(true).queue();
            }
        }
    }
}
