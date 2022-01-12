package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.LinkedDataObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Objects;

public class MemeCommand extends SlashCommand {
    public MemeCommand()
    {
        setCommandData(new CommandData("meme", "Gets a random random Meme from a specific subreddit")
                .addOptions(new OptionData(OptionType.STRING, "subreddit", "Select the Subreddit you want to get the Meme from")
                        .addChoice("r/memes", "memes")
                        .addChoice("r/me_irl", "me_irl")
                        .addChoice("r/ProgrammerHumor", "programmerhumor")
                        .addChoice("r/dankmemes", "dankmemes")
                )
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx){
        try
        {
            String requestURL = (!(event.getOption("subreddit")==null))?String.format("https://meme-api.herokuapp.com/gimme/%s", Objects.requireNonNull(event.getOption("subreddit")).getAsString()):"https://meme-api.herokuapp.com/gimme/memes";
            URL url = new URL(requestURL);
            LinkedDataObject json = LinkedDataObject.parse(url);
            if (json == null)
            {
                ctx.replyError(ctx.getLocalized("commands.meme.api_down")).queue();
                return;
            }
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle(json.getString("title"))
                    .setImage(json.getString("url"))
                    .setDescription("\n\n[" + ctx.getLocalized("commands.meme.source") + "](" + json.getString("postLink") + ")")
                    .setColor(0x152238);
            ctx.reply(builder.build()).queue();

        } catch (Exception e)
        {
            ctx.replyError(ctx.getLocalized("general.unknown_error_occured")).queue();
        }
    }
}
