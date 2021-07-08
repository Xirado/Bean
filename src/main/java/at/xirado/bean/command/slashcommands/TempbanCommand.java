package at.xirado.bean.command.slashcommands;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.moderation.Case;
import at.xirado.bean.moderation.CaseType;
import at.xirado.bean.moderation.Punishments;
import at.xirado.bean.translation.FormattedDuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class TempbanCommand extends SlashCommand
{

    public TempbanCommand()
    {
        setCommandData(new CommandData("tempban", "temporarily bans a user from this guild")
                .addOption(OptionType.USER, "user", "the user to ban", true)
                .addOption(OptionType.INTEGER, "time", "the duration of the ban", true)
                .addOptions(new OptionData(OptionType.STRING, "timeunit", "the duration of the ban")
                        .addChoice("seconds", "s")
                        .addChoice("minutes", "m")
                        .addChoice("hours", "h")
                        .addChoice("days", "d")
                        .addChoice("weeks", "w")
                        .setRequired(true)
                )
                .addOption(OptionType.STRING, "reason", "the reason for this ban", false)
        );

        setRequiredUserPermissions(Permission.BAN_MEMBERS);
        setRequiredBotPermissions(Permission.BAN_MEMBERS);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Guild guild = event.getGuild();
        Member targetMember = event.getOption("user").getAsMember();
        if (targetMember == null)
        {
            ctx.reply(SlashCommandContext.ERROR + " This user is not in this guild!").setEphemeral(true).queue();
            return;
        }
        Long time;
        try
        {
            time = FormattedDuration.parsePeriod(event.getOption("time").getAsLong() + event.getOption("timeunit").getAsString());
        } catch (Exception e)
        {
            ctx.reply(SlashCommandContext.ERROR + " This is not a valid time-format!").setEphemeral(true).queue();
            return;
        }
        if (time == null)
        {
            ctx.reply(SlashCommandContext.ERROR + " This is not a valid time-format!").setEphemeral(true).queue();
            return;
        }
        if (!sender.canInteract(targetMember))
        {
            ctx.reply(SlashCommandContext.ERROR + " You cannot ban this member!").setEphemeral(true).queue();
            return;
        }
        if (!event.getGuild().getSelfMember().canInteract(targetMember))
        {
            ctx.reply(SlashCommandContext.ERROR + " I cannot ban this member!").setEphemeral(true).queue();
            return;
        }
        if (ctx.getGuildData().isModerator(targetMember))
        {
            ctx.reply(SlashCommandContext.ERROR + " You cannot ban a moderator!").setEphemeral(true).queue();
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
                        Punishments.unban(modcase, Bean.getInstance().getJDA().getTextChannelById(channelid));

                    };
                    Bean.getInstance().getExecutor().schedule(r, time, TimeUnit.MILLISECONDS);
                    ctx.reply(SlashCommandContext.SUCCESS + " " + targetMember.getAsMention() + " has been banned!\n`" + "Case #" + modcase.getCaseID() + " (" + Reason + ")`").setEphemeral(true).queue();
                    TextChannel logChannel = ctx.getGuildData().getLogChannel();
                    if (logChannel != null)
                    {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setTimestamp(Instant.now())
                                .setColor(0x8b0000)
                                .setThumbnail(targetUser.getEffectiveAvatarUrl())
                                .setFooter("Target ID: " + targetUser.getIdLong())
                                .setTitle("Ban | Case #" + modcase.getCaseID())
                                .addField("banned", targetUser.getAsMention() + " (" + targetUser.getAsTag() + ")", true)
                                .addField("moderator", sender.getAsMention() + " (" + sender.getUser().getAsTag() + ")", true)
                                .addField("reason", Reason, false)
                                .addField("duration", ctx.parseDuration(time / 1000, " "), true);
                        if (!withReason)
                        {
                            builder.addField("", "Use `/case reason " + modcase.getCaseID() + "`\n to add a reason to this ban.", false);
                        }
                        logChannel.sendMessage(builder.build()).queue();
                    }
                },
                (error) -> ctx.reply(SlashCommandContext.ERROR + " Could not ban this user!").setEphemeral(true).queue()
        );
    }
}
