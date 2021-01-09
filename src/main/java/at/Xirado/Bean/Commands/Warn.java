package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.PunishmentManager.Case;
import at.Xirado.Bean.PunishmentManager.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;

public class Warn extends Command
{
    public Warn(JDA jda)
    {
        super(jda);
        this.invoke = "warn";
        this.neededPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.commandType = CommandType.MODERATION;
        this.description = "warns a user";
        this.usage = "warn [@User/ID] (Optional Reason)";
    }

    @Override
    public void execute(CommandEvent event)
    {
        Member member = event.getMember();
        User user = event.getAuthor();
        Guild guild = event.getGuild();
        String[] args = event.getArguments().getArguments();
        if(args.length < 1)
        {
            event.replyErrorUsage();
            return;
        }
        final String ID = args[0].replaceAll("[^0-9]", "");
        final String Reason = event.getArguments().getAsString(1);
        DiscordBot.instance.jda.retrieveUserById(ID).queue(
                (target) ->
                {
                    Case modcase = Case.createCase(CaseType.WARN, event.getGuild().getIdLong(), target.getIdLong(), member.getIdLong(), Reason, 0L);
                    EmbedBuilder builder = new EmbedBuilder()
                            .setThumbnail(target.getEffectiveAvatarUrl())
                            .setColor(CaseType.WARN.getEmbedColor())
                            .setTitle("[Warn] "+target.getAsTag())
                            .setTimestamp(Instant.now())
                            .setFooter("User-ID: "+target.getIdLong())
                            .addField("User", target.getAsMention(), true)
                            .addField("Moderator", member.getAsMention(), true)
                            .addField("Reason", Reason, true)
                            .setDescription("Case `"+modcase.getCaseID()+"`");
                    event.reply(new EmbedBuilder()
                            .setColor(CaseType.WARN.getEmbedColor())
                            .setDescription("✅ "+target.getAsTag()+" has been warned")
                            .setFooter("User-ID: "+target.getIdLong())
                            .setTimestamp(Instant.now())
                            .build()
                    );
                    event.replyinLogChannel(builder.build());

                },
                (failure) ->
                {
                    event.replyError("Invalid User-ID!");
                }
        );


    }
}
