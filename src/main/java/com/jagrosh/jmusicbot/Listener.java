// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot;

import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter
{
    private final Bot bot;
    
    public Listener(final Bot bot) {
        this.bot = bot;
    }

    
    @Override
    public void onGuildMessageDelete(final GuildMessageDeleteEvent event) {
        this.bot.getNowplayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent e)
    {

    }
    @Override
    public void onShutdown(final ShutdownEvent event) {
        this.bot.shutdown();
    }

}
