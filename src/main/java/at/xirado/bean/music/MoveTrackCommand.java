// 
// Decompiled by Procyon v0.5.36
// 

package at.xirado.bean.music;

import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.queue.FairQueue;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class MoveTrackCommand extends Command
{

    public MoveTrackCommand()
    {
        super("movetrack", "Moves a track in the queue to a different position", "movetrack [from] [to]");
        setCommandCategory(CommandCategory.MUSIC);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        final String[] parts = context.getArguments().toStringArray();
        if(!context.isDJ())
        {
            context.replyError("You need to be a DJ to do this!");
            return;
        }
        if (parts.length < 2) {
            context.replyError("Please include two valid indexes.");
            return;
        }
        int from;
        int to;
        try {
            from = Integer.parseInt(parts[0]);
            to = Integer.parseInt(parts[1]);
        }
        catch (NumberFormatException e) {
            context.replyError("Please provide two valid indexes.");
            return;
        }
        if (from == to) {
            context.replyError("Can't move a track to the same position.");
            return;
        }
        final AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        final FairQueue<QueuedTrack> queue = handler.getQueue();
        if (isUnavailablePosition(queue, from)) {
            final String reply = String.format("`%d` is not a valid position in the queue!", from);
            context.replyError(reply);
            return;
        }
        if (isUnavailablePosition(queue, to)) {
            final String reply = String.format("`%d` is not a valid position in the queue!", to);
            context.replyError(reply);
            return;
        }
        final QueuedTrack track = queue.moveItem(from - 1, to - 1);
        final String trackTitle = track.getTrack().getInfo().title;
        final String reply2 = String.format("Moved **%s** from position `%d` to `%d`.", trackTitle, from, to);
        context.replySuccess(reply2);
    }

    private static boolean isUnavailablePosition(final FairQueue<QueuedTrack> queue, final int position) {
        return position < 1 || position > queue.size();
    }
}
