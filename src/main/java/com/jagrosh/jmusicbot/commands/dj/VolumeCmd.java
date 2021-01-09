// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;

public class VolumeCmd extends DJCommand
{
    public VolumeCmd(final Bot bot) {
        super(bot);
        this.name = "volume";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.help = "sets or shows volume";
        this.arguments = "[0-150]";
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        final Settings settings = event.getClient().getSettingsFor(event.getGuild());
        final int volume = handler.getPlayer().getVolume();
        if (event.getArgs().isEmpty()) {
            event.reply(FormatUtil.volumeIcon(volume) + " Current volume is `" + volume + "`");
        }
        else {
            int nvolume;
            try {
                nvolume = Integer.parseInt(event.getArgs());
            }
            catch (NumberFormatException e) {
                nvolume = -1;
            }
            if (nvolume < 0 || nvolume > 150) {
                event.reply(event.getClient().getError() + " Volume must be a valid integer between 0 and 150!");
            }
            else {
                handler.getPlayer().setVolume(nvolume);
                settings.setVolume(nvolume);
                event.reply(FormatUtil.volumeIcon(nvolume) + " Volume changed from `" + volume + "` to `" + nvolume + "`");
            }
        }
    }
}
