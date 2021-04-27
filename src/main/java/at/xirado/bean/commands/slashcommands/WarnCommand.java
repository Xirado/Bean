package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.SlashCommand;
import at.xirado.bean.commandmanager.SlashCommandContext;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

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
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        Guild g = event.getGuild();
        if(!permissionCheckerManager.isModerator(sender) && !sender.hasPermission(Permission.ADMINISTRATOR))
        {
            ctx.reply(SlashCommandContext.DENY+" "+ctx.getLocalized("general.no_perms")).setEphemeral(true).queue();
            return;
        }
        Member targetMember = event.getOption("member").getAsMember();
        if(targetMember == null)
        {
            ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("commands.user_not_in_guild")).setEphemeral(true).queue();
            return;
        }
        String Reason = event.getOption("reason") == null ? ctx.getLocalized("commands.noreason") : event.getOption("reason").getAsString();
        boolean withReason = event.getOption("reason") != null;
        if(!sender.canInteract(targetMember))
        {
            ctx.reply(SlashCommandContext.DENY+" "+ctx.getLocalized("commands.warn.you_cannot_warn")).setEphemeral(true).queue();
            return;
        }
        if(permissionCheckerManager.isModerator(targetMember) || targetMember.hasPermission(Permission.ADMINISTRATOR))
        {
            ctx.reply(SlashCommandContext.DENY+" "+ctx.getLocalized("commands.warn.you_cannot_warn_moderator")).setEphemeral(true).queue();
            return;
        }
        Case modcase = Case.createCase(CaseType.WARN, g.getIdLong(), targetMember.getIdLong(), sender.getIdLong(), Reason, 0);
        if(modcase == null)
        {
            ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("general.unknown_error_occured")).setEphemeral(true).queue();
            return;
        }
        targetMember.getUser().openPrivateChannel().queue(
                (privateChannel) ->
                {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(CaseType.WARN.getEmbedColor())
                            .setAuthor(ctx.getLocalized("commands.warn.you_have_been_warned", g.getName()), null, g.getIconUrl())
                            .addField(ctx.getLocalized("commands.reason"), Reason, true)
                            .addField("Moderator", sender.getUser().getAsTag(), true);
                    privateChannel.sendMessage(builder.build()).queue(success -> {}, error -> {});
                }, (e) -> {});

        ctx.reply(SlashCommandContext.SUCCESS+" "+ctx.getLocalized("commands.warn.has_been_warned", targetMember.getUser().getAsMention())+"\n`"+ctx.getLocalized("commands.reason")+": "+modcase.getReason()+" (#"+modcase.getCaseID()+")`").setEphemeral(true).queue();

        EmbedBuilder mainembed = new EmbedBuilder()
                .setThumbnail(targetMember.getUser().getEffectiveAvatarUrl())
                .setColor(CaseType.WARN.getEmbedColor())
                .setTimestamp(Instant.now())
                .setFooter(ctx.getLocalized("commands.target_id")+": "+targetMember.getIdLong())
                .setTitle("Warn | Case #"+modcase.getCaseID())
                .addField(ctx.getLocalized("commands.target"), targetMember.getAsMention()+" ("+targetMember.getUser().getAsTag()+")", true)
                .addField("Moderator", sender.getAsMention()+" ("+sender.getUser().getAsTag()+")", true)
                .addField(ctx.getLocalized("commands.reason"), Reason, false);
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
}
