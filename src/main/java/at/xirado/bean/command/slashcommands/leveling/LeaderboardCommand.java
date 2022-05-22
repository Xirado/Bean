package at.xirado.bean.command.slashcommands.leveling;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;


public class LeaderboardCommand extends SlashCommand {

    public LeaderboardCommand() {
        setCommandData(Commands.slash("leaderboard", "Gets the most active members of a server."));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        event.reply("**Visit the leaderboard here:**")
                .addActionRow(Button.link("https://bean.bz/leaderboard?id=" + event.getGuild().getIdLong(), "Leaderboard"))
                .queue();
    }
}
