package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Language.FormattedDuration;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.PunishmentManager.Case;
import at.Xirado.Bean.PunishmentManager.CaseType;
import at.Xirado.Bean.PunishmentManager.Punishments;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;
import java.util.List;

public class Warns extends Command
{
    public Warns(JDA jda)
    {
        super(jda);
        this.invoke = "warns";
        this.commandType = CommandType.MODERATION;
        this.description = "Lists all the warns of a member";
        this.neededPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.aliases = new String[]{"infractions", "modlog", "modlogs"};
        this.usage = "warns [@User/ID]";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        String[] args = event.getArguments().getArguments();
        Guild guild = event.getGuild();
        Member member = event.getMember();
        if(args.length < 1)
        {
            event.replyErrorUsage();
            return;
        }
        final String ID = args[0].replaceAll("[^0-9]", "");
        DiscordBot.instance.jda.retrieveUserById(ID).queue(
                (target) ->
                {
                    if(!guild.isMember(target))
                    {
                        event.replyError("This user is not member of this server!");
                        return;
                    }
                    List<Case> cases = Punishments.getAllInfractions(guild.getMember(target), 10);
                    StringBuilder sb = new StringBuilder();
                    for(Case usercases : cases)
                    {
                        sb.append("» ")
                                .append("**")
                                .append(usercases.getReason())
                                .append("** ")
                                .append("(`")
                                .append(usercases.getCaseID())
                                .append("`) - ")
                                .append(FormattedDuration.getDuration(usercases.getCreatedAt()/1000, true))
                                .append("\n");
                    }
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(CaseType.WARN.getEmbedColor())
                            .setFooter("User-ID: "+target.getIdLong())
                            .setTimestamp(Instant.now())
                            .setDescription("ℹ Use `"+ DiscordBot.instance.prefixManager.getPrefix(guild.getIdLong())+"case [Case-ID]` for more information!")
                            .setTitle("Log | "+target.getAsTag())
                            .addField("Warns in the last 24 hours", String.valueOf(Punishments.getAllInfractions(guild.getMember(target), FormattedDuration.ONE_DAY, -1).size()), true)
                            .addField("Warns in the last 7 days", String.valueOf(Punishments.getAllInfractions(guild.getMember(target), FormattedDuration.ONE_WEEK, -1).size()), true)
                            .addField("Last 10 warns", sb.toString().trim(), false);
                    event.reply(builder.build());
                },
                (failure) ->
                {
                    event.replyError("Invalid User-ID!");
                }
        );


    }
}
