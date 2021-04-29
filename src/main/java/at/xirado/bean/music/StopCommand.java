package at.xirado.bean.music;

import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class StopCommand extends Command
{

    public StopCommand()
    {
        super("stop", "stops the player and clears the queue", "stop");
        setAliases("leave", "quit", "disconnect");
        setCommandCategory(CommandCategory.MUSIC);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        if(context.getMember().getVoiceState() == null)
        {
            if(!context.getMember().getVoiceState().inVoiceChannel())
            {
                context.replyError("You need to be in a Voicechannel to do this!");
                return;
            }
            context.replyError("You need to be in a Voicechannel to do this!");
            return;
        }

        if(event.getGuild().getSelfMember().getVoiceState() == null || !event.getGuild().getSelfMember().getVoiceState().inVoiceChannel())
        {
            context.replyError("There is no music playing!");
            return;
        }
        if(!context.isDJ())
        {
            context.replyError("Only a DJ can do this!");
            return;
        }

        if(ResultHandler.init(context))
            return;
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        handler.stopAndClear();
        event.getGuild().getAudioManager().closeAudioConnection();
        context.replySuccess("The player has stopped and the queue has been cleared.");
        System.gc();
    }
}
