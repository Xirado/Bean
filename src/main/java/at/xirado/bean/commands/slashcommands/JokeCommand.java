package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandutil.SlashCommandContext;
import at.xirado.bean.misc.JSON;
import at.xirado.bean.objects.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.time.Instant;

public class JokeCommand extends SlashCommand
{

    public JokeCommand()
    {
        setCommandData(new CommandData("joke", "tells you a random joke"));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        try
        {
            String requestURL = "https://v2.jokeapi.dev/joke/Miscellaneous,Dark,Pun";
            URL url = new URL(requestURL);
            JSON json = JSON.parse(url);
            if(json == null)
            {
                ctx.replyError(ctx.getLocalized("commands.fact.api_down")).queue();
                return;
            }
            boolean error = json.getBoolean("error");
            if(error)
            {
                ctx.replyError("The API has sent invalid data!").setEphemeral(true).queue();
                return;
            }
            boolean twoPart = json.getString("type").equals("twopart");
            String category = json.getString("category");
            switch (category)
            {
                case "Misc":
                    category = ctx.getLocalized("commands.joke.misc");
                    break;
                case "Dark":
                    category = ctx.getLocalized("commands.joke.dark");
                    break;
                case "Pun":
                    category = ctx.getLocalized("commands.joke.pun");
                    break;
                default:
                    break;
            }
            category = ctx.getLocalized("commands.joke.category", category);

            boolean nsfw = json.getBoolean("flags.nsfw");
            boolean religious = json.getBoolean("flags.religious");
            boolean political = json.getBoolean("flags.political");
            boolean racist = json.getBoolean("flags.racist");
            boolean sexist = json.getBoolean("flags.sexist");
            boolean explicit = json.getBoolean("flags.explicit");

            StringBuilder builder = new StringBuilder();
            if(nsfw) builder.append(ctx.getLocalized("commands.joke.nsfw")).append(", ");
            if(religious) builder.append(ctx.getLocalized("commands.joke.religious")).append(", ");
            if(political) builder.append(ctx.getLocalized("commands.joke.political")).append(", ");
            if(racist) builder.append(ctx.getLocalized("commands.joke.racist")).append(", ");
            if(sexist) builder.append(ctx.getLocalized("commands.joke.sexist")).append(", ");
            if(explicit) builder.append(ctx.getLocalized("commands.joke.explicit")).append(", ");
            String flags = builder.toString();
            if(flags.length() > 2)
            {
                flags = flags.substring(0, flags.length()-2);
            }else {
                flags = ctx.getLocalized("commands.joke.none");
            }
            if(twoPart)
            {
                String setup = json.getString("setup");
                String delivery = json.getString("delivery");
                EmbedBuilder embedbuilder = new EmbedBuilder()
                        .setColor(0x152238)
                        .setTitle(category)
                        .setDescription(setup+"\n\n"+delivery)
                        .setFooter(ctx.getLocalized("commands.joke.flags")+": "+flags)
                        .setTimestamp(Instant.now());
                ctx.reply(embedbuilder.build()).queue();

            }else {
                String joke = json.getString("joke");
                EmbedBuilder embedbuilder = new EmbedBuilder()
                        .setColor(0x152238)
                        .setTitle(category)
                        .setDescription(joke)
                        .setFooter("Flags: "+flags)
                        .setTimestamp(Instant.now());
                ctx.reply(embedbuilder.build()).queue();
            }

        }catch (Exception e)
        {
            e.printStackTrace();
            ctx.replyError(ctx.getLocalized("general.unknown_error_occured")).queue();
        }
    }
}
