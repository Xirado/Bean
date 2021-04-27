// 
// Decompiled by Procyon v0.5.36
// 

package at.xirado.bean.music;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;

public class NowPlayingCommand extends Command
{

    public NowPlayingCommand(JDA jda)
    {
        super(jda);
        this.invoke = "nowplaying";
        this.aliases = Arrays.asList("playingnow", "playing", "pn");
        this.description = "Shows info about the currently playing song";
        this.usage = "nowplaying";
        this.commandType = CommandType.MUSIC;
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        final AudioHandler handler = (AudioHandler)ResultHandler.getHandler(event.getGuild());
        final Message m = handler.getNowPlaying(event.getJDA());
        if (m == null) {
            context.reply(handler.getNoMusicPlaying(event.getJDA()));
            DiscordBot.instance.musicinstance.getNowplayingHandler().clearLastNPMessage(event.getGuild());
        }
        else {
            context.reply(m, msg -> DiscordBot.instance.musicinstance.getNowplayingHandler().setLastNPMessage(msg));
        }
    }
}
