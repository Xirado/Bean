package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;

public class SoftBanCommand extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(SoftBanCommand.class);

    public SoftBanCommand(JDA jda)
    {
        super(jda);
        this.invoke = "softban";
        this.commandType = CommandType.MODERATION;
        this.neededPermissions = Arrays.asList(Permission.BAN_MEMBERS);
        this.neededBotPermissions = Arrays.asList(Permission.BAN_MEMBERS);
        this.description = "softbans an user from the server (Kick + delete recent messages)";
        this.usage = "softban [@Mention/ID] (optional reason)";
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        Guild guild = event.getGuild();
        Member senderMember = context.getMember();
        String[] args = context.getArguments().toStringArray();
        if(args.length < 1)
        {
            context.replyErrorUsage();
            return;
        }

        String target_ID = args[0].replaceAll("[^0-9]", "");
        if(target_ID.length() == 0)
        {
            context.replyError(context.getLocalized("commands.id_empty"));
            return;
        }
        guild.retrieveMemberById(target_ID).queue(
                (target_Member) ->
                {
                    if(!senderMember.canInteract(target_Member))
                    {
                        context.replyError(context.getLocalized("commands.softban.you_cannot_softban_this_member"));
                        return;
                    }
                    if(!event.getGuild().getSelfMember().canInteract(target_Member))
                    {
                        context.replyError(context.getLocalized("commands.softban.i_cannot_softban_this_member"));
                        return;
                    }
                    if(DiscordBot.getInstance().permissionCheckerManager.isModerator(target_Member))
                    {
                        context.replyError(context.getLocalized("commands.softban.cannot_softban_moderator"));
                        return;
                    }
                    boolean withReason = args.length > 1;
                    final String Reason = withReason ? context.getArguments().toString(1) : context.getLocalized("commands.noreason");
                    User target_User = target_Member.getUser();
                    target_User.openPrivateChannel().queue(
                            (privateChannel -> {
                                EmbedBuilder builder = new EmbedBuilder()
                                        .setColor(0xffa500)
                                        .setAuthor(context.getLocalized("commands.softban.you_have_been_softbanned", guild.getName()), null, guild.getIconUrl())
                                        .addField(context.getLocalized("commands.reason"), Reason, true)
                                        .addField("Moderator", senderMember.getUser().getAsTag(), true);
                                privateChannel.sendMessage(builder.build()).queue(
                                        (success1) ->
                                        {

                                        },
                                        (error) ->
                                        {

                                        }
                                );
                            })
                    );
                    guild.ban(target_Member, 7, "Softban ("+Reason+")").queue(
                            (banned) ->
                            {
                                guild.unban(target_ID).queue(
                                        (unbanned) ->
                                        {

                                            Case modcase = Case.createCase(CaseType.SOFTBAN, guild.getIdLong(), target_User.getIdLong(), senderMember.getIdLong(), Reason, 0);
                                            if(modcase == null)
                                            {
                                                logger.error("Could not create modcase!", new Exception());
                                                context.replyError(context.getLocalized("general.unknown_error_occured"));
                                                return;
                                            }
                                            if(context.hasLogChannel())
                                            {
                                                EmbedBuilder builder = new EmbedBuilder()
                                                        .setColor(0xffa500)
                                                        .setDescription(CommandContext.SUCCESS_EMOTE +" "+context.getLocalized("commands.softban.has_been_softbanned", target_User.getAsTag()))
                                                        .setFooter("Case #"+modcase.getCaseID()+" ("+Reason+")");
                                                context.reply(builder.build());
                                            }
                                            EmbedBuilder builder = new EmbedBuilder()
                                                    .setTimestamp(Instant.now())
                                                    .setColor(0xffa500)
                                                    .setThumbnail(target_User.getEffectiveAvatarUrl())
                                                    .setFooter(context.getLocalized("commands.target_id")+": "+target_User.getIdLong())
                                                    .setTitle("Softban | Case #"+modcase.getCaseID())
                                                    .addField(context.getLocalized("commands.target"), target_User.getAsMention()+" ("+target_User.getAsTag()+")", true)
                                                    .addField("Moderator", senderMember.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                                                    .addField(context.getLocalized("commands.reason"), Reason, false);
                                            if(!withReason)
                                            {
                                                builder.addField("", "Use `"+DiscordBot.getInstance().prefixManager.getPrefix(guild.getIdLong())+"reason "+modcase.getCaseID()+" [Reason]` to add a reason to this softban.", false);

                                            }
                                            if(!context.hasLogChannel())
                                            {
                                                context.reply(builder.build());
                                            }else
                                            {
                                                context.replyInLogChannel(builder.build());
                                            }
                                        },
                                        (error) ->
                                        {
                                            context.replyError(context.getLocalized("commands.softban.could_not_unban"));
                                        }
                                );
                            },
                            (error) ->
                            {
                                context.replyError(context.getLocalized("could_not_softban_user"));

                            }
                    );
                },
                new ErrorHandler()
                .handle(ErrorResponse.UNKNOWN_MEMBER, (err) ->
                {
                    context.replyError(context.getLocalized("commands.user_not_in_guild"));
                })
                .handle(ErrorResponse.UNKNOWN_USER, (err) ->
                {
                    context.replyError(context.getLocalized("commands.user_not_exists"));
                })
        );
    }
}
