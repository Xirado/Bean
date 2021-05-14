package at.xirado.bean.commands.xp;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rank extends Command
{
    private static final List<Long> timeout = Collections.synchronizedList(new ArrayList<Long>());

    public Rank()
    {
        super("rank", "gets your current xp on this guild", "rank (optional member)");
        setCommandCategory(CommandCategory.BEAN);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        String[] args = context.getArguments().toStringArray();
        if(args.length == 0)
        {
            User user = event.getAuthor();
            long xp = RankingSystem.getTotalXP(event.getGuild().getIdLong(), user.getIdLong());
            if(xp < 100)
            {
                context.reply("You are not yet ranked!");
                return;
            }
            event.getChannel().sendFile(RankingSystem.generateLevelCard(user, event.getGuild()), "card.png").queue();
        }else {
            String id = args[0].replaceAll("[^0-9]", "");
            Bean.getInstance().jda.retrieveUserById(id).queue(
                    (user) ->
                    {
                        long xp = RankingSystem.getTotalXP(event.getGuild().getIdLong(), user.getIdLong());
                        if(xp < 100)
                        {
                            context.reply("This member is not yet ranked!");
                            return;
                        }
                        event.getChannel().sendFile(RankingSystem.generateLevelCard(user, event.getGuild()), "card.png").queue();
                    },
                    (error) ->
                    {
                        User user = event.getAuthor();
                        long xp = RankingSystem.getTotalXP(event.getGuild().getIdLong(), user.getIdLong());
                        if(xp < 100)
                        {
                            context.reply("You are not yet ranked!");
                            return;
                        }
                        event.getChannel().sendFile(RankingSystem.generateLevelCard(user, event.getGuild()), "card.png").queue();
                    }
            );

        }

    }

}
