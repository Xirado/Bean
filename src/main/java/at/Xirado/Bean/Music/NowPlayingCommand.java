// 
// Decompiled by Procyon v0.5.36
// 

package at.Xirado.Bean.Music;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Main.DiscordBot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;

public class NowPlayingCommand extends Command
{

    public NowPlayingCommand(JDA jda)
    {
        super(jda);
        this.invoke = "nowplaying";
        this.aliases = new String[]{"playingnow", "playing", "pn"};
        this.description = "Shows info about the currently playing song";
        this.usage = "nowplaying";
        this.commandType = CommandType.MUSIC;
    }

    @Override
    public void executeCommand(final CommandEvent event) {
        final AudioHandler handler = (AudioHandler)ResultHandler.getHandler(event.getGuild());
        final Message m = handler.getNowPlaying(event.getJDA());
        if (m == null) {
            event.reply(handler.getNoMusicPlaying(event.getJDA()));
            DiscordBot.instance.musicinstance.getNowplayingHandler().clearLastNPMessage(event.getGuild());
        }
        else {
            event.reply(m, msg -> DiscordBot.instance.musicinstance.getNowplayingHandler().setLastNPMessage(msg));
        }
    }
}
