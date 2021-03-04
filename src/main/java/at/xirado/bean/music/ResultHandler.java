package at.xirado.bean.music;

import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.main.DiscordBot;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.concurrent.TimeUnit;

public class ResultHandler implements AudioLoadResultHandler
{
    private final Message m;
    private final CommandEvent event;
    private final boolean ytsearch;

    public static AudioHandler getHandler(Guild g)
    {
        if(g.getAudioManager().getSendingHandler() == null)
            DiscordBot.instance.musicinstance.getPlayerManager().setUpHandler(g);
        return (AudioHandler)g.getAudioManager().getSendingHandler();

    }
    public static boolean init(CommandEvent event)
    {
        VoiceChannel current = event.getGuild().getSelfMember().getVoiceState().getChannel();
        final GuildVoiceState userState = event.getMember().getVoiceState();
        if (!userState.inVoiceChannel() || userState.isDeafened() || (current != null && !userState.getChannel().equals(current))) {
            event.replyError("You must be listening in " + ((current == null) ? "a voice channel" : ("**" + current.getName() + "**")) + " to use that!");
            return true;
        }
        final VoiceChannel afkChannel = userState.getGuild().getAfkChannel();
        if (afkChannel != null && afkChannel.equals(userState.getChannel())) {
            event.replyError("You cannot use that command in an AFK channel!");
            return true;
        }
        if (!event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
            try {
                event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
            }
            catch (PermissionException ex) {
                event.replyError("I am unable to connect to **" + userState.getChannel().getName() + "**!");
                return true;
            }
        }
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if(handler == null)
            DiscordBot.instance.musicinstance.getPlayerManager().setUpHandler(event.getGuild());
        return false;
    }

    public ResultHandler(final Message m, final CommandEvent event, final boolean ytsearch) {
        this.m = m;
        this.event = event;
        this.ytsearch = ytsearch;
    }

    private void loadSingle(final AudioTrack track, final AudioPlaylist playlist) {
        if (DiscordBot.instance.musicinstance.getConfig().isTooLong(track)) {
            this.m.editMessage(FormatUtil.filter(" This track (**" + track.getInfo().title + "**) is longer than the allowed maximum: `" + FormatUtil.formatTime(track.getDuration()) + "` > `" + FormatUtil.formatTime(DiscordBot.instance.musicinstance.getConfig().getMaxSeconds() * 1000L) + "`")).queue();
            return;
        }
        final AudioHandler handler = (AudioHandler)this.event.getGuild().getAudioManager().getSendingHandler();
        final int pos = handler.addTrack(new QueuedTrack(track, this.event.getAuthor())) + 1;
        String addMsg;
        if(pos == 0)
        {
            addMsg = FormatUtil.filter("Now playing **"+track.getInfo().title+"**. (`"+FormatUtil.formatTime(track.getDuration())+"`)");
        }else
        {
            addMsg = FormatUtil.filter("Added **"+track.getInfo().title+"** (`"+FormatUtil.formatTime(track.getDuration())+"`) to queue. (Position "+pos+")");
        }
        if (playlist == null || !this.event.getSelfMember().hasPermission(this.event.getChannel(), Permission.MESSAGE_ADD_REACTION)) {
            this.m.editMessage(addMsg).queue();
        }
        else {
            new ButtonMenu.Builder()
                    .setText(addMsg + "\nThis playlist has **" + playlist.getTracks().size() + "** tracks attached. Select " + "\ud83d\udce5" + " to load it.")
                    .setChoices("\ud83d\udce5", "\ud83d\udeab")
                    .setEventWaiter(DiscordBot.instance.musicinstance.getWaiter())
                    .setTimeout(30L, TimeUnit.SECONDS)
                    .setAction(re -> {
                        if (re.getName().equals("\ud83d\udce5")) {
                            this.m.editMessage("\nLoaded **" + this.loadPlaylist(playlist, track) + "** additional tracks!").queue();
                        }
                        else {
                            this.m.delete().queue();
                        }
                    }).setFinalAction(m -> {
                try {
                    m.clearReactions().queue(null,null);
                }
                catch (PermissionException ex) {}
            }).build().display(this.m);
        }
    }

    private int loadPlaylist(final AudioPlaylist playlist, final AudioTrack exclude) {
        final int[] count = { 0 };
        final Object o;
        final int n;
        playlist.getTracks().stream().forEach(track -> {
            if (!DiscordBot.instance.musicinstance.getConfig().isTooLong(track) && !track.equals(exclude)) {
                AudioHandler handler = (AudioHandler)this.event.getGuild().getAudioManager().getSendingHandler();
                handler.addTrack(new QueuedTrack(track, this.event.getAuthor()));
                count[0]++;
            }
        });
        return count[0]+1;
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        this.loadSingle(track, null);
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        if (playlist.getTracks().size() == 1 || playlist.isSearchResult()) {
            final AudioTrack single = (playlist.getSelectedTrack() == null) ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
            this.loadSingle(single, null);
        }
        else if (playlist.getSelectedTrack() != null) {
            final AudioTrack single = playlist.getSelectedTrack();
            this.loadSingle(single, playlist);
        }
        else {
            final int count = this.loadPlaylist(playlist, null);
            if (count == 0) {
                this.m.editMessage(FormatUtil.filter(" All entries in the playlist " + ((playlist.getName() == null) ? "" : ("(**" + playlist.getName() + "**) ")) + "were longer than the allowed maximum (`" + DiscordBot.instance.musicinstance.getConfig().getMaxTime() + "`)")).queue();
            }
            else {
                this.m.editMessage(FormatUtil.filter(" Found " + ((playlist.getName() == null) ? "a playlist" : ("playlist **" + playlist.getName() + "**")) + " with `" + playlist.getTracks().size() + "` entries; added to the queue!" + ((count < playlist.getTracks().size()) ? ("\nTracks longer than the allowed maximum (`" + DiscordBot.instance.musicinstance.getConfig().getMaxTime() + "`) have been omitted.") : ""))).queue();
            }
        }
    }

    @Override
    public void noMatches() {
        if (this.ytsearch) {
            this.m.editMessage(FormatUtil.filter("No results found for `" + this.event.getArguments().getAsString(0) + "`.")).queue();
        }
        else {
            DiscordBot.instance.musicinstance.getPlayerManager().loadItemOrdered(this.event.getGuild(), "ytsearch:" + this.event.getArguments().getAsString(0), new ResultHandler(this.m, this.event, true));
        }
    }

    @Override
    public void loadFailed(final FriendlyException throwable) {
        if (throwable.severity == FriendlyException.Severity.COMMON) {
            this.m.editMessage("Error loading: " + throwable.getMessage()).queue();
        }
        else {
            this.m.editMessage("Error loading track.").queue();
        }
    }
}
