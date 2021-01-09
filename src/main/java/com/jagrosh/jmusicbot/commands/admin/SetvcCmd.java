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
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.List;

public class SetvcCmd extends AdminCommand
{
    public SetvcCmd(final Bot bot) {
        this.name = "setvc";
        this.help = "sets the voice channel for playing music";
        this.arguments = "<channel|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    
    @Override
    protected void execute(final CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " Please include a voice channel or NONE");
            return;
        }
        final Settings s = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().equalsIgnoreCase("none")) {
            s.setVoiceChannel(null);
            event.reply(event.getClient().getSuccess() + " Music can now be played in any channel");
        }
        else {
            final List<VoiceChannel> list = FinderUtil.findVoiceChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.reply(event.getClient().getWarning() + " No Voice Channels found matching \"" + event.getArgs() + "\"");
            }
            else if (list.size() > 1) {
                event.reply(event.getClient().getWarning() + FormatUtil.listOfVChannels(list, event.getArgs()));
            }
            else {
                s.setVoiceChannel(list.get(0));
                event.reply(event.getClient().getSuccess() + " Music can now only be played in **" + list.get(0).getName() + "**");
            }
        }
    }
}
