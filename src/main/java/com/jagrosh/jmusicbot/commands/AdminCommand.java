// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands;

import com.jagrosh.jdautilities.command.Command;
import net.dv8tion.jda.api.Permission;

public abstract class AdminCommand extends Command
{
    public AdminCommand() {
        this.category = new Category("Admin", event -> {
            if (event.getAuthor().getId().equals(event.getClient().getOwnerId())) {
                return true;
            }
            else if (event.getGuild() == null) {
                return true;
            }
            else {
                return event.getMember().hasPermission(Permission.MANAGE_SERVER);
            }
        });
        this.guildOnly = true;
    }
}
