package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.SlashCommand;
import at.xirado.bean.commandmanager.SlashCommandContext;
import at.xirado.bean.misc.JSON;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

public class RandomFactCommand extends SlashCommand
{

    public RandomFactCommand()
    {
        setCommandData(new CommandUpdateAction.CommandData("fact", "gets a random useless fact"));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        try
        {
            String requestURL = "";
            if(ctx.getLanguage().toString().equals("de.json"))
            {
                requestURL = "https://uselessfacts.jsph.pl/random.json?language=de";
            }else {
                requestURL = "https://uselessfacts.jsph.pl/random.json?language=en";
            }
            URL url = new URL(requestURL);
            JSON json = JSON.parse(url);
            if(json == null)
            {
                ctx.replyError(ctx.getLocalized("commands.fact.api_down")).queue();
                return;
            }
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle(ctx.getLocalized("commands.fact.title"))
                    .setDescription(json.getString("text")+"\n\n["+ctx.getLocalized("commands.fact.source")+"]("+json.getString("source_url")+")")
                    .setColor(0x152238);
            ctx.reply(builder.build()).queue();

        }catch (Exception e)
        {
            ctx.replyError(ctx.getLocalized("general.unknown_error_occured")).queue();
        }
    }


}
