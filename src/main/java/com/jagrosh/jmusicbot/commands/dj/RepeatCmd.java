// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.settings.Settings;

public class RepeatCmd extends DJCommand
{
    public RepeatCmd(final Bot bot) {
        super(bot);
        this.name = "repeat";
        this.help = "re-adds music to the queue when finished";
        this.arguments = "[on|off]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }
    
    @Override
    protected void execute(final CommandEvent event) {
        final Settings settings = event.getClient().getSettingsFor(event.getGuild());
        boolean value;
        if (event.getArgs().isEmpty()) {
            value = !settings.getRepeatMode();
        }
        else if (event.getArgs().equalsIgnoreCase("true") || event.getArgs().equalsIgnoreCase("on")) {
            value = true;
        }
        else {
            if (!event.getArgs().equalsIgnoreCase("false") && !event.getArgs().equalsIgnoreCase("off")) {
                event.replyError("Valid options are `on` or `off` (or leave empty to toggle)");
                return;
            }
            value = false;
        }
        settings.setRepeatMode(value);
        event.replySuccess("Repeat mode is now `" + (value ? "ON" : "OFF") + "`");
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
    }
}
