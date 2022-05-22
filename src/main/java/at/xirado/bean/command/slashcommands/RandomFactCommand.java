package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.LinkedDataObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

public class RandomFactCommand extends SlashCommand {

    public RandomFactCommand() {
        setCommandData(Commands.slash("fact", "Gets a random useless fact."));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        try {
            String requestURL = "";
            if (ctx.getLanguage().toString().equals("de.json")) {
                requestURL = "https://uselessfacts.jsph.pl/random.json?language=de";
            } else {
                requestURL = "https://uselessfacts.jsph.pl/random.json?language=en";
            }
            URL url = new URL(requestURL);
            LinkedDataObject json = LinkedDataObject.parse(url);
            if (json == null) {
                ctx.replyError(ctx.getLocalized("commands.fact.api_down")).queue();
                return;
            }
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle(ctx.getLocalized("commands.fact.title"))
                    .setDescription(json.getString("text") + "\n\n[" + ctx.getLocalized("commands.fact.source") + "](" + json.getString("source_url") + ")")
                    .setColor(0x152238);
            ctx.reply(builder.build()).queue();

        } catch (Exception e) {
            ctx.replyError(ctx.getLocalized("general.unknown_error_occured")).queue();
        }
    }


}
