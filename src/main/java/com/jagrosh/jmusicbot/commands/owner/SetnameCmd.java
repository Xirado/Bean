// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

public class SetnameCmd extends OwnerCommand
{
    public SetnameCmd(final Bot bot) {
        this.name = "setname";
        this.help = "sets the name of the bot";
        this.arguments = "<name>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }
    
    @Override
    protected void execute(final CommandEvent event) {
        try {
            final String oldname = event.getSelfUser().getName();
            event.getSelfUser().getManager().setName(event.getArgs()).complete(false);
            event.reply(event.getClient().getSuccess() + " Name changed from `" + oldname + "` to `" + event.getArgs() + "`");
        }
        catch (RateLimitedException e) {
            event.reply(event.getClient().getError() + " Name can only be changed twice per hour!");
        }
        catch (Exception e2) {
            event.reply(event.getClient().getError() + " That name is not valid!");
        }
    }
}
