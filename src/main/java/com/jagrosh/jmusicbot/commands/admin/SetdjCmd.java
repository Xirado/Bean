// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class SetdjCmd extends AdminCommand
{
    public SetdjCmd(final Bot bot) {
        this.name = "setdj";
        this.help = "sets the DJ role for certain music commands";
        this.arguments = "<rolename|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    
    @Override
    protected void execute(final CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " Please include a role name or NONE");
            return;
        }
        final Settings s = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().equalsIgnoreCase("none")) {
            s.setDJRole(null);
            event.reply(event.getClient().getSuccess() + " DJ role cleared; Only Admins can use the DJ commands.");
        }
        else {
            final List<Role> list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.reply(event.getClient().getWarning() + " No Roles found matching \"" + event.getArgs() + "\"");
            }
            else if (list.size() > 1) {
                event.reply(event.getClient().getWarning() + FormatUtil.listOfRoles(list, event.getArgs()));
            }
            else {
                s.setDJRole(list.get(0));
                event.reply(event.getClient().getSuccess() + " DJ commands can now be used by users with the **" + list.get(0).getName() + "** role.");
            }
        }
    }
}
