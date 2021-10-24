package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandContext;
import net.dv8tion.jda.api.events.interaction.ApplicationCommandAutocompleteEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.data.DataArray;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class OnSlashCommand extends ListenerAdapter
{
    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event)
    {
        if (Bean.getInstance().isDebug() && event.getUser().getIdLong() != Bean.OWNER_ID)
        {
            event.reply(CommandContext.ERROR_EMOTE + " Bot is in debug mode! Only <@"+Bean.OWNER_ID+"> can execute commands!").setEphemeral(true).queue();
            return;
        }
        Bean.getInstance().getSlashCommandHandler().handleSlashCommand(event, event.getMember());
    }

    @Override
    public void onApplicationCommandAutocomplete(@NotNull ApplicationCommandAutocompleteEvent event)
    {
        Bean.getInstance().getExecutor().submit(() -> {
            try
            {
                if (event.getName().equals("play"))
                {
                    OptionMapping query = event.getOption("query");
                    if (query != null && query.isFocused())
                    {
                        if (query.getAsString().length() == 0)
                        {
                            event.deferChoices(Collections.emptyList()).queue();
                            return;
                        }
                        String url = "https://clients1.google.com/complete/search?client=youtube&gs_ri=youtube&ds=yt&q="+query.getAsString().replace("\\s+", "%20");
                        Request request = new Request.Builder().url(url).build();
                        Call call = Bean.getInstance().getOkHttpClient().newCall(request);
                        Response response = call.execute();
                        if (!response.isSuccessful())
                        {
                            event.deferChoices(Collections.singletonList(new Command.Choice(query.getAsString(), query.getAsString()))).queue();
                            return;
                        }
                        Set<Command.Choice> choices = new LinkedHashSet<>();
                        String string = response.body().string();
                        string = string.substring(19, string.length()-1);
                        DataArray array = DataArray.fromJson(string).getArray(1);
                        array.stream(DataArray::getArray)
                                .limit(10)
                                .forEach(x -> choices.add(new Command.Choice(x.getString(0), x.getString(0))));
                        event.deferChoices(choices).queue();
                    }
                }
            } catch (Exception ex)
            {
                event.deferChoices(Collections.emptyList()).queue();
            }
        });
    }
}
