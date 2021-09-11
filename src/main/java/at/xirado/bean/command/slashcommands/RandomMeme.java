package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class RandomMeme extends SlashCommand
{

    public RandomMeme()
    {
        setCommandData(new CommandData("random-meme", "fetches a random meme from a specified subreddit")
                .addOption(OptionType.STRING, "subreddit", "the subreddit to fetch the meme from", true)
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        URI uri = URI.create("https://meme-api.herokuapp.com/gimme/"+ Objects.requireNonNull(event.getOption("subreddit")).getAsString());
        HttpRequest request = HttpRequest.newBuilder(uri).build();
        String content = null;
        try {
            content = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        assert content != null;
        if(content.contains("url")){
            JsonObject jsonObject = new Gson().fromJson(content, JsonObject.class);
            ctx.reply(jsonObject.get("url").getAsString()).queue();
        }
        else
            ctx.reply("OOPS!\nSomething went wrong here!\nMake sure you spelled your subreddit correct and try again!").queue();
    }
}