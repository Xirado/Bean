package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.SlashCommand;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;

public class BanCommand extends SlashCommand
{

    public BanCommand()
    {
        setCommandData(new CommandUpdateAction.CommandData("ban", "permanently bans a user from this guild")
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.USER, "user", "the user to ban").setRequired(true))
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "reason", "the reason for this ban").setRequired(false))
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.INTEGER, "delDays", "how many days of messages to delete").setRequired(false))
        );
        setNeededUserPermissions(Collections.singletonList(Permission.BAN_MEMBERS));
        setNeededBotPermissions(Collections.singletonList(Permission.BAN_MEMBERS));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull CommandContext ctx)
    {
        Guild g = event.getGuild();
        if(g == null) return;
        User targetUser = event.getOption("user").getAsUser();
        Member targetMember = event.getOption("user").getAsMember();
        String reason = event.getOption("reason") == null ? null : event.getOption("reason").getAsString();
        int deldays = event.getOption("delDays") != null ? (int) Math.max(0, Math.min(7, event.getOption("delDays").getAsLong())) : 0;
        if(targetMember != null)
        {
            if(sender.getIdLong() == targetMember.getIdLong())
            {
                ctx.reply("You cannot ban yourself.").setEphemeral(true).queue();
                return;
            }

            if (!sender.canInteract(targetMember))
            {
                ctx.reply(CommandContext.DENY+" You cannot ban this member!").setEphemeral(true).queue();
                return;
            }

            if(DiscordBot.getInstance().permissionCheckerManager.isModerator(targetMember))
            {
                event.reply(CommandContext.DENY+" You cannot ban a moderator!").setEphemeral(true).queue();
                return;
            }

            if (!g.getSelfMember().canInteract(targetMember))
            {
                ctx.reply(CommandContext.DENY+" I cannot interact with this member!").setEphemeral(true).queue();
                return;
            }
        }
        Case bancase = Case.createCase(CaseType.BAN, g.getIdLong(), targetUser.getIdLong(), sender.getIdLong(), reason != null ? reason : "No reason specified", -1);
        if(bancase == null)
        {
            ctx.reply(CommandContext.ERROR+" An error occured.").setEphemeral(true).queue();
            return;
        }
        try
        {
            PrivateChannel privateChannel = targetUser.openPrivateChannel().complete();
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(CaseType.BAN.getEmbedColor())
                    .setAuthor("You have been banned from "+g.getName()+"!")
                    .addField("reason", bancase.getReason(), true)
                    .addField("moderator", sender.getAsMention() + "("+sender.getUser().getAsTag()+")", true);
            privateChannel.sendMessage(builder.build()).complete();
        }catch (Exception ignored)
        {

        }
        g.ban(targetUser, deldays, bancase.getReason()).queue(
                (success) ->
                {
                    ctx.reply(CommandContext.SUCCESS+" "+targetUser.getAsMention()+" has been banned.\n`Reason: "+bancase.getReason()+" (#"+bancase.getCaseID()+")`").setEphemeral(true).queue();
                    TextChannel logchannel = DiscordBot.getInstance().logChannelManager.getLogChannel(g.getIdLong());
                    EmbedBuilder builder2 = new EmbedBuilder()
                            .setTimestamp(Instant.now())
                            .setColor(0x8b0000)
                            .setThumbnail(targetUser.getEffectiveAvatarUrl())
                            .setFooter("Target ID: "+targetUser.getIdLong())
                            .setTitle("Ban | Case #"+bancase.getCaseID())
                            .addField("banned", targetUser.getAsMention()+" ("+targetUser.getAsTag()+")", true)
                            .addField("moderator", sender.getAsMention()+" ("+sender.getUser().getAsTag()+")", true)
                            .addField("reason", bancase.getReason(), false);
                    Objects.requireNonNullElseGet(logchannel, event::getChannel).sendMessage(builder2.build()).queue();
                },
                (error) ->
                {
                    ctx.reply(CommandContext.ERROR + " An error occured").setEphemeral(true).queue();
                }
        );
    }
}
