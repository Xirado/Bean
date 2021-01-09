// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

public abstract class DJCommand extends MusicCommand
{
    public DJCommand(final Bot bot) {
        super(bot);
        this.category = new Category("DJ", event -> checkDJPermission(event));
    }
    
    public static boolean checkDJPermission(final CommandEvent event) {
        if (event.getAuthor().getId().equals(event.getClient().getOwnerId())) {
            return true;
        }
        if (event.getGuild() == null) {
            return true;
        }
        if (event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            return true;
        }
        final Settings settings = event.getClient().getSettingsFor(event.getGuild());
        final Role dj = settings.getRole(event.getGuild());
        return dj != null && (event.getMember().getRoles().contains(dj) || dj.getIdLong() == event.getGuild().getIdLong());
    }
}
