package at.xirado.bean.music;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        GuildVoiceState guildVoiceState = context.getMember().getVoiceState();
        if(guildVoiceState == null || !guildVoiceState.inVoiceChannel())
        {
            context.replyError("You need to be in a Voicechannel to do this!");
            return;
        }
        VoiceChannel voiceChannel = guildVoiceState.getChannel();
        if(event.getGuild().getAfkChannel() != null)
        {
            if(event.getGuild().getAfkChannel().getIdLong() == voiceChannel.getIdLong())
            {
                context.replyError("You can't do this in an AFK-Channel!");
                return;
            }
        }
        if(!context.isDJ())
        {
            context.replyError("You need to be a DJ to do this!");
            return;
        }
        String Prefix = DiscordBot.instance.prefixManager.getPrefix(event.getGuild().getIdLong());
        final AudioHandler handler = (com.jagrosh.jmusicbot.audio.AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getPlayer().isPaused()) {
            context.replyWarning("The player is already paused! Use `" + Prefix + "play` to unpause!");
            return;
        }
        handler.getPlayer().setPaused(true);
        context.replySuccess("Paused **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**. Type `" + Prefix + "play` to unpause!");
    }
}
