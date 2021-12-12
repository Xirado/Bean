package at.xirado.bean.command.commands;

import at.xirado.bean.command.Command;
import at.xirado.bean.command.CommandCategory;
import at.xirado.bean.command.CommandContext;
import at.xirado.bean.data.RankingSystem;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class Rank extends Command
{

    public Rank()
    {
        super("rank", "gets your current xp on this guild", "rank (optional member)");
        setCommandCategory(CommandCategory.BEAN);
    }

    @Override
    public void executeCommand(MessageReceivedEvent event, CommandContext context)
    {
        String[] args = context.getArguments().toStringArray();
        if (args.length == 0)
        {
            User user = event.getAuthor();
            long xp = RankingSystem.getTotalXP(event.getGuild().getIdLong(), user.getIdLong());
            if (xp < 100)
            {
                context.reply("You are not yet ranked!");
                return;
            }
            event.getChannel().sendFile(RankingSystem.generateLevelCard(user, event.getGuild()), "card.png").queue();
        } else
        {
            String id = args[0].replaceAll("[^0-9]", "");
            event.getJDA().retrieveUserById(id).queue(
                    (user) ->
                    {
                        long xp = RankingSystem.getTotalXP(event.getGuild().getIdLong(), user.getIdLong());
                        if (xp < 100)
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
                        if (xp < 100)
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
