// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;

public class ShuffleCmd extends MusicCommand
{
    public ShuffleCmd(final Bot bot) {
        super(bot);
        this.name = "shuffle";
        this.help = "shuffles songs you have added";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        final int s = handler.getQueue().shuffle(event.getAuthor().getIdLong());
        switch (s) {
            case 0: {
                event.replyError("You don't have any music in the queue to shuffle!");
                break;
            }
            case 1: {
                event.replyWarning("You only have one song in the queue!");
                break;
            }
            default: {
                event.replySuccess("You successfully shuffled your " + s + " entries.");
                break;
            }
        }
    }
}
