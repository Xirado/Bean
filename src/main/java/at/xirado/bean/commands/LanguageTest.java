package at.xirado.bean.commands;

import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.commandutil.CommandFlag;
import at.xirado.bean.objects.Command;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;

import java.time.Instant;

public class LanguageTest extends Command
{
    public LanguageTest()
    {
        super("test", "langtest", "test");
        setCommandFlags(CommandFlag.PRIVATE_COMMAND, CommandFlag.DEVELOPER_ONLY);
        addAllowedGuilds(815597207617142814L);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        EmbedBuilder testEmbed = new EmbedBuilder()
                .setColor(0xff0000)
                .setDescription("Test")
                .setAuthor(event.getAuthor().getName(), null, event.getAuthor().getEffectiveAvatarUrl())
                .setTimestamp(Instant.now())
                .setFooter("Footer text");
        EntityBuilder entityBuilder = ((JDAImpl)event.getJDA()).getEntityBuilder();
        DataObject dataObject = testEmbed.build().toData().put("type", "RICH");
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            System.out.println(mapper.readTree(dataObject.toString()).toPrettyString());
        } catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }
        MessageEmbed embed = entityBuilder.createMessageEmbed(dataObject);
        context.reply(embed);
    }
}
