package at.xirado.bean.command.slashcommands.leveling;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.RankingSystem;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class SetXPBackgroundCommand extends SlashCommand
{
    public SetXPBackgroundCommand()
    {
        setCommandData(Commands.slash("setxpcard", "Updates /rank background.")
                .addOptions(new OptionData(OptionType.STRING, "background", "The Background.")
                        .addChoice("Blue (Default)", "card1")
                        .addChoice("Green", "card2")
                        .addChoice("Red", "card3")
                        .addChoice("Purple", "card4")
                        .setRequired(true)
                )
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx)
    {
        try
        {
            RankingSystem.setPreferredCard(event.getUser(), event.getOption("background").getAsString());
            ctx.reply("Your background has been updated!").setEphemeral(true).queue();
        }
        catch (SQLException ex)
        {
            ctx.replyError("Could not update background!").setEphemeral(true).queue();
            LoggerFactory.getLogger(Bean.class).error("Could not update a users background!", ex);

        }
    }
}
