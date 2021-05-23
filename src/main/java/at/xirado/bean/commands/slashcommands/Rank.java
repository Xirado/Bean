package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandutil.SlashCommandContext;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.objects.SlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.CommandHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Rank extends SlashCommand
{
    public Rank()
    {
        setCommandData(new CommandData("rank", "Shows your rank")
                .addOption(new OptionData(OptionType.USER, "user", "Gets the rank of another member")
                        .setRequired(false))
        );
        setRunnableInDM(false);
    }
    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        CommandHook commandHook = event.getHook();
        event.acknowledge(false).queue();
        SlashCommandEvent.OptionData optionData = event.getOption("user");
        if(optionData == null)
        {
            User user = event.getUser();
            long xp = RankingSystem.getTotalXP(event.getGuild().getIdLong(), user.getIdLong());
            if(xp < 100)
            {
                commandHook.sendMessage("You are not yet ranked!").queue();
                return;
            }
            commandHook.sendMessage("").addFile("card.png", RankingSystem.generateLevelCard(user, event.getGuild())).queue();
        }else {
            User user = optionData.getAsUser();
            long xp = RankingSystem.getTotalXP(event.getGuild().getIdLong(), user.getIdLong());
            if(xp < 100)
            {
                commandHook.sendMessage("This member is not yet ranked!").queue();
                return;
            }
            byte[] levelCard = RankingSystem.generateLevelCard(user, event.getGuild());
            if(levelCard == null)
            {
                commandHook.sendMessage("This member is not yet ranked!").queue();
                return;
            }
            commandHook.sendMessage("").addFile("card.png", levelCard).queue();
        }
    }
}
