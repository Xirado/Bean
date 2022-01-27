package at.xirado.bean.command.slashcommands.leveling;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class LeaderboardCommand extends SlashCommand {

    public LeaderboardCommand() {
        setCommandData(new CommandData("leaderboard", "Gets the most active members of a server."));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx) {
        event.reply("**Visit the leaderboard here:**")
                .addActionRow(Button.link("https://bean.bz/leaderboard?id=" + event.getGuild().getIdLong(), "Leaderboard"))
                .queue();
    }
}
