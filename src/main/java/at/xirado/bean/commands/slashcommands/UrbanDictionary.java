package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.*;
import at.xirado.bean.misc.Util;
import at.xirado.bean.urbanapi.Definition;
import at.xirado.bean.urbanapi.UDParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrbanDictionary extends SlashCommand
{
    public UrbanDictionary()
    {
        setCommandData(new CommandUpdateAction.CommandData("urban", "search for urbandictionary.com definitions")
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "phrase", "the phrase to search for")
                        .setRequired(true))
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.INTEGER, "definition", "gets a specific definition rather than the first one")
                        .setRequired(false))
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull CommandContext ctx)
    {
        String phrase = event.getOption("phrase").getAsString();
        int index = event.getOption("definition") != null ? (int) event.getOption("definition").getAsLong() : 1;
        if(index < 1) index = 1;

        UDParser udparser = new UDParser("http://api.urbandictionary.com/v0/");
        String JSONData = udparser.getJSONData(phrase.replaceAll(" +", "+"));
        Definition[] results = udparser.getDefinitionsWithJSONData(JSONData);
        if(results.length == 0)
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(Color.decode("#1D2439"))
                    .setTitle("Sorry, I haven't found anything...")
                    .setTimestamp(Instant.now());
            ctx.reply(builder.build()).queue();
            return;
        }
        if(results.length < index)
        {

            index = results.length;
        }
        Definition result = results[index-1];
        String description = result.getDefinition();
        Pattern p = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher matcher = p.matcher(description);
        description = matcher.replaceAll(
                match -> "["+match.group().replaceAll("\\[|\\]", "")+"]"+"(https://urbandictionary.com/define.php?term="+match.group().replaceAll(" ", "+").replaceAll("\\[|\\]", "")+")");
        if(description.length() > 1024)
        {
            String replacestring = "[Read more]("+result.getPermalink()+")";
            String split = description.substring(0,1024-replacestring.length());
            description = split+replacestring;
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.decode("#1D2439"))
                .setTitle(result.getWordName())
                .setFooter(Util.ordinal(index)+" definition")
                .setTimestamp(Instant.now())
                .setImage("https://bean.bz/assets/urban.png")
                .setDescription(description);
        String example = result.getExample();
        if(example != null)
        {
            Pattern p1 = Pattern.compile("\\[([^\\]]+)\\]");
            Matcher matcher2 = p1.matcher(example);
            example = matcher2.replaceAll(
                    match -> "["+match.group().replaceAll("\\[|\\]", "")+"]"+"(https://www.urbandictionary.com/define.php?term="+match.group().replaceAll(" ", "+").replaceAll("\\[|\\]", "")+")");
            if(example.length() <= 1022)
                builder.addField("Example", "*"+example+"*", false);
        }
        String authorurl = "https://www.urbandictionary.com/author.php?author=";
        authorurl += result.getAuthor().replaceAll(" ", "%20");
        String timeposted = result.getWrittenDate();
        String monthformatted;
        int year = Integer.parseInt(timeposted.substring(0,4));
        int month = Integer.parseInt(timeposted.substring(5,7));
        int day = Integer.parseInt(timeposted.substring(8,10));
        if(month > 12)
        {
            EmbedBuilder builder2 = new EmbedBuilder()
                    .setColor(Color.decode("#1D2439"))
                    .setTitle("UrbanDictionary sent invalid data!")
                    .setTimestamp(Instant.now());
            ctx.reply(builder2.build()).queue();
            return;
        }
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        monthformatted = months[month-1]+" "+Util.ordinal(day)+", "+year;
        builder.addField("Author","["+result.getAuthor()+"]("+authorurl+")\n"+ monthformatted+"\n\uD83D\uDC4D "+result.getLikes()+" \uD83D\uDC4E "+result.getDislikes()+"\n"+"Permalink: "+result.getPermalink() , false);
        ctx.reply(builder.build()).queue();
    }
}
