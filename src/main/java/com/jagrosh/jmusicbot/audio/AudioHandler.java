// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.audio;

import com.jagrosh.jmusicbot.playlist.PlaylistLoader;
import com.jagrosh.jmusicbot.queue.FairQueue;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AudioHandler extends AudioEventAdapter implements AudioSendHandler
{
    private final FairQueue<QueuedTrack> queue;
    private final List<AudioTrack> defaultQueue;
    private final Set<String> votes;
    private final PlayerManager manager;
    private final AudioPlayer audioPlayer;
    private final long guildId;
    private AudioFrame lastFrame;
    
    protected AudioHandler(final PlayerManager manager, final Guild guild, final AudioPlayer player) {
        this.queue = new FairQueue<QueuedTrack>();
        this.defaultQueue = new LinkedList<AudioTrack>();
        this.votes = new HashSet<String>();
        this.manager = manager;
        this.audioPlayer = player;
        this.guildId = guild.getIdLong();
    }
    
    public int addTrackToFront(final QueuedTrack qtrack) {
        if (this.audioPlayer.getPlayingTrack() == null) {
            this.audioPlayer.playTrack(qtrack.getTrack());
            return -1;
        }
        this.queue.addAt(0, qtrack);
        return 0;
    }
    
    public int addTrack(final QueuedTrack qtrack) {
        if (this.audioPlayer.getPlayingTrack() == null) {
            this.audioPlayer.playTrack(qtrack.getTrack());
            return -1;
        }
        return this.queue.add(qtrack);
    }
    
    public FairQueue<QueuedTrack> getQueue() {
        return this.queue;
    }
    
    public void stopAndClear() {
        this.queue.clear();
        this.defaultQueue.clear();
        this.audioPlayer.stopTrack();
    }
    
    public boolean isMusicPlaying(final JDA jda) {
        return this.guild(jda).getSelfMember().getVoiceState().inVoiceChannel() && this.audioPlayer.getPlayingTrack() != null;
    }
    
    public Set<String> getVotes() {
        return this.votes;
    }
    
    public AudioPlayer getPlayer() {
        return this.audioPlayer;
    }
    
    public long getRequester() {
        if (this.audioPlayer.getPlayingTrack() == null || this.audioPlayer.getPlayingTrack().getUserData(Long.class) == null) {
            return 0L;
        }
        return this.audioPlayer.getPlayingTrack().getUserData(Long.class);
    }
    
    public boolean playFromDefault() {
        if (!this.defaultQueue.isEmpty()) {
            this.audioPlayer.playTrack(this.defaultQueue.remove(0));
            return true;
        }
        final Settings settings = this.manager.getBot().getSettingsManager().getSettings(this.guildId);
        if (settings == null || settings.getDefaultPlaylist() == null) {
            return false;
        }
        final PlaylistLoader.Playlist pl = this.manager.getBot().getPlaylistLoader().getPlaylist(settings.getDefaultPlaylist());
        if (pl == null || pl.getItems().isEmpty()) {
            return false;
        }
        pl.loadTracks(this.manager, at -> {
            if (this.audioPlayer.getPlayingTrack() == null) {
                this.audioPlayer.playTrack(at);
            }
            else {
                this.defaultQueue.add(at);
            }
            return;
        }, () -> {
            if (pl.getTracks().isEmpty() && !this.manager.getBot().getConfig().getStay()) {
                this.manager.getBot().closeAudioConnection(this.guildId);
            }
            return;
        });
        return true;
    }
    
    @Override
    public void onTrackEnd(final AudioPlayer player, final AudioTrack track, final AudioTrackEndReason endReason) {
        if (endReason == AudioTrackEndReason.FINISHED && this.manager.getBot().getSettingsManager().getSettings(this.guildId).getRepeatMode()) {
            this.queue.add(new QueuedTrack(track.makeClone(), (track.getUserData(Long.class) == null) ? 0L : track.getUserData(Long.class)));
        }
        if (this.queue.isEmpty()) {
            if (!this.playFromDefault()) {
                this.manager.getBot().getNowplayingHandler().onTrackUpdate(this.guildId, null, this);
                if (!this.manager.getBot().getConfig().getStay()) {
                    this.manager.getBot().closeAudioConnection(this.guildId);
                }
                player.setPaused(false);
            }
        }
        else {
            final QueuedTrack qt = this.queue.pull();
            player.playTrack(qt.getTrack());
        }
    }
    
    @Override
    public void onTrackStart(final AudioPlayer player, final AudioTrack track) {
        this.votes.clear();
        this.manager.getBot().getNowplayingHandler().onTrackUpdate(this.guildId, track, this);
    }
    
    public Message getNowPlaying(final JDA jda) {
        if (this.isMusicPlaying(jda)) {
            final Guild guild = this.guild(jda);
            final AudioTrack track = this.audioPlayer.getPlayingTrack();
            final MessageBuilder mb = new MessageBuilder();
            mb.append((CharSequence)FormatUtil.filter(this.manager.getBot().getConfig().getSuccess() + " **Now Playing in " + guild.getSelfMember().getVoiceState().getChannel().getName() + "...**"));
            final EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(guild.getSelfMember().getColor());
            if (this.getRequester() != 0L) {
                final User u = guild.getJDA().getUserById(this.getRequester());
                if (u == null) {
                    eb.setAuthor("Unknown (ID:" + this.getRequester() + ")", null, null);
                }
                else {
                    eb.setAuthor(u.getName() + "#" + u.getDiscriminator(), null, u.getEffectiveAvatarUrl());
                }
            }
            try {
                eb.setTitle(track.getInfo().title, track.getInfo().uri);
            }
            catch (Exception e) {
                eb.setTitle(track.getInfo().title);
            }
            if (track instanceof YoutubeAudioTrack && this.manager.getBot().getConfig().useNPImages()) {
                eb.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/mqdefault.jpg");
            }
            if (track.getInfo().author != null && !track.getInfo().author.isEmpty()) {
                eb.setFooter("Source: " + track.getInfo().author, null);
            }
            final double progress = this.audioPlayer.getPlayingTrack().getPosition() / (double)track.getDuration();
            eb.setDescription((this.audioPlayer.isPaused() ? "\u23f8" : "\u25b6") + " " + FormatUtil.progressBar(progress) + " `[" + FormatUtil.formatTime(track.getPosition()) + "/" + FormatUtil.formatTime(track.getDuration()) + "]` " + FormatUtil.volumeIcon(this.audioPlayer.getVolume()));
            return mb.setEmbed(eb.build()).build();
        }
        return null;
    }
    
    public Message getNoMusicPlaying(final JDA jda) {
        final Guild guild = this.guild(jda);
        return new MessageBuilder().setContent(FormatUtil.filter(this.manager.getBot().getConfig().getSuccess() + " **Now Playing...**")).setEmbed(new EmbedBuilder().setTitle("No music playing").setDescription("\u23f9 " + FormatUtil.progressBar(-1.0) + " " + FormatUtil.volumeIcon(this.audioPlayer.getVolume())).setColor(guild.getSelfMember().getColor()).build()).build();
    }
    
    public String getTopicFormat(final JDA jda) {
        if (this.isMusicPlaying(jda)) {
            final long userid = this.getRequester();
            final AudioTrack track = this.audioPlayer.getPlayingTrack();
            String title = track.getInfo().title;
            if (title == null || title.equals("Unknown Title")) {
                title = track.getInfo().uri;
            }
            return "**" + title + "** [" + ((userid == 0L) ? "autoplay" : ("<@" + userid + ">")) + "]\n" + (this.audioPlayer.isPaused() ? "\u23f8" : "\u25b6") + " [" + FormatUtil.formatTime(track.getDuration()) + "] " + FormatUtil.volumeIcon(this.audioPlayer.getVolume());
        }
        return "No music playing \u23f9 " + FormatUtil.volumeIcon(this.audioPlayer.getVolume());
    }
    
    @Override
    public boolean canProvide() {
        this.lastFrame = this.audioPlayer.provide();
        return this.lastFrame != null;
    }
    
    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(this.lastFrame.getData());
    }
    
    @Override
    public boolean isOpus() {
        return true;
    }
    
    private Guild guild(final JDA jda) {
        return jda.getGuildById(this.guildId);
    }
}
