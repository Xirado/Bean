package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.SlashCommand;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.commands.CommandHook;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

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
    public void executeCommand(@NotNull SlashCommandEvent event, @NotNull Member sender, @NotNull CommandContext ctx)
    {
        Guild g = event.getGuild();
        if(g == null) return;
        User targetUser = event.getOption("user").getAsUser();
        Member targetMember = event.getOption("user").getAsMember();
        String reason = event.getOption("reason") == null ? null : event.getOption("reason").getAsString();
        int deldays = event.getOption("delDays") != null ? (int) Math.max(0, Math.min(7, event.getOption("delDays").getAsLong())) : 0;
        if(targetMember != null)
        {
            if (!sender.canInteract(targetMember))
            {
                ctx.reply("You cannot ban this member!").setEphemeral(true).queue();
                return;
            }
            if (!g.getSelfMember().canInteract(targetMember))
            {
                ctx.reply("I cannot interact with this member!").setEphemeral(true).queue();
                return;
            }
        }
        Case bancase = Case.createCase(CaseType.BAN, g.getIdLong(), targetUser.getIdLong(), sender.getIdLong(), reason != null ? reason : "No reason specified", -1);
        if(bancase == null)
        {
            ctx.reply("An error occured, please try again later.").setEphemeral(true).queue();
            return;
        }
        try
        {
            PrivateChannel privateChannel = targetUser.openPrivateChannel().complete();
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(CaseType.BAN.getEmbedColor())
                    .setAuthor("You have been banned from "+g.getName()+"!")
                    .addField("Reason", bancase.getReason(), true)
                    .addField("Moderator", sender.getUser().getAsTag(), true);
            privateChannel.sendMessage(builder.build()).complete();
        }catch (Exception ignored)
        {

        }
        g.ban(targetUser, deldays, bancase.getReason()).queue(
                (success) ->
                {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(0x8b0000)
                            .setDescription(targetUser.getAsMention()+" has been banned!")
                            .setFooter("Case #"+bancase.getCaseID()+" ("+bancase.getReason()+")");
                    ctx.reply(builder.build()).queue();
                    TextChannel logchannel = DiscordBot.getInstance().logChannelManager.getLogChannel(g.getIdLong());
                    EmbedBuilder builder2 = new EmbedBuilder()
                            .setTimestamp(Instant.now())
                            .setColor(0x8b0000)
                            .setThumbnail(targetUser.getEffectiveAvatarUrl())
                            .setFooter("Target ID: "+targetUser.getIdLong())
                            .setTitle("Ban | Case #"+bancase.getCaseID())
                            .addField("Target", targetUser.getAsMention()+" ("+targetUser.getAsTag()+")", true)
                            .addField("Moderator", sender.getAsMention()+" ("+sender.getUser().getAsTag()+")", true)
                            .addField("Reason", bancase.getReason(), false);
                    if(logchannel != null) logchannel.sendMessage(builder2.build()).queue();
                },
                (error) ->
                {
                    ctx.reply("An error occured").setEphemeral(true).queue();
                }
        );
    }
}
