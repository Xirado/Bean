// 
// Decompiled by Procyon v0.5.36
// 

package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class NowPlayingCommand extends Command
{

    public NowPlayingCommand()
    {
        super("nowplaying", "Shows info about the currently playing song", "nowplaying");
        setAliases("playingnow", "playing", "pn", "np");
        setCommandCategory(CommandCategory.MUSIC);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        final AudioHandler handler = ResultHandler.getHandler(event.getGuild());
        final Message m = handler.getNowPlaying(event.getJDA());
        if (m == null) {
            context.reply(handler.getNoMusicPlaying(event.getJDA()));
            Bean.instance.musicinstance.getNowplayingHandler().clearLastNPMessage(event.getGuild());
        }
        else {
            context.reply(m, msg -> Bean.instance.musicinstance.getNowplayingHandler().setLastNPMessage(msg));
        }
    }
}
