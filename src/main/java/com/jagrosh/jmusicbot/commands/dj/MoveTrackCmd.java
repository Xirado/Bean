// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.queue.FairQueue;

public class MoveTrackCmd extends DJCommand
{
    public MoveTrackCmd(final Bot bot) {
        super(bot);
        this.name = "movetrack";
        this.help = "move a track in the current queue to a different position";
        this.arguments = "<from> <to>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        final String[] parts = event.getArgs().split("\\s+", 2);

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
