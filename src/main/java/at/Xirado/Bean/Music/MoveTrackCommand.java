// 
// Decompiled by Procyon v0.5.36
// 

package at.Xirado.Bean.Music;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.queue.FairQueue;
import net.dv8tion.jda.api.JDA;

public class MoveTrackCommand extends Command
{

    public MoveTrackCommand(JDA jda)
    {
        super(jda);
        this.invoke = "movetrack";
        this.description = "Moves a track in the queue to a different position";
        this.usage = "movetrack [from] [to]";
        this.commandType = CommandType.MUSIC;
    }

    @Override
    public void execute(final CommandEvent event) {
        final String[] parts = event.getArguments().getArguments();
        if(!event.isDJ())
        {
            event.replyError("You need to be a DJ to do this!");
            return;
        }
        if (parts.length < 2) {
            event.replyError("Please include two valid indexes.");
            return;
        }
        int from;
        int to;
        try {
            from = Integer.parseInt(parts[0]);
            to = Integer.parseInt(parts[1]);
        }
        catch (NumberFormatException e) {
            event.replyError("Please provide two valid indexes.");
            return;
        }
        if (from == to) {
            event.replyError("Can't move a track to the same position.");
            return;
        }
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        final FairQueue<QueuedTrack> queue = handler.getQueue();
        if (isUnavailablePosition(queue, from)) {
            final String reply = String.format("`%d` is not a valid position in the queue!", from);
            event.replyError(reply);
            return;
        }
        if (isUnavailablePosition(queue, to)) {
            final String reply = String.format("`%d` is not a valid position in the queue!", to);
            event.replyError(reply);
            return;
        }
        final QueuedTrack track = queue.moveItem(from - 1, to - 1);
        final String trackTitle = track.getTrack().getInfo().title;
        final String reply2 = String.format("Moved **%s** from position `%d` to `%d`.", trackTitle, from, to);
        event.replySuccess(reply2);
    }

    private static boolean isUnavailablePosition(final FairQueue<QueuedTrack> queue, final int position) {
        return position < 1 || position > queue.size();
    }
}
