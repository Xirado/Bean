package at.xirado.bean.command.slashcommands.leveling;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.RankedUser;
import at.xirado.bean.data.RankingSystem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;


public class LeaderboardCommand extends SlashCommand
{

    public LeaderboardCommand()
    {
        setCommandData(new CommandData("leaderboard", "Gets the top 10 members of a server."));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Guild guild = event.getGuild();
        ArrayList<RankedUser> users = RankingSystem.getTopTen(guild.getIdLong());
        if (users.size() == 0)
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(0xff0000)
                    .setDescription("There are no ranked members!");
            ctx.reply(builder.build()).setEphemeral(true).queue();
            return;
        }
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (RankedUser user : users)
        {
            builder.append("`#").append(index).append("` | **").append(user.getName()).append("#").append(user.getDiscriminator()).append("** ").append(RankingSystem.formatXP(user.getTotalXP())).append(" XP (Level " + RankingSystem.getLevel(user.getTotalXP()) + ")\n");
            index++;
        }
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(0x00FFFF)
                .setTitle("Top 10 members")
                .setDescription(builder.toString().trim())
                .setAuthor(guild.getName(), null, guild.getIconUrl());
        ctx.reply(embed.build()).queue();
    }
}
