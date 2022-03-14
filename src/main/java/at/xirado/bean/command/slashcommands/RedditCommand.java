package at.xirado.bean.command.slashcommands;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.BasicAutocompletionChoice;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RedditCommand extends SlashCommand
{

    public RedditCommand()
    {
        setCommandData(Commands.slash("reddit", "Gets a trending post from a subreddit")
                .addOptions(new OptionData(OptionType.STRING, "subreddit", "Select or enter a subreddit")
                        .setAutoComplete(true)
                )
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx)
    {
        InteractionHook hook = event.getHook();

        try
        {
            event.deferReply().queue();
            OptionMapping option = event.getOption("subreddit");
            String subreddit = option == null || option.getAsString().isEmpty()
                    ? "memes"
                    : option.getAsString().startsWith("r/")
                    ? option.getAsString().substring(2)
                    : option.getAsString();

            String requestURL = String.format("https://meme-api.herokuapp.com/gimme/%s", subreddit);
            OkHttpClient client = Bean.getInstance().getOkHttpClient();
            Request request = new Request.Builder()
                    .url(requestURL)
                    .get()
                    .build();

            Call call = client.newCall(request);
            Response response = call.execute();
            DataObject object = DataObject.fromJson(response.body().string());
            response.close();
            if (!response.isSuccessful())
            {
                if (!object.isNull("message"))
                    hook.sendMessageEmbeds(EmbedUtil.errorEmbed(object.getString("message"))).queue();
                else
                    hook.sendMessageEmbeds(EmbedUtil.errorEmbed(ctx.getLocalized("commands.meme.api_down"))).queue();
                return;
            }

            if (object.getBoolean("nsfw") && !event.getTextChannel().isNSFW())
            {
                hook.sendMessageEmbeds(EmbedUtil.errorEmbed(ctx.getLocalized("commands.meme.is_nsfw"))).setEphemeral(true).queue();
                return;
            }

            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle(object.getString("title"))
                    .setImage(object.getString("url"))
                    .setDescription("[" + ctx.getLocalized("commands.meme.source") + "](" + object.getString("postLink") + ")")
                    .setFooter("r/" + object.getString("subreddit"))
                    .setColor(0x152238);
            hook.sendMessageEmbeds(builder.build()).queue();
        }
        catch (Exception e)
        {
            hook.sendMessageEmbeds(EmbedUtil.errorEmbed(ctx.getLocalized("general.unknown_error_occured"))).queue();
        }
    }

    @Override
    public void handleAutocomplete(@NotNull CommandAutoCompleteInteractionEvent event)
    {
        List<BasicAutocompletionChoice> choices = List.of(
                new BasicAutocompletionChoice("r/memes", "memes"),
                new BasicAutocompletionChoice("r/me_irl", "me_irl"),
                new BasicAutocompletionChoice("r/ProgrammerHumor", "programmerhumor"),
                new BasicAutocompletionChoice("r/dankmemes", "dankmemes"),
                new BasicAutocompletionChoice("r/AdviceAnimals", "adviceanimals"),
                new BasicAutocompletionChoice("r/interestingasfuck", "interestingasfuck")
        );
        event.replyChoices(
                choices.stream()
                        .filter(value -> StringUtils.startsWithIgnoreCase(value.getName(), event.getFocusedOption().getValue())
                                      || StringUtils.startsWithIgnoreCase(value.getValue(), event.getFocusedOption().getValue()))
                        .map(BasicAutocompletionChoice::toCommandAutocompleteChoice)
                        .toList()
        ).queue();
    }
}
