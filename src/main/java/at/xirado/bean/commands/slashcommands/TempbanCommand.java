package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.*;
import at.xirado.bean.translation.FormattedDuration;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.Util;
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
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TempbanCommand extends SlashCommand
{

    public TempbanCommand()
    {
        setCommandData(new CommandUpdateAction.CommandData("tempban", "temporarily bans a user from this guild")
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.USER, "user", "the user to ban")
                        .setRequired(true))
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.INTEGER, "time", "the duration of the ban")
                        .setRequired(true))
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "timeunit", "the duration of the ban")
                        .addChoice("seconds", "s")
                        .addChoice("minutes", "m")
                        .addChoice("hours", "h")
                        .addChoice("days", "d")
                        .addChoice("weeks", "w")
                        .setRequired(true)
                )
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "reason", "the reason for this ban")
                        .setRequired(false))
        );

        setNeededUserPermissions(Arrays.asList(Permission.BAN_MEMBERS));
        setNeededBotPermissions(Arrays.asList(Permission.BAN_MEMBERS));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull CommandContext ctx)
    {
        Guild guild = event.getGuild();
        Member targetMember = event.getOption("user").getAsMember();
        if(targetMember == null)
        {
            ctx.reply(CommandContext.ERROR+" This user is not in this guild!").setEphemeral(true).queue();
            return;
        }
        Long time;
        try
        {
            time = FormattedDuration.parsePeriod(event.getOption("time").getAsLong()+event.getOption("timeunit").getAsString());
        } catch (Exception e)
        {
            ctx.reply(CommandContext.ERROR+" This is not a valid time-format!").setEphemeral(true).queue();
            return;
        }
        if(time == null)
        {
            ctx.reply(CommandContext.ERROR+" This is not a valid time-format!").setEphemeral(true).queue();
            return;
        }
        if(!sender.canInteract(targetMember))
        {
            ctx.reply(CommandContext.ERROR+" You cannot ban this member!").setEphemeral(true).queue();
            return;
        }
        if(!event.getGuild().getSelfMember().canInteract(targetMember))
        {
            ctx.reply(CommandContext.ERROR+" I cannot ban this member!").setEphemeral(true).queue();
            return;
        }
        if(DiscordBot.getInstance().permissionCheckerManager.isModerator(targetMember))
        {
            ctx.reply(CommandContext.ERROR+" You cannot ban a moderator!").setEphemeral(true).queue();
            return;
        }
        boolean withReason = event.getOption("reason") != null;
        final String Reason = withReason ? event.getOption("reason").getAsString() : ctx.getLocalized("commands.noreason");
        User targetUser = targetMember.getUser();
        long channelid = event.getChannel().getIdLong();
        guild.ban(targetMember, 0, Reason).queue(
                (success) ->
                {
                    Case modcase = Case.createCase(CaseType.TEMPBAN, guild.getIdLong(), targetMember.getIdLong(), sender.getIdLong(), Reason, time);
                    Runnable r = () ->
                    {
                        Punishments.unban(modcase, DiscordBot.getInstance().jda.getTextChannelById(channelid));

                    };
                    DiscordBot.getInstance().scheduledExecutorService.schedule(r, time, TimeUnit.MILLISECONDS);
                    ctx.reply(CommandContext.SUCCESS+" "+targetMember.getAsMention()+" has been banned!\n`"+"Case #"+modcase.getCaseID()+" ("+Reason+")`").setEphemeral(true).queue();
                    TextChannel logChannel = DiscordBot.getInstance().logChannelManager.getLogChannel(guild.getIdLong());
                    if(logChannel != null)
                    {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setTimestamp(Instant.now())
                                .setColor(0x8b0000)
                                .setThumbnail(targetUser.getEffectiveAvatarUrl())
                                .setFooter("Target ID: "+targetUser.getIdLong())
                                .setTitle("Ban | Case #"+modcase.getCaseID())
                                .addField("banned", targetUser.getAsMention()+" ("+targetUser.getAsTag()+")", true)
                                .addField("moderator", sender.getAsMention()+" ("+sender.getUser().getAsTag()+")", true)
                                .addField("reason", Reason, false)
                                .addField("duration", ctx.parseDuration(time/1000, " "), true);
                        if(!withReason)
                        {
                            builder.addField("", "Use `/case reason "+modcase.getCaseID()+"`\n to add a reason to this ban.", false);
                        }
                        logChannel.sendMessage(builder.build()).queue();
                    }
                },
                (error) -> ctx.reply(CommandContext.ERROR+" Could not ban this user!").setEphemeral(true).queue()
        );
    }
}
