// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

public class RemoveCmd extends MusicCommand
{
    public RemoveCmd(final Bot bot) {
        super(bot);
        this.name = "remove";
        this.help = "removes a song from the queue";
        this.arguments = "<position|ALL>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getQueue().isEmpty()) {
            event.replyError("There is nothing in the queue!");
            return;
        }
        if (event.getArgs().equalsIgnoreCase("all")) {
            final int count = handler.getQueue().removeAll(event.getAuthor().getIdLong());
            if (count == 0) {
                event.replyWarning("You don't have any songs in the queue!");
            }
            else {
                event.replySuccess("Successfully removed your " + count + " entries.");
            }
            return;
        }
        int pos;
        try {
            pos = Integer.parseInt(event.getArgs());
        }
        catch (NumberFormatException e) {
            pos = 0;
        }
        if (pos < 1 || pos > handler.getQueue().size()) {
            event.replyError("Position must be a valid integer between 1 and " + handler.getQueue().size() + "!");
            return;
        }
        final Settings settings = event.getClient().getSettingsFor(event.getGuild());
        boolean isDJ = event.getMember().hasPermission(Permission.MANAGE_SERVER);
        if (!isDJ) {
            isDJ = event.getMember().getRoles().contains(settings.getRole(event.getGuild()));
        }
        final QueuedTrack qt = handler.getQueue().get(pos - 1);
        if (qt.getIdentifier() == event.getAuthor().getIdLong()) {
            handler.getQueue().remove(pos - 1);
            event.replySuccess("Removed **" + qt.getTrack().getInfo().title + "** from the queue");
        }
        else if (isDJ) {
            handler.getQueue().remove(pos - 1);
            User u;
            try {
                u = event.getJDA().getUserById(qt.getIdentifier());
            }
            catch (Exception e2) {
                u = null;
            }
            event.replySuccess("Removed **" + qt.getTrack().getInfo().title + "** from the queue (requested by " + ((u == null) ? "someone" : ("**" + u.getName() + "**")) + ")");
        }
        else {
            event.replyError("You cannot remove **" + qt.getTrack().getInfo().title + "** because you didn't add it!");
        }
    }
}
