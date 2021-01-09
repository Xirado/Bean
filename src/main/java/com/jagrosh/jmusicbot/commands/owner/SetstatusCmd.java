// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import net.dv8tion.jda.api.OnlineStatus;

public class SetstatusCmd extends OwnerCommand
{
    public SetstatusCmd(final Bot bot) {
        this.name = "setstatus";
        this.help = "sets the status the bot displays";
        this.arguments = "<status>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }
    
    @Override
    protected void execute(final CommandEvent event) {
        try {
            final OnlineStatus status = OnlineStatus.fromKey(event.getArgs());
            if (status == OnlineStatus.UNKNOWN) {
                event.replyError("Please include one of the following statuses: `ONLINE`, `IDLE`, `DND`, `INVISIBLE`");
            }
            else {
                event.getJDA().getPresence().setStatus(status);
                event.replySuccess("Set the status to `" + status.getKey().toUpperCase() + "`");
            }
        }
        catch (Exception e) {
            event.reply(event.getClient().getError() + " The status could not be set!");
        }
    }
}
