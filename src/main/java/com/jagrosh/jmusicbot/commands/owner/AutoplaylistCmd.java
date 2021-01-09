// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.settings.Settings;

public class AutoplaylistCmd extends OwnerCommand
{
    private final Bot bot;
    
    public AutoplaylistCmd(final Bot bot) {
        this.bot = bot;
        this.guildOnly = true;
        this.name = "autoplaylist";
        this.arguments = "<name|NONE>";
        this.help = "sets the default playlist for the server";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    
    public void execute(final CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " Please include a playlist name or NONE");
            return;
        }
        if (event.getArgs().equalsIgnoreCase("none")) {
            final Settings settings = event.getClient().getSettingsFor(event.getGuild());
            settings.setDefaultPlaylist(null);
            event.reply(event.getClient().getSuccess() + " Cleared the default playlist for **" + event.getGuild().getName() + "**");
            return;
        }
        final String pname = event.getArgs().replaceAll("\\s+", "_");
        if (this.bot.getPlaylistLoader().getPlaylist(pname) == null) {
            event.reply(event.getClient().getError() + " Could not find `" + pname + ".txt`!");
        }
        else {
            final Settings settings2 = event.getClient().getSettingsFor(event.getGuild());
            settings2.setDefaultPlaylist(pname);
            event.reply(event.getClient().getSuccess() + " The default playlist for **" + event.getGuild().getName() + "** is now `" + pname + "`");
        }
    }
}
