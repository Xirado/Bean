package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.*;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import at.xirado.bean.punishmentmanager.Punishments;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class WarnCommand extends SlashCommand
{
    public WarnCommand()
    {
        setCommandData(new CommandUpdateAction.CommandData("warn", "warns a member")
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.USER, "member", "the member to warn").setRequired(true))
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "reason", "the reason for this warn").setRequired(false))
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull CommandContext ctx)
    {
        Member m = event.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        Guild g = event.getGuild();
        if(!permissionCheckerManager.isModerator(m) && !m.hasPermission(Permission.ADMINISTRATOR))
        {
            ctx.reply(CommandContext.DENY+" You don't have permission to do this!").setEphemeral(true).queue();
            return;
        }
        Member targetMember = event.getOption("member").getAsMember();
        if(targetMember == null)
        {
            ctx.reply(CommandContext.ERROR+" This user is not on this server!").setEphemeral(true).queue();
            return;
        }
        String Reason = event.getOption("reason") == null ? "No reason specified" : event.getOption("reason").getAsString();
        boolean withReason = event.getOption("reason") != null;
        if(!m.canInteract(targetMember))
        {
            ctx.reply(CommandContext.DENY+" You cannot warn this member!").setEphemeral(true).queue();
            return;
        }
        if(permissionCheckerManager.isModerator(targetMember) || targetMember.hasPermission(Permission.ADMINISTRATOR))
        {
            ctx.reply(CommandContext.DENY+" You cannot warn a moderator!").setEphemeral(true).queue();
            return;
        }
        Case modcase = Case.createCase(CaseType.WARN, g.getIdLong(), targetMember.getIdLong(), m.getIdLong(), Reason, 0);
        if(modcase == null)
        {
            ctx.reply(CommandContext.ERROR+" An error occured! Please try again later.").setEphemeral(true).queue();
            return;
        }
        targetMember.getUser().openPrivateChannel().queue(
                (privateChannel) ->
                {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(CaseType.WARN.getEmbedColor())
                            .setAuthor("You have been warned on "+g.getName()+"!", null, g.getIconUrl())
                            .addField("Reason", Reason, true)
                            .addField("Moderator", m.getUser().getAsTag(), true);
                    privateChannel.sendMessage(builder.build()).queue(success -> {}, error -> {});
                }, (e) -> {});

        ctx.reply(CommandContext.SUCCESS+" "+targetMember.getUser().getAsMention()+" has been warned.\n`Reason: "+modcase.getReason()+" (#"+modcase.getCaseID()+")`").setEphemeral(true).queue();

        EmbedBuilder mainembed = new EmbedBuilder()
                .setThumbnail(targetMember.getUser().getEffectiveAvatarUrl())
                .setColor(CaseType.WARN.getEmbedColor())
                .setTimestamp(Instant.now())
                .setFooter("Target ID: "+targetMember.getIdLong())
                .setTitle("Warn | Case #"+modcase.getCaseID())
                .addField("Target", targetMember.getAsMention()+" ("+targetMember.getUser().getAsTag()+")", true)
                .addField("Moderator", m.getAsMention()+" ("+m.getUser().getAsTag()+")", true)
                .addField("Reason", Reason, false);
        if(!withReason)
        {
            mainembed.addField("", "Use `"+DiscordBot.getInstance().prefixManager.getPrefix(g.getIdLong())+"case "+modcase.getCaseID()+" reason [Reason]`\n to add a reason to this warn.", false);
        }
        TextChannel logChannel = DiscordBot.getInstance().logChannelManager.getLogChannel(g.getIdLong());
        if(logChannel != null)
        {
            logChannel.sendMessage(mainembed.build()).queue(s -> {}, e -> {});
        }
    }

    /**
     * HTL-Server
     * @param m Member
     */
    public static void doWarnPolicy(Member m)
    {
        List<Case> cases = Punishments.getAllWarns(m);
        if(cases == null || cases.isEmpty()) return;
        int warnsLessThan100DaysOld = 0;
        for(Case modcase : cases)
        {
            if(!modcase.isActive()) continue;
            if(modcase.getCreatedAt() > System.currentTimeMillis()-8640000000L)
            {
                warnsLessThan100DaysOld++;
            }
        }
        switch (warnsLessThan100DaysOld)
        {
            case 0:
                return;
            case 1:

        }
    }
}
