// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class EvalCmd extends OwnerCommand
{
    private final Bot bot;
    
    public EvalCmd(final Bot bot) {
        this.bot = bot;
        this.name = "eval";
        this.help = "evaluates nashorn code";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }
    
    @Override
    protected void execute(final CommandEvent event) {
        final ScriptEngine se = new ScriptEngineManager().getEngineByName("Nashorn");
        se.put("bot", this.bot);
        se.put("event", event);
        se.put("jda", event.getJDA());
        se.put("guild", event.getGuild());
        se.put("channel", event.getChannel());
        try {
            event.reply(event.getClient().getSuccess() + " Evaluated Successfully:\n```\n" + se.eval(event.getArgs()) + " ```");
        }
        catch (Exception e) {
            event.reply(event.getClient().getError() + " An exception was thrown:\n```\n" + e + " ```");
        }
    }
}
