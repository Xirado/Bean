package at.xirado.bean.music;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.JDA;

import java.util.Arrays;

public class StopCommand extends Command
{

    public StopCommand(JDA jda)
    {
        super(jda);
        this.invoke = "stop";
        this.description = "Stops the player and clears the queue";
        this.usage = "stop";
        this.aliases = Arrays.asList("leave", "quit", "disconnect");
        this.commandType = CommandType.MUSIC;
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        if(event.getMember().getVoiceState() == null)
        {
            if(!event.getMember().getVoiceState().inVoiceChannel())
            {
                event.replyError("You need to be in a Voicechannel to do this!");
                return;
            }
            event.replyError("You need to be in a Voicechannel to do this!");
            return;
        }

        if(event.getGuild().getSelfMember().getVoiceState() == null || !event.getGuild().getSelfMember().getVoiceState().inVoiceChannel())
        {
            event.replyError("There is no music playing!");
            return;
        }
        if(!event.isDJ())
        {
            event.replyError("Only a DJ can do this!");
            return;
        }

        if(ResultHandler.init(event))
            return;
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        handler.stopAndClear();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.replySuccess("The player has stopped and the queue has been cleared.");
        System.gc();
    }
}
