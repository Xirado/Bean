package at.xirado.bean.commands;

import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.misc.Util;
import at.xirado.bean.objects.Command;
import at.xirado.bean.urbanapi.Definition;
import at.xirado.bean.urbanapi.UDParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrbanDictionary extends Command
{
    public UrbanDictionary()
    {
        super("urban", "Search phrases on urbandictionary", "urban [Phrase]");
        setAliases("urbandict", "urbandictionary");
        setCommandCategory(CommandCategory.FUN);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        String[] args = context.getArguments().toStringArray();
        TextChannel channel = event.getChannel();
        Member m = context.getMember();
        if(args.length == 0)
        {
            context.replyErrorUsage();
            return;
        }
        StringBuilder b = new StringBuilder();
        int index = 0;
        if(args.length > 1)
        {

            try
            {
                index = Integer.parseInt(args[args.length-1])-1;
                if(index < 0)
                {
                    context.replyError(index+1+" is not a valid index!");
                    return;
                }
                List<String> arl = new ArrayList<>(Arrays.asList(args));
                arl.remove(arl.size()-1);
                args = arl.toArray(String[]::new);

            } catch (NumberFormatException ignored)
            {

            }
        }
        for (String arg : args) {
            b.append(arg).append(" ");
        }
        String Query = b.toString().trim();

        UDParser udparser = new UDParser("http://api.urbandictionary.com/v0/");
        String JSONData = udparser.getJSONData(Query.replaceAll(" +", "+"));
        Definition[] results = udparser.getDefinitionsWithJSONData(JSONData);
        if(results.length == 0)
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(Color.decode("#1D2439"))
                    .setTitle("Sorry, I haven't found anything...")
                    .setTimestamp(Instant.now());
            channel.sendMessage(builder.build()).queue();
            return;
        }
        if(results.length < index+1)
        {

            index = results.length-1;
        }
        Definition result = results[index];
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
                .setFooter(Util.ordinal(index+1)+" definition | requested by "+event.getAuthor().getAsTag())
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
        String monthformatted = null;
        int year = Integer.parseInt(timeposted.substring(0,4));
        int month = Integer.parseInt(timeposted.substring(5,7));
        int day = Integer.parseInt(timeposted.substring(8,10));
        switch (month)
        {
            case 1:
                monthformatted = "January "+Util.ordinal(day)+", "+year;
                break;
            case 2:
                monthformatted = "February "+Util.ordinal(day)+", "+year;
                break;
            case 3:
                monthformatted = "March "+Util.ordinal(day)+", "+year;
                break;
            case 4:
                monthformatted = "April "+Util.ordinal(day)+", "+year;
                break;
            case 5:
                monthformatted = "May "+Util.ordinal(day)+", "+year;
                break;
            case 6:
                monthformatted = "June "+Util.ordinal(day)+", "+year;
                break;
            case 7:
                monthformatted = "July "+Util.ordinal(day)+", "+year;
                break;
            case 8:
                monthformatted = "August "+Util.ordinal(day)+", "+year;
                break;
            case 9:
                monthformatted = "September "+Util.ordinal(day)+", "+year;
                break;
            case 10:
                monthformatted = "October "+Util.ordinal(day)+", "+year;
                break;
            case 11:
                monthformatted = "November "+Util.ordinal(day)+", "+year;
                break;
            case 12:
                monthformatted = "December "+Util.ordinal(day)+", "+year;
                break;
            default:
                monthformatted = "Unknown";

        }
        builder.addField("Author","["+result.getAuthor()+"]("+authorurl+")\n"+ monthformatted+"\n\uD83D\uDC4D "+result.getLikes()+" \uD83D\uDC4E "+result.getDislikes()+"\n"+"Permalink: "+result.getPermalink() , false);
        channel.sendMessage(builder.build()).queue();
    }
}
