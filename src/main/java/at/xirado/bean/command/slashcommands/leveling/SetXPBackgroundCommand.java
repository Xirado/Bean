package at.xirado.bean.command.slashcommands.leveling;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.RankingSystem;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetXPBackgroundCommand extends SlashCommand
{
    public SetXPBackgroundCommand()
    {
        setCommandData(new CommandData("setxpcard", "Updates /rank background.")
                .addOptions(new OptionData(OptionType.STRING, "background", "The Background.")
                        .addChoice("Blue (Default)", "card1")
                        .addChoice("Green", "card2")
                        .addChoice("Red", "card3")
                        .addChoice("Purple", "card4")
                )
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        try
        {
            RankingSystem.setPreferredCard(event.getUser(), event.getOption("background").getAsString());
            ctx.reply("Your background has been updated!").setEphemeral(true).queue();
        } catch (Exception ex)
        {
            ctx.replyError("Could not update background!").setEphemeral(true).queue();
            ex.printStackTrace();
        }
    }
}
