// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import net.dv8tion.jda.api.entities.Activity;

public class SetgameCmd extends OwnerCommand
{
    public SetgameCmd(final Bot bot) {
        this.name = "setgame";
        this.help = "sets the game the bot is playing";
        this.arguments = "[action] [game]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
        this.children = new OwnerCommand[] { new SetlistenCmd(), new SetstreamCmd(), new SetwatchCmd() };
    }
    
    @Override
    protected void execute(final CommandEvent event) {
        final String title = event.getArgs().toLowerCase().startsWith("playing") ? event.getArgs().substring(7).trim() : event.getArgs();
        try {
            event.getJDA().getPresence().setActivity(title.isEmpty() ? null : Activity.playing(title));
            event.reply(event.getClient().getSuccess() + " **" + event.getSelfUser().getName() + "** is " + (title.isEmpty() ? "no longer playing anything." : ("now playing `" + title + "`")));
        }
        catch (Exception e) {
            event.reply(event.getClient().getError() + " The game could not be set!");
        }
    }
    
    private class SetstreamCmd extends OwnerCommand
    {
        private SetstreamCmd() {
            this.name = "stream";
            this.aliases = new String[] { "twitch", "streaming" };
            this.help = "sets the game the bot is playing to a stream";
            this.arguments = "<username> <game>";
            this.guildOnly = false;
        }
        
        @Override
        protected void execute(final CommandEvent event) {
            final String[] parts = event.getArgs().split("\\s+", 2);
            if (parts.length < 2) {
                event.replyError("Please include a twitch username and the name of the game to 'stream'");
                return;
            }
            try {
                event.getJDA().getPresence().setActivity(Activity.streaming(parts[1], "https://twitch.tv/" + parts[0]));
                event.replySuccess("**" + event.getSelfUser().getName() + "** is now streaming `" + parts[1] + "`");
            }
            catch (Exception e) {
                event.reply(event.getClient().getError() + " The game could not be set!");
            }
        }
    }
    
    private class SetlistenCmd extends OwnerCommand
    {
        private SetlistenCmd() {
            this.name = "listen";
            this.aliases = new String[] { "listening" };
            this.help = "sets the game the bot is listening to";
            this.arguments = "<title>";
            this.guildOnly = false;
        }
        
        @Override
        protected void execute(final CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.replyError("Please include a title to listen to!");
                return;
            }
            final String title = event.getArgs().toLowerCase().startsWith("to") ? event.getArgs().substring(2).trim() : event.getArgs();
            try {
                event.getJDA().getPresence().setActivity(Activity.listening(title));
                event.replySuccess("**" + event.getSelfUser().getName() + "** is now listening to `" + title + "`");
            }
            catch (Exception e) {
                event.reply(event.getClient().getError() + " The game could not be set!");
            }
        }
    }
    
    private class SetwatchCmd extends OwnerCommand
    {
        private SetwatchCmd() {
            this.name = "watch";
            this.aliases = new String[] { "watching" };
            this.help = "sets the game the bot is watching";
            this.arguments = "<title>";
            this.guildOnly = false;
        }
        
        @Override
        protected void execute(final CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.replyError("Please include a title to watch!");
                return;
            }
            final String title = event.getArgs();
            try {
                event.getJDA().getPresence().setActivity(Activity.watching(title));
                event.replySuccess("**" + event.getSelfUser().getName() + "** is now watching `" + title + "`");
            }
            catch (Exception e) {
                event.reply(event.getClient().getError() + " The game could not be set!");
            }
        }
    }
}
