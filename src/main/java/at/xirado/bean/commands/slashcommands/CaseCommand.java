package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.*;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.language.FormattedDuration;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.Util;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.Punishments;
import com.mysql.cj.util.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

public class CaseCommand extends SlashCommand
{
    public CaseCommand()
    {
        setCommandData(new CommandUpdateAction.CommandData("case", "shows/modifies moderation cases")
                .addSubcommand(new CommandUpdateAction.SubcommandData("show", "shows information about a modcase")
                        .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "case", "the case to show infos about").setRequired(true))
                )
                .addSubcommand(new CommandUpdateAction.SubcommandData("reason", "changes the reason of a modcase")
                        .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "case", "the case you want to change the reason on").setRequired(true))
                        .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "reason", "the new reason").setRequired(true))
                )
                .addSubcommand(new CommandUpdateAction.SubcommandData("delete", "deletes a modcase")
                    .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "case", "the case you want to delete").setRequired(true))
                )
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull CommandContext ctx)
    {
        Guild g = event.getGuild();
        if(sender == null || g == null) return;
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        if(!permissionCheckerManager.isModerator(sender) && !sender.hasPermission(Permission.ADMINISTRATOR))
        {
            ctx.reply(CommandContext.DENY+" You don't have permission to do this!").setEphemeral(true).queue();
            return;
        }

        if(event.getSubcommandName() == null) return;
        if(event.getSubcommandName().equals("show"))
        {
            String caseID = event.getOption("case").getAsString();
            if(caseID.length() != 6)
            {
                ctx.reply(CommandContext.ERROR+" The Case-ID must be 6 characters long!").setEphemeral(true).queue();
                return;
            }
            Case modcase = Punishments.getCaseByID(caseID, g.getIdLong());
            if(modcase == null)
            {
                ctx.reply(CommandContext.ERROR+" The case with the ID \""+caseID.toUpperCase()+"\" does not exist!").setEphemeral(true).queue();
                return;
            }
            StringBuilder builder = new StringBuilder();
            builder.append(CommandContext.SUCCESS + " ").append(modcase.getType().getFriendlyName()).append(" - Case #").append(modcase.getCaseID()).append("\n\n");
            builder.append("Issued ").append(FormattedDuration.getDuration(modcase.getCreatedAt()/1000, true)).append(" by <@").append(modcase.getModeratorID()).append(">").append("\n");
            builder.append("Reason: ").append(modcase.getReason()).append("\n");
            if(modcase.getDuration() > 0)
            {
                builder.append("\nDuration: ").append(Util.getLength(modcase.getDuration() / 1000));
            }
            event.reply(builder.toString()).allowedMentions(Collections.emptyList()).setEphemeral(true).queue();
            return;
        }else if(event.getSubcommandName().equals("reason"))
        {
            String caseID = event.getOption("case").getAsString();
            String reason = event.getOption("reason").getAsString();
            if(caseID.length() != 6)
            {
                ctx.reply(CommandContext.ERROR+" The Case-ID must be 6 characters long!").setEphemeral(true).queue();
                return;
            }
            Case modcase = Punishments.getCaseByID(caseID, g.getIdLong());
            if(modcase == null)
            {
                ctx.reply(CommandContext.ERROR+" The case with the ID \""+caseID.toUpperCase()+"\" does not exist!").setEphemeral(true).queue();
                return;
            }
            modcase.setReason(reason);
            ctx.reply(CommandContext.SUCCESS+" Reason for case \""+caseID.toUpperCase()+"\" has been changed to `"+reason+"`!").setEphemeral(true).queue();
            return;
        }else if(event.getSubcommandName().equals("delete"))
        {
            String caseID = event.getOption("case").getAsString();
            if(caseID.length() != 6)
            {
                ctx.reply(CommandContext.ERROR+" The Case-ID must be 6 characters long!").setEphemeral(true).queue();
                return;
            }
            Case modcase = Punishments.getCaseByID(caseID, g.getIdLong());
            if(modcase == null)
            {
                ctx.reply(CommandContext.ERROR+" The case with the ID \""+caseID.toUpperCase()+"\" does not exist!").setEphemeral(true).queue();
                return;
            }
            boolean x = modcase.deleteCase();
            if(x)
            {
                ctx.reply(CommandContext.SUCCESS+" Case #"+modcase.getCaseID()+" has been deleted!").setEphemeral(true).queue();

            }else
            {
                ctx.reply(CommandContext.ERROR+" Case #"+modcase.getCaseID()+" could not be deleted!").setEphemeral(true).queue();
            }
        }
    }
}
