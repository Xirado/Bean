// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

public class NowplayingCmd extends MusicCommand
{
    public NowplayingCmd(final Bot bot) {
        super(bot);
        this.name = "nowplaying";
        this.help = "shows the song that is currently playing";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.botPermissions = new Permission[] { Permission.MESSAGE_EMBED_LINKS };
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        final Message m = handler.getNowPlaying(event.getJDA());
        if (m == null) {
            event.reply(handler.getNoMusicPlaying(event.getJDA()));
            this.bot.getNowplayingHandler().clearLastNPMessage(event.getGuild());
        }
        else {
            event.reply(m, msg -> this.bot.getNowplayingHandler().setLastNPMessage(msg));
        }
    }
}
