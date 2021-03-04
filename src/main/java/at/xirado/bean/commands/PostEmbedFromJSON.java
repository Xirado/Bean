package at.xirado.bean.commands;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;

import java.util.Arrays;

public class PostEmbedFromJSON extends Command
{
    public PostEmbedFromJSON(JDA jda)
    {
        super(jda);
        this.invoke = "postfromembed";
        this.commandType = CommandType.ADMIN;
        this.description = "Posts an embed from a JSONObject";
        this.neededPermissions = Arrays.asList(Permission.ADMINISTRATOR);
        this.global = false;
        this.enabledGuilds = Arrays.asList(451719783184990208L);
        this.usage = "postfromembed [JSONObject]";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        TextChannel channel = event.getChannel();
        DataObject dataObject = DataObject.fromJson("some long json text");
        EntityBuilder entityBuilder = new EntityBuilder(DiscordBot.instance.jda);
        MessageEmbed embed = entityBuilder.createMessageEmbed(dataObject);
        channel.sendMessage(embed).queue();

    }
}
