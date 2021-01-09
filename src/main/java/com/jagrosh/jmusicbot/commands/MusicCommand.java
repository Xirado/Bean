// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

public abstract class MusicCommand extends Command
{
    protected final Bot bot;
    protected boolean bePlaying;
    protected boolean beListening;
    
    public MusicCommand(final Bot bot) {
        this.bot = bot;
        this.guildOnly = true;
        this.category = new Category("Music");
    }
    
    @Override
    protected void execute(final CommandEvent event) {
        final Settings settings = event.getClient().getSettingsFor(event.getGuild());
        final TextChannel tchannel = settings.getTextChannel(event.getGuild());
        if (tchannel != null && !event.getTextChannel().equals(tchannel)) {
            try {
                event.getMessage().delete().queue();
            }
            catch (PermissionException ex2) {}
            event.replyInDm(event.getClient().getError() + " You can only use that command in " + tchannel.getAsMention() + "!");
            return;
        }
        this.bot.getPlayerManager().setUpHandler(event.getGuild());
        if (this.bePlaying && !((AudioHandler)event.getGuild().getAudioManager().getSendingHandler()).isMusicPlaying(event.getJDA())) {
            event.reply(event.getClient().getError() + " There must be music playing to use that!");
            return;
        }
        if (this.beListening) {
            VoiceChannel current = event.getGuild().getSelfMember().getVoiceState().getChannel();
            if (current == null) {
                current = settings.getVoiceChannel(event.getGuild());
            }
            final GuildVoiceState userState = event.getMember().getVoiceState();
            if (!userState.inVoiceChannel() || userState.isDeafened() || (current != null && !userState.getChannel().equals(current))) {
                event.replyError("You must be listening in " + ((current == null) ? "a voice channel" : ("**" + current.getName() + "**")) + " to use that!");
                return;
            }
            final VoiceChannel afkChannel = userState.getGuild().getAfkChannel();
            if (afkChannel != null && afkChannel.equals(userState.getChannel())) {
                event.replyError("You cannot use that command in an AFK channel!");
                return;
            }
            if (!event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
                try {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                }
                catch (PermissionException ex) {
                    event.reply(event.getClient().getError() + " I am unable to connect to **" + userState.getChannel().getName() + "**!");
                    return;
                }
            }
        }
        this.doCommand(event);
    }
    
    public abstract void doCommand(final CommandEvent p0);
}
