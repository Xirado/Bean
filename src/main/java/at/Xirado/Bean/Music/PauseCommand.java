package at.Xirado.Bean.Music;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Main.DiscordBot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class PauseCommand extends Command
{


    public PauseCommand(JDA jda)
    {
        super(jda);
        this.invoke = "pause";
        this.description = "Pauses the currently playing song";
        this.usage = "pause";
        this.commandType = CommandType.MUSIC;
    }

    @Override
    public void execute(CommandEvent e)
    {
        GuildVoiceState guildVoiceState = e.getMember().getVoiceState();
        if(guildVoiceState == null || !guildVoiceState.inVoiceChannel())
        {
            e.replyError("You need to be in a Voicechannel to do this!");
            return;
        }
        VoiceChannel voiceChannel = guildVoiceState.getChannel();
        if(e.getGuild().getAfkChannel() != null)
        {
            if(e.getGuild().getAfkChannel().getIdLong() == voiceChannel.getIdLong())
            {
                e.replyError("You can't do this in an AFK-Channel!");
                return;
            }
        }
        if(!e.isDJ())
        {
            e.replyError("You need to be a DJ to do this!");
            return;
        }
        String Prefix = DiscordBot.instance.prefixManager.getPrefix(e.getGuild().getIdLong());
        final AudioHandler handler = (com.jagrosh.jmusicbot.audio.AudioHandler)e.getGuild().getAudioManager().getSendingHandler();
        if (handler.getPlayer().isPaused()) {
            e.replyWarning("The player is already paused! Use `" + Prefix + "play` to unpause!");
            return;
        }
        handler.getPlayer().setPaused(true);
        e.replySuccess("Paused **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**. Type `" + Prefix + "play` to unpause!");
    }
}
