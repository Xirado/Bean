package at.xirado.bean.music;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandArgument;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.JDA;

import java.util.Arrays;

public class PlayCommand extends Command
{
    public PlayCommand(JDA jda)
    {
        super(jda);
        this.invoke = "play";
        this.description = "Plays/Resumes a song";
        this.usage = "play [Title/URL]";
        this.aliases = Arrays.asList("p");
        this.commandType = CommandType.MUSIC;
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        Bot bot = DiscordBot.instance.musicinstance;
        CommandArgument commandArgument = event.getArguments();
        String asWhole = commandArgument.toString(0);
        String[] args = commandArgument.toStringArray();
        if(ResultHandler.init(event))
            return;
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if (args.length != 0 || !event.getMessage().getAttachments().isEmpty()) {
            handler.getPlayer().setVolume(70);
            if(!event.getGuild().getAudioManager().isSelfDeafened())
            {
                event.getGuild().getAudioManager().setSelfDeafened(true);
            }
            final String argument = (asWhole.startsWith("<") && asWhole.endsWith(">")) ? asWhole.substring(1, asWhole.length() - 1) : (asWhole.isEmpty() ? event.getMessage().getAttachments().get(0).getUrl() : asWhole);
            event.reply(CommandEvent.LOADING_EMOTE + " Loading... `[" + asWhole + "]`", m -> DiscordBot.instance.musicinstance.getPlayerManager().loadItemOrdered(event.getGuild(), asWhole, new ResultHandler(m, event, false)));
            return;
        }
        if(handler.getPlayer().getPlayingTrack() != null)
        {
            if(handler.getPlayer().isPaused())
            {
                if (event.isDJ()) {
                    handler.getPlayer().setPaused(false);
                    event.replySuccess("Resumed **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**.");
                    return;
                }
                else {
                    event.replyError("Only DJs can unpause the player!");
                    return;
                }
            }else
            {
                event.replyWarning("The player is not paused!");
                return;
            }
        }
        event.replyErrorUsage();
    }
}