package at.xirado.bean.commands.Moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
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
    public void executeCommand(CommandEvent event)
    {
        Guild guild = event.getGuild();
        Member senderMember = event.getMember();
        String[] args = event.getArguments().toStringArray();
        if(args.length < 1)
        {
            event.replyErrorUsage();
            return;
        }
        String target_ID = args[0].replaceAll("[^0-9]", "");
        guild.retrieveMemberById(target_ID).queue(
                (target_Member) ->
                {
                    if(!senderMember.canInteract(target_Member))
                    {
                        event.replyError("You cannot softban this member!");
                        return;
                    }
                    if(!event.getSelfMember().canInteract(target_Member))
                    {
                        event.replyError("I cannot softban this member!");
                        return;
                    }
                    if(DiscordBot.getInstance().permissionCheckerManager.isModerator(target_Member))
                    {
                        event.replyError("You cannot softban a moderator!");
                        return;
                    }
                    boolean withReason = args.length > 1;
                    final String Reason = withReason ? event.getArguments().toString(1) : "No reason specified";
                    User target_User = target_Member.getUser();
                    target_User.openPrivateChannel().queue(
                            (privateChannel -> {
                                EmbedBuilder builder = new EmbedBuilder()
                                        .setColor(0xffa500)
                                        .setAuthor("You have been softbanned from "+guild.getName()+"!", null, guild.getIconUrl())
                                        .addField("Reason", Reason, true)
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
                                                return;
                                            }
                                            if(event.hasLogChannel())
                                            {
                                                EmbedBuilder builder = new EmbedBuilder()
                                                        .setColor(0xffa500)
                                                        .setDescription(CommandEvent.SUCCESS_EMOTE +" "+target_User.getAsTag()+" has been softbanned")
                                                        .setFooter("Case #"+modcase.getCaseID()+" ("+Reason+")");
                                                event.reply(builder.build());
                                            }
                                            EmbedBuilder builder = new EmbedBuilder()
                                                    .setTimestamp(Instant.now())
                                                    .setColor(0xffa500)
                                                    .setThumbnail(target_User.getEffectiveAvatarUrl())
                                                    .setFooter("Target ID: "+target_User.getIdLong())
                                                    .setTitle("Softban | Case #"+modcase.getCaseID())
                                                    .addField("Target", target_User.getAsMention()+" ("+target_User.getAsTag()+")", true)
                                                    .addField("Moderator", senderMember.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                                                    .addField("Reason", Reason, false);
                                            if(!withReason)
                                            {
                                                builder.addField("", "Use `"+DiscordBot.getInstance().prefixManager.getPrefix(guild.getIdLong())+"reason "+modcase.getCaseID()+" [Reason]` to add a reason to this softban.", false);

                                            }
                                            if(!event.hasLogChannel())
                                            {
                                                event.reply(builder.build());
                                            }else
                                            {
                                                event.replyinLogChannel(builder.build());
                                            }
                                        },
                                        (error) ->
                                        {
                                            event.replyError("Error: Could not unban user. (Still banned)");
                                        }
                                );
                            },
                            (error) ->
                            {
                                event.replyError("Could not softban this user");

                            }
                    );
                },
                new ErrorHandler()
                .handle(ErrorResponse.UNKNOWN_MEMBER, (err) ->
                {
                    event.replyError("The specified user is not a member of this guild!");
                })
                .handle(ErrorResponse.UNKNOWN_USER, (err) ->
                {
                    event.replyError("This user does not exist!");
                })
        );
    }
}
