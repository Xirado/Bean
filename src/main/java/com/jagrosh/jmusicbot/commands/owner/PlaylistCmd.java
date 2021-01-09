// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.playlist.PlaylistLoader;

import java.io.IOException;
import java.util.List;

public class PlaylistCmd extends OwnerCommand
{
    private final Bot bot;
    
    public PlaylistCmd(final Bot bot) {
        this.bot = bot;
        this.guildOnly = false;
        this.name = "playlist";
        this.arguments = "<append|delete|make|setdefault>";
        this.help = "playlist management";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.children = new OwnerCommand[] { new ListCmd(), new AppendlistCmd(), new DeletelistCmd(), new MakelistCmd(), new DefaultlistCmd(bot) };
    }
    
    public void execute(final CommandEvent event) {
        final StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " Playlist Management Commands:\n");
        for (final Command cmd : this.children) {
            builder.append("\n`").append(event.getClient().getPrefix()).append(this.name).append(" ").append(cmd.getName()).append(" ").append((cmd.getArguments() == null) ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        }
        event.reply(builder.toString());
    }
    
    public class MakelistCmd extends OwnerCommand
    {
        public MakelistCmd() {
            this.name = "make";
            this.aliases = new String[] { "create" };
            this.help = "makes a new playlist";
            this.arguments = "<name>";
            this.guildOnly = false;
        }
        
        @Override
        protected void execute(final CommandEvent event) {
            final String pname = event.getArgs().replaceAll("\\s+", "_");
            if (PlaylistCmd.this.bot.getPlaylistLoader().getPlaylist(pname) == null) {
                try {
                    PlaylistCmd.this.bot.getPlaylistLoader().createPlaylist(pname);
                    event.reply(event.getClient().getSuccess() + " Successfully created playlist `" + pname + "`!");
                }
                catch (IOException e) {
                    event.reply(event.getClient().getError() + " I was unable to create the playlist: " + e.getLocalizedMessage());
                }
            }
            else {
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` already exists!");
            }
        }
    }
    
    public class DeletelistCmd extends OwnerCommand
    {
        public DeletelistCmd() {
            this.name = "delete";
            this.aliases = new String[] { "remove" };
            this.help = "deletes an existing playlist";
            this.arguments = "<name>";
            this.guildOnly = false;
        }
        
        @Override
        protected void execute(final CommandEvent event) {
            final String pname = event.getArgs().replaceAll("\\s+", "_");
            if (PlaylistCmd.this.bot.getPlaylistLoader().getPlaylist(pname) == null) {
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` doesn't exist!");
            }
            else {
                try {
                    PlaylistCmd.this.bot.getPlaylistLoader().deletePlaylist(pname);
                    event.reply(event.getClient().getSuccess() + " Successfully deleted playlist `" + pname + "`!");
                }
                catch (IOException e) {
                    event.reply(event.getClient().getError() + " I was unable to delete the playlist: " + e.getLocalizedMessage());
                }
            }
        }
    }
    
    public class AppendlistCmd extends OwnerCommand
    {
        public AppendlistCmd() {
            this.name = "append";
            this.aliases = new String[] { "add" };
            this.help = "appends songs to an existing playlist";
            this.arguments = "<name> <URL> | <URL> | ...";
            this.guildOnly = false;
        }
        
        @Override
        protected void execute(final CommandEvent event) {
            final String[] parts = event.getArgs().split("\\s+", 2);
            if (parts.length < 2) {
                event.reply(event.getClient().getError() + " Please include a playlist name and URLs to add!");
                return;
            }
            final String pname = parts[0];
            final PlaylistLoader.Playlist playlist = PlaylistCmd.this.bot.getPlaylistLoader().getPlaylist(pname);
            if (playlist == null) {
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` doesn't exist!");
            }
            else {
                final StringBuilder builder = new StringBuilder();
                playlist.getItems().forEach(item -> builder.append("\r\n").append(item));
                final String[] split;
                final String[] urls = split = parts[1].split("\\|");
                for (final String url : split) {
                    String u = url.trim();
                    if (u.startsWith("<") && u.endsWith(">")) {
                        u = u.substring(1, u.length() - 1);
                    }
                    builder.append("\r\n").append(u);
                }
                try {
                    PlaylistCmd.this.bot.getPlaylistLoader().writePlaylist(pname, builder.toString());
                    event.reply(event.getClient().getSuccess() + " Successfully added " + urls.length + " items to playlist `" + pname + "`!");
                }
                catch (IOException e) {
                    event.reply(event.getClient().getError() + " I was unable to append to the playlist: " + e.getLocalizedMessage());
                }
            }
        }
    }
    
    public class DefaultlistCmd extends AutoplaylistCmd
    {
        public DefaultlistCmd(final Bot bot) {
            super(bot);
            this.name = "setdefault";
            this.aliases = new String[] { "default" };
            this.arguments = "<playlistname|NONE>";
            this.guildOnly = true;
        }
    }
    
    public class ListCmd extends OwnerCommand
    {
        public ListCmd() {
            this.name = "all";
            this.aliases = new String[] { "available", "list" };
            this.help = "lists all available playlists";
            this.guildOnly = true;
        }
        
        @Override
        protected void execute(final CommandEvent event) {
            if (!PlaylistCmd.this.bot.getPlaylistLoader().folderExists()) {
                PlaylistCmd.this.bot.getPlaylistLoader().createFolder();
            }
            if (!PlaylistCmd.this.bot.getPlaylistLoader().folderExists()) {
                event.reply(event.getClient().getWarning() + " Playlists folder does not exist and could not be created!");
                return;
            }
            final List<String> list = PlaylistCmd.this.bot.getPlaylistLoader().getPlaylistNames();
            if (list == null) {
                event.reply(event.getClient().getError() + " Failed to load available playlists!");
            }
            else if (list.isEmpty()) {
                event.reply(event.getClient().getWarning() + " There are no playlists in the Playlists folder!");
            }
            else {
                final StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available playlists:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString());
            }
        }
    }
}
