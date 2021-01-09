// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.audio;

import com.jagrosh.jmusicbot.queue.Queueable;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.User;

public class QueuedTrack implements Queueable
{
    private final AudioTrack track;
    
    public QueuedTrack(final AudioTrack track, final User owner) {
        this(track, owner.getIdLong());
    }
    
    public QueuedTrack(final AudioTrack track, final long owner) {
        (this.track = track).setUserData(owner);
    }
    
    @Override
    public long getIdentifier() {
        return this.track.getUserData(Long.class);
    }
    
    public AudioTrack getTrack() {
        return this.track;
    }
    
    @Override
    public String toString() {
        return "`[" + FormatUtil.formatTime(this.track.getDuration()) + "]` **" + this.track.getInfo().title + "** - <@" + this.track.getUserData(Long.class) + ">";
    }
}
