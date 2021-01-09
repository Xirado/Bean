// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;

import java.util.List;

public class PlaylistsCmd extends MusicCommand
{
    public PlaylistsCmd(final Bot bot) {
        super(bot);
        this.name = "playlists";
        this.help = "shows the available playlists";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
        this.beListening = false;
        this.beListening = false;
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        if (!this.bot.getPlaylistLoader().folderExists()) {
            this.bot.getPlaylistLoader().createFolder();
        }
        if (!this.bot.getPlaylistLoader().folderExists()) {
            event.reply(event.getClient().getWarning() + " Playlists folder does not exist and could not be created!");
            return;
        }
        final List<String> list = this.bot.getPlaylistLoader().getPlaylistNames();
        if (list == null) {
            event.reply(event.getClient().getError() + " Failed to load available playlists!");
        }
        else if (list.isEmpty()) {
            event.reply(event.getClient().getWarning() + " There are no playlists in the Playlists folder!");
        }
        else {
            final StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available playlists:\n");
            list.forEach(str -> builder.append("`").append(str).append("` "));
            builder.append("\nType `").append(event.getClient().getTextualPrefix()).append("play playlist <name>` to play a playlist");
            event.reply(builder.toString());
        }
    }
}
