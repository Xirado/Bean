package at.Xirado.Bean.Commands.Moderation;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Logging.Console;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.PunishmentManager.Case;
import at.Xirado.Bean.PunishmentManager.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;

public class BanCommand extends Command
{
    public BanCommand(JDA jda)
    {
        super(jda);
        this.invoke = "ban";
        this.commandType = CommandType.MODERATION;
        this.neededPermissions = Arrays.asList(Permission.BAN_MEMBERS);
        this.neededBotPermissions = Arrays.asList(Permission.BAN_MEMBERS);
        this.description = "permanently bans an user from the server";
        this.usage = "ban [@Mention/ID] (optional reason)";
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
        if(target_ID.length() == 0)
        {
            event.replyError("ID may not be empty!");
            return;
        }
        DiscordBot.getInstance().jda.retrieveUserById(target_ID).queue(
                (target_User) ->
                {
                    boolean userIsInCurrentGuild = guild.isMember(target_User);
                    if(userIsInCurrentGuild)
                    {
                        guild.retrieveMember(target_User).queue(
                                (target_Member) ->
                                {
                                    if(!senderMember.canInteract(target_Member))
                                    {
                                        event.replyError("You cannot ban this member!");
                                        return;
                                    }
                                    if(!event.getSelfMember().canInteract(target_Member))
                                    {
                                        event.replyError("I cannot ban this member!");
                                        return;
                                    }
                                    if(DiscordBot.getInstance().permissionCheckerManager.isModerator(target_Member))
                                    {
                                        event.replyError("You cannot ban a moderator!");
                                        return;
                                    }
                                },
                                (error) ->
                                {
                                    Console.logger.error("Could not retrieve member!", error);
                                    event.replyError("Could not retrieve member!");
                                    return;
                                }
                        );
                    }
                    guild.retrieveBan(target_User).queue(
                            (ban) -> // User is banned
                            {
                                event.replyError("This user is already banned");
                                return;
                            },
                            new ErrorHandler()
                                .handle(ErrorResponse.UNKNOWN_BAN, (err) -> // User is not banned
                                        {
                                            boolean withReason = args.length > 1;
                                            final String Reason = withReason ? event.getArguments().getAsString(1) : "No reason specified";
                                            target_User.openPrivateChannel().queue(
                                                    (privateChannel -> {
                                                        EmbedBuilder builder = new EmbedBuilder()
                                                                .setColor(CaseType.BAN.getEmbedColor())
                                                                .setAuthor("You have been banned from "+guild.getName()+"!")
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
                                            guild.ban(target_User, 0, Reason).queue(
                                                    (success) ->
                                                    {
                                                        Case bancase = Case.createCase(CaseType.BAN, guild.getIdLong(), target_User.getIdLong(), senderMember.getIdLong(), Reason, -1);
                                                        if(event.hasLogChannel())
                                                        {
                                                            EmbedBuilder builder = new EmbedBuilder()
                                                                    .setColor(0x8b0000)
                                                                    .setDescription(CommandEvent.SUCCESS_EMOTE +" "+target_User.getAsTag()+" has been banned")
                                                                    .setFooter("Case #"+bancase.getCaseID()+" ("+Reason+")");
                                                            event.reply(builder.build());
                                                        }
                                                        EmbedBuilder builder = new EmbedBuilder()
                                                                .setTimestamp(Instant.now())
                                                                .setColor(0x8b0000)
                                                                .setThumbnail(target_User.getEffectiveAvatarUrl())
                                                                .setFooter("Target ID: "+target_User.getIdLong())
                                                                .setTitle("Ban | Case #"+bancase.getCaseID())
                                                                .addField("Target", target_User.getAsMention()+" ("+target_User.getAsTag()+")", true)
                                                                .addField("Moderator", senderMember.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                                                                .addField("Reason", Reason, false);
                                                        if(!withReason)
                                                        {
                                                            builder.addField("", "Use `"+DiscordBot.getInstance().prefixManager.getPrefix(guild.getIdLong())+"case "+bancase.getCaseID()+" reason [Reason]`\n to add a reason to this ban.", false);

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
                                                        event.replyError("For some reason, this user cannot be banned...");
                                                        Console.logger.error("Could not ban user!", error);

                                                    }
                                            );

                                        })
                    );
                },
                (error) ->
                {
                    event.replyError("This user does not exist.");
                }
        );

    }
}
