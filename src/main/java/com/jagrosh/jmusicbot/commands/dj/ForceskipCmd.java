// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import net.dv8tion.jda.api.entities.User;

public class ForceskipCmd extends DJCommand
{
    public ForceskipCmd(final Bot bot) {
        super(bot);
        this.name = "forceskip";
        this.help = "skips the current song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        final User u = event.getJDA().getUserById(handler.getRequester());
        event.reply(event.getClient().getSuccess() + " Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title + "** (requested by " + ((u == null) ? "someone" : ("**" + u.getName() + "**")) + ")");
        handler.getPlayer().stopTrack();
    }
}
